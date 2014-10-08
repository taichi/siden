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
package ninja.siden.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ninja.siden.Connection;
import ninja.siden.WebSocket;
import ninja.siden.WebSocketCustomizer;
import ninja.siden.WebSocketFactory;
import ninja.siden.util.ExceptionalBiConsumer;
import ninja.siden.util.ExceptionalConsumer;

/**
 * @author taichi
 */
public class LambdaWebSocketFactory implements WebSocketFactory,
		WebSocketCustomizer {

	List<ExceptionalConsumer<Connection, IOException>> conn = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, String, IOException>> txt = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> bin = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> pong = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> ping = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> close = new ArrayList<>();

	@Override
	public WebSocket create(Connection connection) {
		return new LambdaWebSocket();
	}

	class LambdaWebSocket implements WebSocket {
		Connection connection;

		List<ExceptionalConsumer<Connection, IOException>> conn = new ArrayList<>(
				LambdaWebSocketFactory.this.conn);
		List<ExceptionalBiConsumer<Connection, String, IOException>> txt = new ArrayList<>(
				LambdaWebSocketFactory.this.txt);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> bin = new ArrayList<>(
				LambdaWebSocketFactory.this.bin);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> pong = new ArrayList<>(
				LambdaWebSocketFactory.this.pong);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> ping = new ArrayList<>(
				LambdaWebSocketFactory.this.ping);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], IOException>> close = new ArrayList<>(
				LambdaWebSocketFactory.this.close);

		@Override
		public void onConnect(Connection connection) throws IOException {
			this.connection = connection;
			for (ExceptionalConsumer<Connection, IOException> fn : this.conn) {
				fn.accept(this.connection);
			}
		}

		<T> void forEach(T payload,
				List<ExceptionalBiConsumer<Connection, T, IOException>> list)
				throws IOException {
			for (ExceptionalBiConsumer<Connection, T, IOException> fn : list) {
				fn.accept(this.connection, payload);
			}
		}

		@Override
		public void onText(String payload) throws IOException {
			forEach(payload, this.txt);
		}

		@Override
		public void onBinary(ByteBuffer[] payload) throws IOException {
			forEach(payload, this.bin);
		}

		@Override
		public void onPong(ByteBuffer[] payload) throws IOException {
			forEach(payload, this.pong);
		}

		@Override
		public void onPing(ByteBuffer[] payload) throws IOException {
			forEach(payload, this.ping);
		}

		@Override
		public void onClose(ByteBuffer[] payload) throws IOException {
			forEach(payload, this.close);
		}
	}

	@Override
	public WebSocketCustomizer onConnect(
			ExceptionalConsumer<Connection, IOException> fn) {
		return this;
	}

	@Override
	public WebSocketCustomizer onText(
			ExceptionalBiConsumer<Connection, String, IOException> fn) {
		this.txt.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onBinary(
			ExceptionalBiConsumer<Connection, ByteBuffer[], IOException> fn) {
		this.bin.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onPong(
			ExceptionalBiConsumer<Connection, ByteBuffer[], IOException> fn) {
		this.pong.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onPing(
			ExceptionalBiConsumer<Connection, ByteBuffer[], IOException> fn) {
		this.ping.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onClose(
			ExceptionalBiConsumer<Connection, ByteBuffer[], IOException> fn) {
		this.close.add(fn);
		return this;
	}
}
