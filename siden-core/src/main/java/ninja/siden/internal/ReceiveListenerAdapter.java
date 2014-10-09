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

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

import ninja.siden.WebSocket;
import ninja.siden.util.ExceptionalConsumer;

import org.xnio.Pooled;

/**
 * @author taichi
 */
public class ReceiveListenerAdapter extends AbstractReceiveListener {

	final WebSocket adaptee;

	public ReceiveListenerAdapter(WebSocket socket) {
		this.adaptee = socket;
	}

	@Override
	protected void onFullTextMessage(WebSocketChannel channel,
			BufferedTextMessage message) throws IOException {
		try {
			this.adaptee.onText(message.getData());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void onFullBinaryMessage(WebSocketChannel channel,
			BufferedBinaryMessage message) throws IOException {
		deliver(this.adaptee::onBinary, message);
	}

	void deliver(ExceptionalConsumer<ByteBuffer[], Exception> deliver,
			BufferedBinaryMessage message) throws IOException {
		Pooled<ByteBuffer[]> pooled = message.getData();
		try {
			deliver.accept(pooled.getResource());
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			pooled.free();
		}
	}

	@Override
	protected void onFullPongMessage(WebSocketChannel channel,
			BufferedBinaryMessage message) throws IOException {
		deliver(this.adaptee::onPong, message);
	}

	@Override
	protected void onFullPingMessage(WebSocketChannel channel,
			BufferedBinaryMessage message) throws IOException {
		deliver(this.adaptee::onPing, message);
	}

	@Override
	protected void onFullCloseMessage(WebSocketChannel channel,
			BufferedBinaryMessage message) throws IOException {
		deliver(this.adaptee::onClose, message);
	}
}
