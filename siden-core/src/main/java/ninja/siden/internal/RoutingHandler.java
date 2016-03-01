/*
 * Copyright 2014 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ninja.siden.internal;

import io.undertow.Undertow;
import io.undertow.predicate.Predicate;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ninja.siden.*;
import ninja.siden.def.ErrorCodeRoutingDef;
import ninja.siden.def.ExceptionalRoutingDef;
import org.xnio.OptionMap;

import java.util.*;

/**
 * @author taichi
 */
public class RoutingHandler implements HttpHandler {

    static final Set<String> ignorePackages = new HashSet<>();

    static {
        ignorePackages.add(App.class.getPackage().getName());
        ignorePackages.add(Undertow.class.getPackage().getName());
    }

    final List<Routing> routings = new ArrayList<>();

    final Map<Class<? extends Throwable>, ExceptionalRoutingDef<? extends Throwable>> exceptionalMappings = new HashMap<>();

    final Map<Integer, List<ErrorCodeRoutingDef>> errorCodeMappings = new HashMap<>();

    final HttpHandler next;

    public RoutingHandler(HttpHandler handler) {
        this.next = handler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        for (Routing route : routings) {
            if (route.predicate.resolve(exchange)) {
                HttpHandler hh = ex -> handle(ex, route.route::handle,
                        route.renderer);
                if (exchange.isInIoThread()) {
                    exchange.dispatch(hh);
                } else {
                    hh.handleRequest(exchange);
                }
                return;
            }
        }
        this.next.handleRequest(exchange);
    }

    boolean handle(Integer responseCode, HttpServerExchange exchange)
            throws Exception {
        if (exchange.isRequestChannelAvailable()) {
            exchange.setResponseCode(responseCode);
            List<ErrorCodeRoutingDef> list = this.errorCodeMappings
                    .getOrDefault(responseCode, Collections.emptyList());
            for (ErrorCodeRoutingDef route : list) {
                if (handle(exchange, route.getRoute()::handle, route.getRenderer())) {
                    return true;
                }
            }
        }
        return false;
    }

    <T extends Throwable> ExceptionalRoutingDef<T> find(T exception) {
        for (Class<?> c = exception.getClass(); c != null; c = c
                .getSuperclass()) {
            @SuppressWarnings("unchecked")
            ExceptionalRoutingDef<T> route = (ExceptionalRoutingDef<T>) this.exceptionalMappings.get(c);
            if (route != null) {
                return route;
            }
        }
        return null;
    }

    <T extends Throwable> boolean handle(T exception,
                                         HttpServerExchange exchange) throws Exception {
        ExceptionalRoutingDef<T> def = find(exception);
        return def != null && handle(exchange, (req, res) -> {
            exchange.setResponseCode(500);
            return def.getRoute().handle(exception, req, res);
        }, def.getRenderer());
    }

    boolean handle(HttpServerExchange exchange, Route fn, Renderer<?> renderer)
            throws Exception {
        try {
            Request request = exchange.getAttachment(Core.REQUEST);
            Response response = exchange.getAttachment(Core.RESPONSE);
            Object model = fn.handle(request, response);
            if (model != null) {
                if (model instanceof Optional) {
                    @SuppressWarnings("unchecked")
                    Optional<Object> opt = (Optional<Object>) model;
                    model = opt.map(v -> v).orElse(null);
                }
                if (model instanceof Integer) {
                    return handle((Integer) model, exchange);
                }
                if (model instanceof ExchangeState) {
                    return true;
                }
                if (model != null && contains(model.getClass()) == false) {
                    resolve(renderer, exchange).render(model, exchange);
                    return true;
                }
            }
            return handle(exchange.getResponseCode(), exchange);
        } catch (Exception ex) {
            if (exchange.isRequestChannelAvailable()) {
                if (handle(ex, exchange)) {
                    return true;
                }
            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    Renderer<Object> resolve(Renderer<?> renderer, HttpServerExchange exchange) {
        if (renderer == null) {
            OptionMap config = exchange.getAttachment(Core.CONFIG);
            return config.get(Config.DEFAULT_RENDERER);
        }
        return (Renderer<Object>) renderer;
    }

    boolean contains(Class<?> clazz) {
        String n = clazz.getName();
        return ignorePackages.stream().anyMatch(n::startsWith);
    }

    public void add(Predicate predicate, Route route, Renderer<?> renderer) {
        this.routings.add(new Routing(predicate, route, renderer));
    }

    public void add(ExceptionalRoutingDef<?> model) {
        this.exceptionalMappings.put(model.getType(), model);
    }

    public void add(ErrorCodeRoutingDef model) {
        List<ErrorCodeRoutingDef> list = this.errorCodeMappings.get(model
                .getCode());
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(model);
        this.errorCodeMappings.put(model.getCode(), list);
    }

    class Routing {
        Predicate predicate;
        Route route;
        Renderer<?> renderer;

        Routing(Predicate predicate, Route route, Renderer<?> renderer) {
            this.predicate = predicate;
            this.route = route;
            this.renderer = renderer;
        }
    }
}
