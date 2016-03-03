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

import io.undertow.UndertowLogger
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.spi.WebSocketHttpExchange
import ninja.siden.Connection
import ninja.siden.WebSocketFactory
import org.xnio.IoUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author taichi
 */
class ConnectionCallback(internal val factory: WebSocketFactory) : WebSocketConnectionCallback {
    internal val peers = Collections.newSetFromMap(ConcurrentHashMap<Connection, Boolean>())
    override fun onConnect(exchange: WebSocketHttpExchange,
                           channel: WebSocketChannel) {
        try {
            val connection = SidenConnection(exchange, channel,
                    peers)
            val socket = factory.create(connection)
            socket.onConnect(connection)
            channel.receiveSetter.set(ReceiveListenerAdapter(socket))
            channel.resumeReceives()
        } catch (e: IOException) {
            UndertowLogger.REQUEST_IO_LOGGER.ioException(e)
            IoUtils.safeClose(channel)
        } catch (e: Exception) {
            IoUtils.safeClose(channel)
        }
    }
}
