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
package ninja.siden.jmx;

import java.nio.ByteBuffer;

import ninja.siden.Connection;
import ninja.siden.WebSocket;
import ninja.siden.WebSocketFactory;

/**
 * @author taichi
 */
public class WebSocketTracker implements WebSocketFactory, WebSocketMXBean {

	final WebSocketFactory original;

	RequestMeter onConnect = new RequestMeter();
	RequestMeter onText = new RequestMeter();
	RequestMeter onBinary = new RequestMeter();
	RequestMeter onPong = new RequestMeter();
	RequestMeter onPing = new RequestMeter();
	RequestMeter onClose = new RequestMeter();

	public WebSocketTracker(WebSocketFactory original) {
		this.original = original;
	}

	@Override
	public WebSocket create(Connection connection) {
		return new WsWrapper(this.original.create(connection));
	}

	class WsWrapper implements WebSocket {
		final WebSocket original;

		public WsWrapper(WebSocket original) {
			this.original = original;
		}

		@Override
		public void onConnect(Connection connection) throws Exception {
			onConnect.accept(m -> original.onConnect(connection));
		}

		@Override
		public void onText(String payload) throws Exception {
			onText.accept(m -> original.onText(payload));
		}

		@Override
		public void onBinary(ByteBuffer[] payload) throws Exception {
			onBinary.accept(m -> original.onBinary(payload));
		}

		@Override
		public void onPong(ByteBuffer[] payload) throws Exception {
			onPong.accept(m -> original.onPong(payload));
		}

		@Override
		public void onPing(ByteBuffer[] payload) throws Exception {
			onPing.accept(m -> original.onPing(payload));
		}

		@Override
		public void onClose(ByteBuffer[] payload) throws Exception {
			onClose.accept(m -> original.onClose(payload));
		}
	}

	@Override
	public void reset() {
		this.onConnect.reset();
		this.onText.reset();
		this.onBinary.reset();
		this.onPong.reset();
		this.onPing.reset();
		this.onClose.reset();
	}

	@Override
	public RequestMetrics getOnConnect() {
		return onConnect.toMetrics();
	}

	@Override
	public RequestMetrics getOnText() {
		return onText.toMetrics();
	}

	@Override
	public RequestMetrics getOnBinary() {
		return onBinary.toMetrics();
	}

	@Override
	public RequestMetrics getOnPong() {
		return onPong.toMetrics();
	}

	@Override
	public RequestMetrics getOnPing() {
		return onPing.toMetrics();
	}

	@Override
	public RequestMetrics getOnClose() {
		return onClose.toMetrics();
	}
}
