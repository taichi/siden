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
package ninja.siden

import io.undertow.websockets.core.WebSocketChannel
import java.io.OutputStream
import java.io.Writer
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author taichi
 * *
 * @see io.undertow.websockets.core.WebSocketChannel
 */
interface Connection : AttributeContainer {

    // endpoint methods

    fun send(text: String): CompletableFuture<Void>

    fun send(payload: ByteBuffer): CompletableFuture<Void>

    fun ping(payload: ByteBuffer): CompletableFuture<Void>

    fun pong(payload: ByteBuffer): CompletableFuture<Void>

    fun close(): CompletableFuture<Void>

    fun close(code: Int, reason: String): CompletableFuture<Void>

    fun sendStream(fn: (OutputStream) -> Unit)

    fun sendWriter(fn: (Writer) -> Unit)

    // informations

    val protocolVersion: String

    val subProtocol: String

    val secure: Boolean

    val open: Boolean

    val peers: Set<Connection>

    // from request

    fun params(key: String): Optional<String>

    val params: Map<String, String>

    fun query(key: String): Optional<String>

    fun header(name: String): Optional<String>

    fun headers(name: String): List<String>

    val headers: Map<String, List<String>>

    val cookies: Map<String, Cookie>

    fun cookie(name: String): Optional<Cookie>

    val current: Session

    val raw: WebSocketChannel

}
