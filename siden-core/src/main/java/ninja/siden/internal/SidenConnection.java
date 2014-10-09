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

import io.undertow.server.session.Session;
import io.undertow.util.Cookies;
import io.undertow.util.Headers;
import io.undertow.websockets.core.BinaryOutputStream;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketFrameType;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import ninja.siden.AttributeContainer;
import ninja.siden.Connection;
import ninja.siden.Cookie;
import ninja.siden.util.ExceptionalConsumer;
import ninja.siden.util.Io;

import org.xnio.ChannelListener;

/**
 * @author taichi
 */
public class SidenConnection implements Connection {

	final WebSocketChannel channel;
	final Optional<ninja.siden.Session> session;
	final Set<String> attrKeys = new HashSet<>();
	final Set<Connection> peers;

	final Map<String, String> params;
	final Map<String, List<String>> queries;
	final Map<String, List<String>> headers;
	final List<String> cookies;

	public SidenConnection(WebSocketHttpExchange exchange,
			WebSocketChannel channel, Set<Connection> peers) {
		this.channel = channel;
		Session sess = (Session) exchange.getSession();
		this.session = Optional.ofNullable(sess).map(WebSocketSession::new);
		this.peers = peers;
		channel.addCloseTask(new ChannelListener<WebSocketChannel>() {
			@Override
			public void handleEvent(WebSocketChannel channel) {
				SidenConnection.this.peers.remove(SidenConnection.this);
			}
		});
		peers.add(this);

		this.params = exchange.getAttachment(PathPredicate.PARAMS);
		this.queries = exchange.getRequestParameters();
		this.headers = exchange.getRequestHeaders();
		this.cookies = this.headers.get(Headers.COOKIE_STRING);
	}

	class WebSocketSession extends SidenSession {
		public WebSocketSession(io.undertow.server.session.Session sess) {
			super(null, sess);
		}

		@Override
		public void invalidate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ninja.siden.Session regenerate() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key, T newone) {
		T old = (T) this.channel.getAttribute(key);
		this.attrKeys.add(key);
		this.channel.setAttribute(key, newone);
		return Optional.ofNullable(old);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key) {
		return Optional.ofNullable((T) this.channel.getAttribute(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> remove(String key) {
		T old = (T) this.channel.getAttribute(key);
		this.attrKeys.remove(key);
		this.channel.setAttribute(key, null);
		return Optional.ofNullable(old);
	}

	@Override
	public Iterator<Attr> iterator() {
		Iterator<String> i = this.attrKeys.iterator();
		return new Iterator<AttributeContainer.Attr>() {
			@Override
			public boolean hasNext() {
				return i.hasNext();
			}

			@Override
			public AttributeContainer.Attr next() {
				String key = i.next();
				return new Attr() {
					@Override
					public String name() {
						return key;
					}

					@Override
					public <T> T remove() {
						Optional<T> t = SidenConnection.this.remove(key);
						return t.get();
					}

					@Override
					public <T> T value() {
						Optional<T> t = attr(key);
						return t.get();
					}
				};
			}
		};
	}

	class WsCb implements WebSocketCallback<Void> {
		CompletableFuture<Void> future;

		public WsCb(CompletableFuture<Void> future) {
			this.future = future;
		}

		@Override
		public void complete(WebSocketChannel channel, Void context) {
			future.complete(null);
		}

		@Override
		public void onError(WebSocketChannel channel, Void context,
				Throwable throwable) {
			future.completeExceptionally(throwable);
		}
	}

	@Override
	public CompletableFuture<Void> send(String text) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendText(text, this.channel, new WsCb(future));
		return future;
	}

	@Override
	public CompletableFuture<Void> send(ByteBuffer payload) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendBinary(payload, this.channel, new WsCb(future));
		return future;
	}

	@Override
	public CompletableFuture<Void> ping(ByteBuffer payload) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendPing(payload, this.channel, new WsCb(future));
		return future;
	}

	@Override
	public CompletableFuture<Void> pong(ByteBuffer payload) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendPong(payload, this.channel, new WsCb(future));
		return future;
	}

	@Override
	public CompletableFuture<Void> close() {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendClose(CloseMessage.NORMAL_CLOSURE, null, this.channel,
				new WsCb(future));
		return future;
	}

	@Override
	public CompletableFuture<Void> close(int code, String reason) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		WebSockets.sendClose(code, reason, this.channel, new WsCb(future));
		return future;
	}

	@Override
	public void sendStream(ExceptionalConsumer<OutputStream, Exception> fn) {
		Io.using(
				() -> new BinaryOutputStream(this.channel
						.send(WebSocketFrameType.BINARY)), os -> {
					fn.accept(os);
					return null;
				});
	}

	@Override
	public void sendWriter(ExceptionalConsumer<Writer, Exception> fn) {
		Io.using(() -> new OutputStreamWriter(new BinaryOutputStream(
				this.channel.send(WebSocketFrameType.TEXT)),
				StandardCharsets.UTF_8), os -> {
			fn.accept(os);
			return null;
		});
	}

	@Override
	public String protocolVersion() {
		return this.channel.getVersion().toHttpHeaderValue();
	}

	@Override
	public String subProtocol() {
		return this.channel.getSubProtocol();
	}

	@Override
	public boolean secure() {
		return this.channel.isSecure();
	}

	@Override
	public boolean open() {
		return this.channel.isOpen();
	}

	@Override
	public Set<Connection> peerConnections() {
		return Collections.unmodifiableSet(this.peers);
	}

	@Override
	public Optional<String> params(String key) {
		return this.params == null ? Optional.empty() : Optional
				.ofNullable(this.params.get(key));
	}

	@Override
	public Map<String, String> params() {
		Map<String, String> m = this.params;
		if (m == null) {
			return Collections.emptyMap();
		}
		return m;
	}

	@Override
	public Optional<String> query(String key) {
		return getFirst(this.queries.get(key));
	}

	Optional<String> getFirst(List<String> v) {
		if (v == null || v.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(v.get(0));
	}

	@Override
	public Optional<String> header(String name) {
		return getFirst(this.headers.get(name));
	}

	@Override
	public List<String> headers(String name) {
		return this.headers.get(name);
	}

	@Override
	public Map<String, List<String>> headers() {
		return this.headers;
	}

	Map<String, Cookie> parsedCookies = null;

	@Override
	public Map<String, Cookie> cookies() {
		if (this.parsedCookies == null) {
			if (this.cookies == null || this.cookies.isEmpty()) {
				this.parsedCookies = Collections.emptyMap();
			} else {
				Map<String, io.undertow.server.handlers.Cookie> cookies = Cookies
						.parseRequestCookies(200, false, this.cookies);
				Map<String, Cookie> newone = new HashMap<>();
				cookies.forEach((k, v) -> {
					newone.put(k, new SidenCookie(v));
				});
				this.parsedCookies = Collections.unmodifiableMap(newone);
			}
		}
		return this.parsedCookies;
	}

	@Override
	public Optional<Cookie> cookie(String name) {
		return Optional.ofNullable(cookies().get(name));
	}

	@Override
	public Optional<ninja.siden.Session> current() {
		return this.session;
	}

	@Override
	public WebSocketChannel raw() {
		return this.channel;
	}

}
