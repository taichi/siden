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
import io.undertow.util.DateUtils
import io.undertow.util.Headers
import io.undertow.util.HttpString
import io.undertow.util.StatusCodes
import ninja.siden.*
import java.util.*

/**
 * @author taichi
 */
class SidenResponse(internal val exchange: HttpServerExchange) : Response {

    override fun status(code: Int): Response {
        this.exchange.responseCode = code
        return this
    }

    override fun header(name: String, vararg values: String): Response {
        val hm = this.exchange.responseHeaders
        hm.remove(name)
        hm.addAll(HttpString(name), Arrays.asList(*values))
        return this
    }

    override fun header(name: String, date: Long): Response {
        this.exchange.responseHeaders.put(HttpString(name),
                DateUtils.toDateString(Date(date)))
        return this
    }

    override fun headers(headers: Map<String, String>): Response {
        val hm = this.exchange.responseHeaders
        headers.forEach { hm.put(HttpString(it.key), it.value) }
        return this
    }

    override fun cookie(name: String, value: String): Cookie {
        val c = io.undertow.server.handlers.CookieImpl(name, value)
        this.exchange.setResponseCookie(c)
        return SidenCookie(c)
    }

    override fun removeCookie(name: String): Cookie? {
        val c = this.exchange.responseCookies.remove(name)
        return if (c != null) {
            SidenCookie(c)
        } else {
            null
        }
    }

    override fun type(contentType: String): Response {
        this.exchange.responseHeaders.put(Headers.CONTENT_TYPE,
                contentType)
        return this
    }

    override val raw: HttpServerExchange
        get() = this.exchange


    override fun redirect(location: String): Any {
        return this.redirect(StatusCodes.FOUND, location)
    }

    override fun redirect(code: Int, location: String): Any {
        this.exchange.responseCode = code
        this.exchange.responseHeaders.put(Headers.LOCATION, location)
        this.exchange.endExchange()
        return ExchangeState.Redirected
    }

    override fun <MODEL> render(model: MODEL, renderer: Renderer<MODEL>): Any {
        renderer.render(model, this.exchange)
        return ExchangeState.Rendered
    }

    override fun <MODEL> render(model: MODEL, template: String): Any {
        val config = this.exchange.getAttachment(Core.CONFIG)
        val repo = config.get(Config.RENDERER_REPOSITORY)?: RendererRepository.EMPTY
        return render(model, repo.find<MODEL>(template))
    }

    @SuppressWarnings("resource")
    override fun toString(): String {
        val fmt = Formatter()
        fmt.format("RESPONSE{ResponseCode:%d", this.exchange.responseCode)
        fmt.format(" ,Headers:[%s]", this.exchange.responseHeaders)
        fmt.format(" ,Cookies:[")
        this.exchange.responseCookies.forEach { fmt.format("%s", SidenCookie(it.value)) }
        return fmt.format("]}").toString()
    }
}
