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
package ninja.siden;

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author taichi
 */
public enum HttpMethod implements Predicate {

    GET(Methods.GET), HEAD(Methods.HEAD), POST(Methods.POST), PUT(Methods.PUT), DELETE(
            Methods.DELETE), TRACE(Methods.TRACE), OPTIONS(Methods.OPTIONS), CONNECT(
            Methods.CONNECT), PATCH(new HttpString("PATCH")), LINK(
            new HttpString("LINK")), UNLINK(new HttpString("UNLINK"));

    static final Map<HttpString, HttpMethod> methods = new HashMap<>();

    static {
        for (HttpMethod hm : HttpMethod.values()) {
            methods.put(hm.rawdata, hm);
        }
    }

    HttpString rawdata;

    private HttpMethod(HttpString string) {
        this.rawdata = string;
    }

    @Override
    public boolean resolve(HttpServerExchange value) {
        return this.rawdata.equals(value.getRequestMethod());
    }

    public static HttpMethod of(HttpServerExchange exchange) {
        return methods.getOrDefault(exchange.getRequestMethod(), GET);
    }

    public static Optional<HttpString> find(String method) {
        if (method == null || method.isEmpty()) {
            return Optional.empty();
        }
        String m = method.toUpperCase();
        return Optional.ofNullable(methods.get(new HttpString(m))).map(
                hm -> hm.rawdata);
    }
}
