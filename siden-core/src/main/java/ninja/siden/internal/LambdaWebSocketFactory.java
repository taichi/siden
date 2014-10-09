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

	List<ExceptionalConsumer<Connection, Exception>> conn = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, String, Exception>> txt = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> bin = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> pong = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> ping = new ArrayList<>();
	List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> close = new ArrayList<>();

	@Override
	public WebSocket create(Connection connection) {
		return new LambdaWebSocket();
	}

	class LambdaWebSocket implements WebSocket {
		Connection connection;

		List<ExceptionalConsumer<Connection, Exception>> conn = new ArrayList<>(
				LambdaWebSocketFactory.this.conn);
		List<ExceptionalBiConsumer<Connection, String, Exception>> txt = new ArrayList<>(
				LambdaWebSocketFactory.this.txt);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> bin = new ArrayList<>(
				LambdaWebSocketFactory.this.bin);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> pong = new ArrayList<>(
				LambdaWebSocketFactory.this.pong);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> ping = new ArrayList<>(
				LambdaWebSocketFactory.this.ping);
		List<ExceptionalBiConsumer<Connection, ByteBuffer[], Exception>> close = new ArrayList<>(
				LambdaWebSocketFactory.this.close);

		@Override
		public void onConnect(Connection connection) throws Exception {
			this.connection = connection;
			for (ExceptionalConsumer<Connection, Exception> fn : this.conn) {
				fn.accept(this.connection);
			}
		}

		<T> void forEach(T payload,
				List<ExceptionalBiConsumer<Connection, T, Exception>> list)
				throws Exception {
			for (ExceptionalBiConsumer<Connection, T, Exception> fn : list) {
				fn.accept(this.connection, payload);
			}
		}

		@Override
		public void onText(String payload) throws Exception {
			forEach(payload, this.txt);
		}

		@Override
		public void onBinary(ByteBuffer[] payload) throws Exception {
			forEach(payload, this.bin);
		}

		@Override
		public void onPong(ByteBuffer[] payload) throws Exception {
			forEach(payload, this.pong);
		}

		@Override
		public void onPing(ByteBuffer[] payload) throws Exception {
			forEach(payload, this.ping);
		}

		@Override
		public void onClose(ByteBuffer[] payload) throws Exception {
			forEach(payload, this.close);
		}
	}

	@Override
	public WebSocketCustomizer onConnect(
			ExceptionalConsumer<Connection, Exception> fn) {
		this.conn.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onText(
			ExceptionalBiConsumer<Connection, String, Exception> fn) {
		this.txt.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onBinary(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn) {
		this.bin.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onPong(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn) {
		this.pong.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onPing(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn) {
		this.ping.add(fn);
		return this;
	}

	@Override
	public WebSocketCustomizer onClose(
			ExceptionalBiConsumer<Connection, ByteBuffer[], Exception> fn) {
		this.close.add(fn);
		return this;
	}
}
