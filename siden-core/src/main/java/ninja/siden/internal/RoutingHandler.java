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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.ExceptionalRoute;
import ninja.siden.Renderer;
import ninja.siden.RendererCustomizer;
import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.Route;
import ninja.siden.RoutingCustomizer;

import org.xnio.OptionMap;

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

	final Map<Class<? extends Throwable>, ExceptionalRouting<? extends Throwable>> exceptionalMappings = new HashMap<>();

	final Map<Integer, List<ErrorCodeRouting>> errorCodeMappings = new HashMap<>();

	final HttpHandler next;

	public RoutingHandler(HttpHandler handler) {
		this.next = handler;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		try {
			for (Routing route : routings) {
				if (route.predicate.resolve(exchange)) {
					handle(exchange,
							(req, res) -> route.route.handle(req, res),
							route.renderer);
					return;
				}
			}
			this.next.handleRequest(exchange);
		} catch (Exception ex) {
			if (exchange.isRequestChannelAvailable()) {
				if (handle(ex, exchange)) {
					return;
				}
			}
			throw ex;
		}
	}

	boolean handle(Integer responseCode, HttpServerExchange exchange)
			throws Exception {
		if (exchange.isRequestChannelAvailable()) {
			exchange.setResponseCode(responseCode);
			List<ErrorCodeRouting> list = this.errorCodeMappings
					.get(responseCode);
			if (list != null && list.isEmpty() == false) {
				for (ErrorCodeRouting route : list) {
					if (handle(exchange,
							(req, res) -> route.route.handle(req, res),
							route.renderer)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	<T extends Throwable> ExceptionalRouting<T> find(T exception) {
		for (Class<?> c = exception.getClass(); c != null; c = c
				.getSuperclass()) {
			@SuppressWarnings("unchecked")
			ExceptionalRouting<T> route = (ExceptionalRouting<T>) this.exceptionalMappings
					.get(c);
			if (route != null) {
				return route;
			}
		}
		return null;
	}

	<T extends Throwable> boolean handle(T exception,
			HttpServerExchange exchange) throws Exception {
		ExceptionalRouting<T> route = find(exception);
		return route != null && handle(exchange, (req, res) -> {
			exchange.setResponseCode(500);
			return route.route.handle(exception, req, res);
		}, route.renderer);
	}

	boolean handle(HttpServerExchange exchange, Route fn, Renderer renderer)
			throws Exception {
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
			if (contains(model.getClass()) == false) {
				resolve(renderer, exchange).render(model, exchange);
				return true;
			}
		}
		return handle(exchange.getResponseCode(), exchange);
	}

	Renderer resolve(Renderer renderer, HttpServerExchange exchange) {
		if (renderer == null) {
			OptionMap config = exchange.getAttachment(Core.CONFIG);
			return config.get(Config.DEFAULT_RENDERER);
		}
		return renderer;
	}

	boolean contains(Class<?> clazz) {
		String n = clazz.getName();
		return ignorePackages.stream().anyMatch(s -> n.startsWith(s));
	}

	public RoutingCustomizer add(Predicate predicate, Route route) {
		Routing r = new Routing(predicate, route);
		this.routings.add(r);
		return r;
	}

	public <EX extends Throwable> RendererCustomizer<?> add(Class<EX> type,
			ExceptionalRoute<EX> route) {
		ExceptionalRouting<EX> er = new ExceptionalRouting<>(type, route);
		this.exceptionalMappings.put(type, er);
		return er;
	}

	public RendererCustomizer<?> add(Integer errorCode, Route route) {
		ErrorCodeRouting ecr = new ErrorCodeRouting(route);
		List<ErrorCodeRouting> list = this.errorCodeMappings.get(errorCode);
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(ecr);
		this.errorCodeMappings.put(errorCode, list);
		return ecr;
	}
}
