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
import mockit.Mock
import mockit.MockUp
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.config.SocketConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.test.assertNotNull

/**
 * @author taichi
 */
class Testing {

    companion object {

        fun client(): CloseableHttpClient {
            val builder = HttpClientBuilder.create()
            builder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(2000).build())
            builder.setRetryHandler(DefaultHttpRequestRetryHandler(0, false))
            return builder.build()
        }

        @Throws(Exception::class)
        fun request(request: HttpUriRequest, fn: (HttpResponse)-> Unit) {
            client().use { c -> fn(c.execute(request))  }
        }

        @Throws(Exception::class)
        fun read(response: HttpResponse): String {
            val entity = response.entity ?: return ""
            Scanner(entity.content,
                    StandardCharsets.UTF_8.name()).use { scanner -> return scanner.useDelimiter("\\A").next() }
        }

        fun mustCall(): HttpHandler {
            return object : MockUp<HttpHandler>() {
                @Mock(invocations = 1)
                @Throws(Exception::class)
                fun handleRequest(exchange: HttpServerExchange) {
                    assertNotNull(exchange)
                }
            }.mockInstance
        }

        fun empty(): HttpHandler {
            return HttpHandler { exc -> throw AssertionError() }
        }

        fun useALL(target: Class<*>) {
            val h = ConsoleHandler()
            h.level = Level.ALL
            val logger = Logger.getLogger(target.name)
            logger.addHandler(h)
            logger.level = Level.ALL
        }
    }

}
