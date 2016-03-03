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

import java.nio.ByteBuffer

/**
 * @author taichi
 */
interface WebSocket {

    open fun onConnect(connection: Connection) {
    }

    open fun onText(payload: String) {
    }

    open fun onBinary(payload: Array<ByteBuffer>) {
    }

    open fun onPong(payload: Array<ByteBuffer>) {
    }

    open fun onPing(payload: Array<ByteBuffer>) {
    }

    open fun onClose(payload: Array<ByteBuffer>) {
    }
}
