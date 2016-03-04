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
import io.undertow.util.Headers
import ninja.siden.Config
import ninja.siden.SecurityHeaders
import org.jboss.logging.Logger

/**
 * @author taichi
 */
class SecurityHandler(internal var next: HttpHandler) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        val config = exchange.getAttachment(Core.CONFIG)
        val rh = exchange.responseHeaders

        rh.add(SecurityHeaders.FRAME_OPTIONS, config.get(Config.FRAME_OPTIONS))

        if (config.get<Boolean>(Config.USE_XSS_PROTECTION)) {
            rh.add(SecurityHeaders.XSS_PROTECTION, "1; mode=block")
        }

        if (config.get<Boolean>(Config.USE_CONTENT_TYPE_OPTIONS)) {
            rh.add(SecurityHeaders.CONTENT_TYPE_OPTIONS, "nosniff")
        }

        exchange.addExchangeCompleteListener { ex, next ->
            try {
                if (rh.contains(Headers.CONTENT_TYPE) == false && rh.contains(Headers.SEC_WEB_SOCKET_ACCEPT) == false) {
                    LOG.warn(ex.requestURI + " Content-Type header doesn't exist.")
                }
            } finally {
                next.proceed()
            }
        }

        next.handleRequest(exchange)
    }

    companion object {
        internal val LOG = Logger.getLogger(SecurityHandler::class.java)

        @JvmOverloads fun addContentType(exchange: HttpServerExchange, type: String? = null) {
            val t = type ?: "application/octet-stream"
            val hm = exchange.responseHeaders
            val hv = hm.get(Headers.CONTENT_TYPE)
            if (hv == null || hv.isEmpty()) {
                hm.add(Headers.CONTENT_TYPE, t)
            }
        }
    }
}
