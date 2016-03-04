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
package ninja.siden.internal

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.util.AttachmentKey
import io.undertow.util.HeaderMap
import io.undertow.util.Methods
import mockit.Mock
import mockit.MockUp
import mockit.integration.junit4.JMockit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author taichi
 */
@RunWith(JMockit::class)
class MethodOverrideHandlerTest {

    lateinit var target: HttpHandler

    @Before
    fun setUp() {
        this.target = MethodOverrideHandler(Testing.mustCall())
    }

    @Test
    @Throws(Exception::class)
    fun testNotOverride() {
        val exchange = object : MockUp<HttpServerExchange>() {
            val requestHeaders: HeaderMap
                @Mock
                get() = throw AssertionError()

            @Mock
            fun <T> getAttachment(key: AttachmentKey<T>): T {
                throw AssertionError(key.toString())
            }
        }.mockInstance

        exchange.requestMethod = Methods.GET

        this.target.handleRequest(exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testOverrideFromHeader() {
        val exchange = HttpServerExchange(null)
        exchange.requestMethod = Methods.POST
        exchange.requestHeaders.put(MethodOverrideHandler.HEADER,
                "CONNECT")
        this.target.handleRequest(exchange)
        assertEquals(Methods.CONNECT, exchange.requestMethod)
    }

    @Test
    @Throws(Exception::class)
    fun testOverrideFromForm() {
        val exchange = HttpServerExchange(null)
        exchange.requestMethod = Methods.POST

        val fd = FormData(3)
        fd.add(MethodOverrideHandler.FORM, "PUT")
        exchange.putAttachment(FormDataParser.FORM_DATA, fd)
        this.target.handleRequest(exchange)
        assertEquals(Methods.PUT, exchange.requestMethod)
    }
}
