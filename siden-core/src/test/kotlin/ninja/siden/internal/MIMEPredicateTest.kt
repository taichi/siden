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

import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

/**
 * @author taichi
 */
@RunWith(Parameterized::class)
class MIMEPredicateTest(internal var wait:

                        String, internal var request:

                        String, internal var `is`:

                        Boolean) {

    lateinit var exchange: HttpServerExchange

    @Before
    fun setUp() {
        this.exchange = HttpServerExchange(null)
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val p = MIMEPredicate.accept(wait)
        this.exchange.requestHeaders.add(Headers.ACCEPT, request)
        assertEquals(`is`, p.resolve(exchange))
    }

    companion object {

        @Parameters(name = "{0} {1}")
        @Throws(Exception::class)
        @JvmStatic
        fun parameters(): Iterable<Array<Any>> {
            return Arrays.asList(*arrayOf(arrayOf("application/json", "application/json", true), arrayOf("application/json", "APPLICATION/json", true), arrayOf("application/json", "application/JSON", true),

                    arrayOf("application/*", "application/json", true), arrayOf("*/json", "text/html", true), arrayOf("*/*", "application/json", true), arrayOf("*", "application/json", true),

                    arrayOf("application/json", "application/*", true), arrayOf("text/html", "*/json", true), arrayOf("application/json", "*/*", true), arrayOf("application/json", "*", true),

                    arrayOf("application/json", "application/xml", false), arrayOf("application/json", "text/json", false), arrayOf("application/*", "text/json", false),

                    arrayOf("application/json", "text/*", false)))
        }
    }
}
