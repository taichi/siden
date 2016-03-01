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
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderMap;
import io.undertow.util.Methods;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class MethodOverrideHandlerTest {

    HttpHandler target;

    @Before
    public void setUp() {
        this.target = new MethodOverrideHandler(Testing.mustCall());
    }

    @Test
    public void testNotOverride() throws Exception {
        HttpServerExchange exchange = new MockUp<HttpServerExchange>() {
            @Mock
            public HeaderMap getRequestHeaders() {
                throw new AssertionError();
            }

            @Mock
            public <T> T getAttachment(final AttachmentKey<T> key) {
                throw new AssertionError();
            }
        }.getMockInstance();

        exchange.setRequestMethod(Methods.GET);

        this.target.handleRequest(exchange);
    }

    @Test
    public void testOverrideFromHeader() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.POST);
        exchange.getRequestHeaders().put(MethodOverrideHandler.HEADER,
                "CONNECT");
        this.target.handleRequest(exchange);
        assertEquals(Methods.CONNECT, exchange.getRequestMethod());
    }

    @Test
    public void testOverrideFromForm() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.POST);

        FormData fd = new FormData(3);
        fd.add(MethodOverrideHandler.FORM, "PUT");
        exchange.putAttachment(FormDataParser.FORM_DATA, fd);
        this.target.handleRequest(exchange);
        assertEquals(Methods.PUT, exchange.getRequestMethod());
    }
}
