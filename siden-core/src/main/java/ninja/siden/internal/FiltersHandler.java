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
import ninja.siden.FilterChain;
import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.def.FilterDef;

import java.util.ArrayList;
import java.util.List;

/**
 * @author taichi
 */
public class FiltersHandler implements HttpHandler {

    HttpHandler next;

    List<FilterDef> filters = new ArrayList<>();

    public FiltersHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (filters.size() < 1) {
            next.handleRequest(exchange);
            return;
        }
        SimpleChain chain = new SimpleChain(exchange);
        chain.next();
    }

    public void add(FilterDef model) {
        this.filters.add(model);
    }

    enum ChainState {
        HasNext, NoMore;
    }

    class SimpleChain implements FilterChain {

        int cursor;

        HttpServerExchange exchange;

        Request request;

        Response response;

        public SimpleChain(HttpServerExchange exchange) {
            super();
            this.exchange = exchange;
            this.request = exchange.getAttachment(Core.REQUEST);
            this.response = exchange.getAttachment(Core.RESPONSE);
        }

        @Override
        public Object next() throws Exception {
            for (int index = cursor++; index < filters.size(); index = cursor++) {
                FilterDef f = filters.get(index);
                if (f.resolve(exchange)) {
                    f.filter(request, response, this);
                    return ChainState.HasNext;
                }
            }
            next.handleRequest(exchange);
            return ChainState.NoMore;
        }
    }
}