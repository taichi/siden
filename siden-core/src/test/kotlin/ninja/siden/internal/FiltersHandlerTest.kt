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
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import mockit.Mock
import mockit.MockUp
import mockit.Mocked
import mockit.integration.junit4.JMockit
import ninja.siden.Filter
import ninja.siden.FilterChain
import ninja.siden.Request
import ninja.siden.Response
import ninja.siden.def.FilterDef
import ninja.siden.internal.FiltersHandler.SimpleChain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author taichi
 */
@RunWith(JMockit::class)
class FiltersHandlerTest {

    lateinit  var exchange: HttpServerExchange

    @Mocked
    lateinit var request: Request

    @Mocked
    lateinit var response: Response

    @Before
    fun setUp() {
        this.exchange = HttpServerExchange(null)
        this.exchange.putAttachment(Core.REQUEST, this.request)
        this.exchange.putAttachment(Core.RESPONSE, this.response)
    }

    @Test
    @Throws(Exception::class)
    fun testNoFilter() {
        object : MockUp<SimpleChain>() {
            @Mock(invocations = 0)
            @Throws(Exception::class)
            operator fun next(): Any? {
                return null
            }
        }
        val called = booleanArrayOf(false)
        val target = FiltersHandler(HttpHandler { exc -> called[0] = true })
        target.handleRequest(this.exchange)
        assertTrue(called[0])
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleCall() {
        var filter = Filter { req, res, chain -> chain.next() }

        val target = FiltersHandler(Testing.mustCall())
        target.add(FilterDef(Predicates.truePredicate(), filter))
        target.add(FilterDef(Predicates.falsePredicate(),
               Filter { req, res, ch -> throw AssertionError() }))
        target.add(FilterDef(Predicates.truePredicate(), filter))

        target.handleRequest(this.exchange)
    }
}
