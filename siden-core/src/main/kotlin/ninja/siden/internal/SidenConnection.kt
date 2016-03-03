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

import io.undertow.util.Cookies
import io.undertow.util.Headers
import io.undertow.websockets.core.*
import io.undertow.websockets.spi.WebSocketHttpExchange
import ninja.siden.AttributeContainer
import ninja.siden.Connection
import ninja.siden.Cookie
import ninja.siden.util.Using
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author taichi
 */
class SidenConnection(private val exchange: WebSocketHttpExchange,
                      internal val channel: WebSocketChannel,
                      override val peers: MutableSet<Connection>,
                      private val attrs: AttributeContainer = DefaultAttributeContainer()) : Connection, AttributeContainer by attrs {
    override val current: ninja.siden.Session?
        get() {
            var sess = exchange.session as? io.undertow.server.session.Session
            if (sess != null) {
                return WebSocketSession(sess)
            }
            return null
        }

    override val params: Map<String, String> by lazy {
        exchange.getAttachment(PathPredicate.PARAMS) ?: emptyMap()
    }
    internal val queries: Map<String, List<String>> = exchange.requestParameters
    override val headers: Map<String, List<String>> = exchange.requestHeaders

    override val cookies: Map<String, Cookie> by lazy {
        val cs = this.headers[Headers.COOKIE_STRING]
        val cookies = Cookies.parseRequestCookies(200, false, cs)
        val newone = HashMap<String, Cookie>()
        cookies.forEach { newone.put(it.key, SidenCookie(it.value)) }
        Collections.unmodifiableMap(newone)
    }

    init {
        channel.addCloseTask { this@SidenConnection.peers.remove(this@SidenConnection) }
        peers.add(this)
    }

    internal inner class WebSocketSession(sess: io.undertow.server.session.Session) : SidenSession(null, sess) {

        override fun invalidate() {
            throw UnsupportedOperationException()
        }

        override fun regenerate(): ninja.siden.Session {
            throw UnsupportedOperationException()
        }
    }

    internal inner class WsCb(var future: CompletableFuture<Void>) : WebSocketCallback<Void> {

        override fun complete(channel: WebSocketChannel, context: Void?) {
            future.complete(null)
        }

        override fun onError(channel: WebSocketChannel, context: Void?, throwable: Throwable) {
            future.completeExceptionally(throwable)
        }
    }

    override fun send(text: String): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendText(text, this.channel, WsCb(future))
        return future
    }

    override fun send(payload: ByteBuffer): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendBinary(payload, this.channel, WsCb(future))
        return future
    }

    override fun ping(payload: ByteBuffer): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendPing(payload, this.channel, WsCb(future))
        return future
    }

    override fun pong(payload: ByteBuffer): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendPong(payload, this.channel, WsCb(future))
        return future
    }

    override fun close(): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendClose(CloseMessage.NORMAL_CLOSURE, null, this.channel,
                WsCb(future))
        return future
    }

    override fun close(code: Int, reason: String): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        WebSockets.sendClose(code, reason, this.channel, WsCb(future))
        return future
    }

    override fun sendStream(fn: (OutputStream) -> Unit) {
        Using.consume({ BinaryOutputStream(this.channel.send(WebSocketFrameType.BINARY)) }, fn)
    }

    override fun sendWriter(fn: (Writer) -> Unit) {
        Using.consume({
            OutputStreamWriter(BinaryOutputStream(
                    this.channel.send(WebSocketFrameType.TEXT)),
                    StandardCharsets.UTF_8)
        }, fn)
    }

    override val protocolVersion: String
        get() = this.channel.version.toHttpHeaderValue()


    override val subProtocol: String
        get() = this.channel.subProtocol

    override val secure: Boolean
        get() = this.channel.isSecure

    override val open: Boolean
        get() = this.channel.isOpen

    override fun params(key: String): Optional<String> {
        return Optional.ofNullable<String>(this.params[key])
    }

    override fun query(key: String): Optional<String> {
        return getFirst(this.queries[key])
    }

    internal fun getFirst(v: List<String>?): Optional<String> {
        if (v == null || v.isEmpty()) {
            return Optional.empty<String>()
        }
        return Optional.of(v[0])
    }

    override fun header(name: String): Optional<String> {
        return getFirst(this.headers[name])
    }

    override fun headers(name: String): List<String> {
        return this.headers[name] ?: emptyList()
    }

    override fun cookie(name: String): Optional<Cookie> {
        return Optional.ofNullable<Cookie>(cookies[name])
    }

    override val raw: WebSocketChannel
        get() = this.channel
}
