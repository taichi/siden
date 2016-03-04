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

import io.undertow.predicate.Predicates
import io.undertow.server.HttpServerExchange
import io.undertow.util.HeaderMap
import mockit.Mock
import mockit.MockUp
import mockit.integration.junit4.JMockit
import ninja.siden.Config
import ninja.siden.Renderer
import ninja.siden.Request
import ninja.siden.Response
import ninja.siden.def.ErrorCodeRoutingDef
import ninja.siden.def.ExceptionalRoutingDef
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import kotlin.test.assertNotNull

/**
 * @author taichi
 */
@RunWith(JMockit::class)
class RoutingHandlerTest {

    lateinit var exchange: HttpServerExchange

    @Before
    fun setUp() {
        this.exchange = object : MockUp<HttpServerExchange>() {
            val isInIoThread: Boolean
                @Mock
                get() = false
        }.mockInstance
        this.exchange.putAttachment(Core.CONFIG, Config.defaults().map)
        this.exchange.putAttachment(Core.REQUEST, SidenRequest(this.exchange))
        this.exchange.putAttachment(Core.RESPONSE, SidenResponse(this.exchange))
    }

    @Test
    @Throws(Exception::class)
    fun testNotMatch() {
        val target = RoutingHandler(Testing.mustCall())
        target.add(Predicates.falsePredicate(), { q, s -> "" }, null)
        target.handleRequest(this.exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleMatch() {
        val target = RoutingHandler(Testing.empty())
        target.add(Predicates.truePredicate(), { q, s -> "Hello" },
                object : MockUp<Renderer<Any>>() {
                    @Mock(invocations = 1)
                    @Throws(IOException::class)
                    @JvmName("render")
                    fun render(model: Any, sink: HttpServerExchange) {
                        assertEquals("Hello", model)
                        assertNotNull(sink)
                    }
                }.mockInstance)
        target.handleRequest(this.exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testReturnOptional() {
        val target = RoutingHandler(Testing.empty())
        target.add(Predicates.truePredicate(), { q, s -> Optional.of("Hello") },
                object : MockUp<Renderer<Any>>() {
                    @Mock(invocations = 1)
                    @Throws(IOException::class)
                    @JvmName("render")
                    fun render(model: Any, sink: HttpServerExchange) {
                        assertEquals("Hello", model)
                        assertNotNull(sink)
                    }
                }.mockInstance)
        target.handleRequest(this.exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testReturnStatusCode() {
        val target = RoutingHandler(Testing.empty())
        target.add(ErrorCodeRoutingDef(400, { r, s -> "Hey" }).render(object : MockUp<Renderer<Any>>() {
            @Mock(invocations = 1)
            @Throws(IOException::class)
            @JvmName("render")
            fun render(model: Any, sink: HttpServerExchange) {
                assertEquals("Hey", model)
                assertNotNull(sink)
            }
        }.mockInstance))

        target.add(Predicates.truePredicate(), { q, s -> 400 },
                object : Renderer<Any> {
                    override fun render(model: Any, sink: HttpServerExchange) {
                        throw AssertionError()
                    }
                })
        target.handleRequest(this.exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testSetStatusCode() {
        val target = RoutingHandler(Testing.empty())
        target.add(ErrorCodeRoutingDef(402, { r, s -> "Hey" }).render(object : MockUp<Renderer<Any>>() {
            @Mock(invocations = 1)
            @JvmName("render")
            internal fun render(model: Any, sink: HttpServerExchange) {
                assertEquals("Hey", model)
                assertNotNull(sink)
            }
        }.mockInstance))

        target.add(Predicates.truePredicate(), { q, s -> s.status(402) },
                object : Renderer<Any> {
                    override fun render(model: Any, sink: HttpServerExchange) {
                        throw AssertionError()
                    }
                })

        target.handleRequest(this.exchange)
        assertEquals(402, this.exchange.responseCode.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testEspeciallyPkgsAreNotRender() {
        this.exchange.putAttachment(Core.CONFIG, Config.defaults().map)
        this.exchange.putAttachment(Core.RESPONSE, SidenResponse(
                this.exchange))

        val target = RoutingHandler(Testing.empty())
        target.add(Predicates.truePredicate(),
                { q, s -> s.cookie("hoge", "fuga") },
                object : Renderer<Any> {
                    override fun render(model: Any, sink: HttpServerExchange) {
                        throw AssertionError()
                    }
                })

        target.handleRequest(this.exchange)
    }

    @Test
    @Throws(Exception::class)
    fun testResponseCodeSettigIsOnce() {
        this.exchange = object : MockUp<HttpServerExchange>() {
            val isInIoThread: Boolean
                @Mock
                get() = false

            val responseHeaders: HeaderMap
                @Mock
                get() = HeaderMap()

            @Mock(invocations = 1)
            fun setResponseCode(responseCode: Int): HttpServerExchange {
                assert(0 < responseCode)
                return mockInstance
            }

            @Mock(invocations = 1)
            fun endExchange(): HttpServerExchange {
                return mockInstance
            }
        }.mockInstance

        this.exchange.putAttachment(Core.CONFIG, Config.defaults().map)
        this.exchange.putAttachment(Core.RESPONSE, SidenResponse(this.exchange))

        val target = RoutingHandler(Testing.empty())
        target.add(Predicates.truePredicate(),
                { q, s -> s.redirect("/hoge/fuga/moge") }, null)

        this.exchange.putAttachment(Core.CONFIG, Config.defaults().map)
        this.exchange.putAttachment(Core.REQUEST, SidenRequest(this.exchange))
        this.exchange.putAttachment(Core.RESPONSE, SidenResponse(this.exchange))

        target.handleRequest(this.exchange)
    }

    internal class MyIoException : IOException() {
        companion object {
            private val serialVersionUID = 1L
        }
    }

    @Test
    @Throws(Exception::class)
    fun testExceptionalRouting() {
        this.exchange = object : MockUp<HttpServerExchange>() {
            val isInIoThread: Boolean
                @Mock
                get() = false

            val isRequestChannelAvailable: Boolean
                @Mock
                get() = true
        }.mockInstance
        val target = RoutingHandler(Testing.empty())

        target.add(ExceptionalRoutingDef(IOException::class.java
        ) { ex, res, req ->
            assertEquals(MyIoException::class.java, ex.javaClass)
            "Hey"
        }.render(object : MockUp<Renderer<Any>>() {
            @Mock(invocations = 1)
            @Throws(IOException::class)
            @JvmName("render")
            fun render(model: Any, sink: HttpServerExchange) {
                assertEquals("Hey", model)
                assertNotNull(sink)
            }
        }.mockInstance))

        target.add(Predicates.truePredicate(), { q, s -> throw MyIoException() }, object : Renderer<Any> {
            override fun render(model: Any, sink: HttpServerExchange) {
                throw AssertionError()
            }
        })

        this.exchange.putAttachment(Core.CONFIG, Config.defaults().map)
        this.exchange.putAttachment(Core.REQUEST, SidenRequest(this.exchange))
        this.exchange.putAttachment(Core.RESPONSE, SidenResponse(this.exchange))

        target.handleRequest(this.exchange)

        assertEquals(500, this.exchange.responseCode.toLong())
    }
}
