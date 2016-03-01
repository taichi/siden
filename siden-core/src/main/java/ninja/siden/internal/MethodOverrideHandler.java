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
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import ninja.siden.HttpMethod;

import java.util.Optional;

/**
 * @author taichi
 */
public class MethodOverrideHandler implements HttpHandler {

    static final HttpString HEADER = new HttpString("X-HTTP-Method-Override");

    static final String FORM = "_method";

    HttpHandler next;

    public MethodOverrideHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (Methods.POST.equals(exchange.getRequestMethod())) {
            String newMethod = exchange.getRequestHeaders().getFirst(HEADER);
            Optional<HttpString> opt = HttpMethod.find(newMethod);
            if (opt.isPresent()) {
                exchange.setRequestMethod(opt.get());
            } else {
                FormData data = exchange
                        .getAttachment(FormDataParser.FORM_DATA);
                if (data != null) {
                    FormData.FormValue fv = data.getFirst(FORM);
                    if (fv != null && fv.isFile() == false) {
                        HttpMethod.find(fv.getValue()).map(
                                exchange::setRequestMethod);
                    }
                }
            }
        }
        this.next.handleRequest(exchange);
    }
}
