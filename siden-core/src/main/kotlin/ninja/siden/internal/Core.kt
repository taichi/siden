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
import io.undertow.util.AttachmentKey
import ninja.siden.Request
import ninja.siden.Response
import org.xnio.OptionMap

/**
 * @author taichi
 */
class Core(internal val config: OptionMap, internal val next: HttpHandler) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.putAttachment(CONFIG, config)
        exchange.putAttachment(REQUEST, SidenRequest(exchange))
        exchange.putAttachment(RESPONSE, SidenResponse(exchange))
        exchange.addExchangeCompleteListener { ex, next ->
            try {
                exchange.removeAttachment(CONFIG)
                exchange.removeAttachment(REQUEST)
                exchange.removeAttachment(RESPONSE)
            } finally {
                next.proceed()
            }
        }
        next.handleRequest(exchange)
    }

    companion object {

        @JvmField val CONFIG = AttachmentKey.create(OptionMap::class.java)

        @JvmField val REQUEST = AttachmentKey.create(Request::class.java)

        @JvmField val RESPONSE = AttachmentKey.create(Response::class.java)

        fun adapt(fn: (Request)->Boolean): io.undertow.predicate.Predicate {
            return io.undertow.predicate.Predicate { exchange ->
                val request = exchange.getAttachment(Core.REQUEST)
                fn(request)
            }
        }
    }
}
