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

import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedBinaryMessage
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import ninja.siden.WebSocket
import java.io.IOException
import java.nio.ByteBuffer

/**
 * @author taichi
 */
class ReceiveListenerAdapter(internal val adaptee: WebSocket) : AbstractReceiveListener() {

    override fun onFullTextMessage(channel: WebSocketChannel?, message: BufferedTextMessage) {
        this.adaptee.onText(message.data)
    }

    override fun onFullBinaryMessage(channel: WebSocketChannel?, message: BufferedBinaryMessage) {
        deliver({ this.adaptee.onBinary(it) }, message)
    }

    internal fun deliver(deliver: (Array<ByteBuffer>) -> Unit, message: BufferedBinaryMessage) {
        val pooled = message.data
        try {
            deliver(pooled.resource)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        } finally {
            pooled.free()
        }
    }

    override fun onFullPongMessage(channel: WebSocketChannel?, message: BufferedBinaryMessage) {
        deliver({ this.adaptee.onPong(it) }, message)
    }

    override fun onFullPingMessage(channel: WebSocketChannel, message: BufferedBinaryMessage) {
        deliver({ this.adaptee.onPing(it) }, message)
    }

    override fun onFullCloseMessage(channel: WebSocketChannel,
                                    message: BufferedBinaryMessage) {
        deliver({ this.adaptee.onClose(it) }, message)
    }
}
