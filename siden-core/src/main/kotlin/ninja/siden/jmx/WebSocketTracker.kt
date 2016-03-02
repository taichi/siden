/*
 * Copyright 2015 SATO taichi
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
package ninja.siden.jmx

import ninja.siden.Connection
import ninja.siden.WebSocket
import ninja.siden.WebSocketFactory

import java.nio.ByteBuffer

/**
 * @author taichi
 */
class WebSocketTracker(internal val original: WebSocketFactory) : WebSocketFactory, WebSocketMXBean {

    internal var _onConnect = RequestMeter()
    internal var _onText = RequestMeter()
    internal var _onBinary = RequestMeter()
    internal var _onPong = RequestMeter()
    internal var _onPing = RequestMeter()
    internal var _onClose = RequestMeter()

    override fun create(connection: Connection): WebSocket {
        return WsWrapper(this.original.create(connection))
    }

    internal inner class WsWrapper(val original: WebSocket) : WebSocket {
        override fun onConnect(connection: Connection) = _onConnect.record { original.onConnect(connection) }
        override fun onText(payload: String) = _onText.record { original.onText(payload) }
        override fun onBinary(payload: Array<ByteBuffer>) = _onBinary.record { original.onBinary(payload) }
        override fun onPong(payload: Array<ByteBuffer>) = _onPong.record { original.onPong(payload) }
        override fun onPing(payload: Array<ByteBuffer>) = _onPing.record { original.onPing(payload) }
        override fun onClose(payload: Array<ByteBuffer>) = _onClose.record { original.onClose(payload) }
    }

    override fun reset() {
        this._onConnect.reset()
        this._onText.reset()
        this._onBinary.reset()
        this._onPong.reset()
        this._onPing.reset()
        this._onClose.reset()
    }

    override val onConnect: RequestMetrics
        get() = _onConnect.toMetrics()
    override val onText: RequestMetrics
        get() = _onText.toMetrics()
    override val onBinary: RequestMetrics
        get() = _onBinary.toMetrics()
    override val onPong: RequestMetrics
        get() = _onPong.toMetrics()
    override val onPing: RequestMetrics
        get() = _onPing.toMetrics()
    override val onClose: RequestMetrics
        get() = _onClose.toMetrics()
}
