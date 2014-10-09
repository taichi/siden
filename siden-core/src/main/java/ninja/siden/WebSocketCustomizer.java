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
package ninja.siden;

import java.nio.ByteBuffer;

import ninja.siden.util.ExceptionalBiConsumer;
import ninja.siden.util.ExceptionalConsumer;

/**
 * @author taichi
 */
public interface WebSocketCustomizer {

	WebSocketCustomizer onConnect(ExceptionalConsumer<Connection, Exception> fn);

	WebSocketCustomizer onText(
			ExceptionalBiConsumer<Connection, String, Exception> fn);

	WebSocketCustomizer onBinary(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn);

	WebSocketCustomizer onPong(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn);

	WebSocketCustomizer onPing(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn);

	WebSocketCustomizer onClose(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn);
}
