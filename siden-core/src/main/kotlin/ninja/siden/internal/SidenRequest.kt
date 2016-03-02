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
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.util.AttachmentKey
import io.undertow.util.Sessions
import ninja.siden.*
import org.xnio.Pooled
import org.xnio.channels.Channels
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

/**
 * @author taichi
 */
class SidenRequest(internal val exchange: HttpServerExchange,
                   private val attrs: AttributeContainer = DefaultAttributeContainer()) : Request, AttributeContainer by attrs {

    override val method: HttpMethod
        get() = HttpMethod.of(exchange)

    override val path: String
        get() = this.exchange.relativePath

    override fun params(key: String): Optional<String> {
        val m = this.exchange.getAttachment(PathPredicate.PARAMS)
        return if (m == null) Optional.empty<String>() else Optional.ofNullable<String>(m[key])
    }

    override fun params(): Map<String, String> {
        val m = this.exchange.getAttachment(PathPredicate.PARAMS) ?: return emptyMap()
        return m
    }

    override fun query(key: String): Optional<String> {
        val v = this.exchange.queryParameters[key]
        return getFirst(v)
    }

    internal fun getFirst(v: Deque<String>?): Optional<String> {
        if (v == null || v.isEmpty()) {
            return Optional.empty<String>()
        }
        return Optional.of(v.first)
    }

    override fun header(name: String): Optional<String> {
        val v = this.exchange.requestHeaders.get(name)
        return getFirst(v)
    }

    override fun headers(name: String): List<String> {
        val hv = this.exchange.requestHeaders.get(name) ?: return emptyList()
        return hv
    }

    override val headers: Map<String, List<String>> by lazy {
        val newone = HashMap<String, List<String>>()
        this.exchange.requestHeaders.forEach { hv -> newone.put(hv.headerName.toString(), hv) }
        Collections.unmodifiableMap(newone)
    }

    override val cookies: Map<String, Cookie> by lazy {
        val newone = HashMap<String, Cookie>()
        this.exchange.requestCookies.forEach { newone.put(it.key, SidenCookie(it.value)) }
        Collections.unmodifiableMap(newone)
    }

    override fun cookie(name: String): Optional<Cookie> {
        return Optional.ofNullable<Cookie>(cookies[name])
    }

    override fun form(key: String): Optional<String> {
        val list = forms[key]
        if (list == null || list.isEmpty()) {
            return Optional.empty<String>()
        }
        return Optional.of(list[0])
    }

    override fun forms(key: String): List<String> {
        return forms[key] ?: emptyList<String>()
    }

    override val forms: Map<String, List<String>> by lazy {
        translateForms({ it.isFile == false }, FormData.FormValue::getValue)
    }

    internal fun <T> translateForms(pred: (FormData.FormValue) -> Boolean,
                                    translator: (FormData.FormValue) -> T): Map<String, List<T>> {
        val fd = this.exchange.getAttachment(FormDataParser.FORM_DATA) ?: return emptyMap()
        val newone = HashMap<String, List<T>>()
        fd.forEach {
            val list = fd.get(it).filter(pred).map(translator)
            newone.put(it, list)
        }
        return Collections.unmodifiableMap(newone)
    }

    override val files: Map<String, List<File>> by lazy {
        translateForms({ it.isFile }, FormData.FormValue::getFile)
    }

    override fun file(key: String): Optional<File> {
        val list = files[key]
        if (list == null || list.isEmpty()) {
            return Optional.empty<File>()
        }
        return Optional.of(list[0])
    }

    override fun files(key: String): List<File> {
        return files[key] ?: emptyList<File>()
    }

    override fun body(): Optional<String> {
        val existing = exchange.getAttachment(FormDataParser.FORM_DATA)
        if (existing != null) {
            return Optional.ofNullable(existing.iterator().asSequence().first())
        }
        val parsedBody = exchange.getAttachment(STRING_BODY)
        if (parsedBody != null) {
            return Optional.of(parsedBody)
        }

        var length = exchange.requestContentLength
        if (length < 1 || exchange.isRequestChannelAvailable == false) {
            return Optional.empty<String>()
        }

        return exchange.connection.bufferPool.allocate().use { pooled: Pooled<ByteBuffer> ->
            val charset = parseCharset(exchange)
            val decoder = charset.newDecoder()
            val builder = StringBuilder()
            val buffer = pooled.resource
            val channel = exchange.requestChannel
            do {
                buffer.clear()
                val read = Channels.readBlocking(channel, buffer)
                if (0 < buffer.position()) {
                    buffer.flip()
                    builder.append(decoder.decode(buffer))
                }
                if (read < 1) {
                    break
                }
                length -= read.toLong()
            } while (0 < length)

            val s = String(builder)
            this.exchange.putAttachment(STRING_BODY, s)
            Optional.of(s)
        }
    }

    override val session: Session
        get() = SidenSession(this.exchange, Sessions.getOrCreateSession(this.exchange))

    override val current: Session?
        get() {
            val s = Sessions.getSession(this.exchange)
            if (s != null) {
                return SidenSession(exchange, s)
            }
            return null
        }

    override val xhr: Boolean
        get() = this.exchange.requestHeaders.contains(SecurityHeaders.REQUESTED_WITH)

    override val protocol: String
        get() = this.exchange.protocol.toString()

    override val scheme: String
        get() = this.exchange.requestScheme

    override val raw: HttpServerExchange
        get() = this.exchange

    @SuppressWarnings("resource")
    override fun toString(): String {
        return "REQUEST{$method $path ,Headers:[$headers], ,Cookies:[$cookies],Forms:[$forms]}"
    }

    companion object {

        internal fun parseCharset(exchange: HttpServerExchange): Charset {
            // TODO support force charset
            val config = exchange.getAttachment(Core.CONFIG)
            val defaultCs = config.get(Config.CHARSET)
            val cs = exchange.requestCharset
            if (defaultCs.displayName().equals(cs, ignoreCase = true) == false && cs != null
                    && Charset.isSupported(cs)) {
                return Charset.forName(cs)
            }
            return defaultCs
        }

        internal val STRING_BODY = AttachmentKey.create(String::class.java)

        internal val SESSION = AttachmentKey.create(Session::class.java)
    }
}
