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

import io.undertow.UndertowLogger;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ninja.siden.Connection;
import ninja.siden.WebSocket;
import ninja.siden.WebSocketFactory;

import org.xnio.IoUtils;

/**
 * @author taichi
 */
public class ConnectionCallback implements WebSocketConnectionCallback {

	final WebSocketFactory factory;
	final Set<Connection> peers = Collections
			.newSetFromMap(new ConcurrentHashMap<>());

	public ConnectionCallback(WebSocketFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onConnect(WebSocketHttpExchange exchange,
			WebSocketChannel channel) {
		try {
			Connection connection = new SidenConnection(exchange, channel,
					peers);
			WebSocket socket = factory.create(connection);
			socket.onConnect(connection);
			channel.getReceiveSetter().set(new ReceiveListenerAdapter(socket));
			channel.resumeReceives();
		} catch (IOException e) {
			UndertowLogger.REQUEST_IO_LOGGER.ioException(e);
			IoUtils.safeClose(channel);
		} catch (Exception e) {
			IoUtils.safeClose(channel);
		}
	}
}
