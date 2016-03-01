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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import ninja.siden.Request;
import ninja.siden.Response;
import org.xnio.OptionMap;

import java.util.function.Predicate;

/**
 * @author taichi
 */
public class Core implements HttpHandler {

    public static final AttachmentKey<OptionMap> CONFIG = AttachmentKey
            .create(OptionMap.class);

    public static final AttachmentKey<Request> REQUEST = AttachmentKey
            .create(Request.class);

    public static final AttachmentKey<Response> RESPONSE = AttachmentKey
            .create(Response.class);

    final OptionMap config;
    final HttpHandler next;

    public Core(OptionMap config, HttpHandler next) {
        super();
        this.config = config;
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(CONFIG, config);
        exchange.putAttachment(REQUEST, new SidenRequest(exchange));
        exchange.putAttachment(RESPONSE, new SidenResponse(exchange));
        exchange.addExchangeCompleteListener((ex, next) -> {
            try {
                exchange.removeAttachment(CONFIG);
                exchange.removeAttachment(REQUEST);
                exchange.removeAttachment(RESPONSE);
            } finally {
                next.proceed();
            }
        });
        next.handleRequest(exchange);
    }

    public static io.undertow.predicate.Predicate adapt(Predicate<Request> fn) {
        return exchange -> {
            Request request = exchange.getAttachment(Core.REQUEST);
            return fn.test(request);
        };
    }
}
