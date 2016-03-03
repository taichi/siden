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

import ninja.siden.Connection
import ninja.siden.WebSocket
import ninja.siden.WebSocketCustomizer
import ninja.siden.WebSocketFactory
import java.nio.ByteBuffer
import java.util.*

/**
 * @author taichi
 */
class LambdaWebSocketFactory : WebSocketFactory, WebSocketCustomizer {

    internal fun <T> makeList(): MutableList<(Connection, T) -> Unit> = ArrayList()

    internal var connFns = ArrayList<(Connection) -> Unit>()
    internal var txtFns = makeList<String>()
    internal var binFns = makeList<Array<ByteBuffer>>()
    internal var pongFns = makeList<Array<ByteBuffer>>()
    internal var pingFns = makeList<Array<ByteBuffer>>()
    internal var closeFns = makeList<Array<ByteBuffer>>()

    override fun create(connection: Connection): WebSocket {
        return LambdaWebSocket()
    }

    internal inner class LambdaWebSocket : WebSocket {
        var connection: Connection? = null

        internal fun <T> makeList(src: List<(Connection, T) -> Unit>): MutableList<(Connection, T) -> Unit> = ArrayList(src)

        val conn = ArrayList<(Connection) -> Unit>(this@LambdaWebSocketFactory.connFns)
        val txt = makeList(this@LambdaWebSocketFactory.txtFns)
        val bin = makeList(this@LambdaWebSocketFactory.binFns)
        val pong = makeList(this@LambdaWebSocketFactory.pongFns)
        val ping = makeList(this@LambdaWebSocketFactory.pingFns)
        val close = makeList(this@LambdaWebSocketFactory.closeFns)

        override fun onConnect(connection: Connection) {
            this.connection = connection
            this.conn.forEach { it(connection) }
        }

        fun <T> forEach(payload: T, list: List<(Connection, T) -> Unit>) {
            requireNotNull(this.connection)
            val c = this.connection
            if (c != null) {
                list.forEach { it(c, payload) }
            }
        }

        override fun onText(payload: String) = forEach(payload, this.txt)

        override fun onBinary(payload: Array<ByteBuffer>) = forEach(payload, this.bin)

        override fun onPong(payload: Array<ByteBuffer>) = forEach(payload, this.pong)

        override fun onPing(payload: Array<ByteBuffer>) = forEach(payload, this.ping)

        override fun onClose(payload: Array<ByteBuffer>) = forEach(payload, this.close)
    }

    override fun onConnect(fn: (Connection) -> Unit): WebSocketCustomizer {
        this.connFns.add(fn)
        return this
    }

    override fun onText(fn: (Connection, String) -> Unit): WebSocketCustomizer {
        this.txtFns.add(fn)
        return this
    }

    override fun onBinary(fn: (Connection, Array<ByteBuffer>) -> Unit): WebSocketCustomizer {
        this.binFns.add(fn)
        return this
    }

    override fun onPong(fn: (Connection, Array<ByteBuffer>) -> Unit): WebSocketCustomizer {
        this.pongFns.add(fn)
        return this
    }

    override fun onPing(fn: (Connection, Array<ByteBuffer>) -> Unit): WebSocketCustomizer {
        this.pingFns.add(fn)
        return this
    }

    override fun onClose(fn: (Connection, Array<ByteBuffer>) -> Unit): WebSocketCustomizer {
        this.closeFns.add(fn)
        return this
    }
}
