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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Sessions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.Deque;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ninja.siden.AttributeContainer;
import ninja.siden.Config;
import ninja.siden.Cookie;
import ninja.siden.HttpMethod;
import ninja.siden.Request;
import ninja.siden.SecurityHeaders;
import ninja.siden.Session;
import ninja.siden.util.Predicates;

import org.xnio.OptionMap;
import org.xnio.Pooled;
import org.xnio.channels.Channels;
import org.xnio.channels.StreamSourceChannel;

/**
 * @author taichi
 */
public class SidenRequest implements Request {

	final HttpServerExchange exchange;
	final Map<String, Object> attrs = new HashMap<>();

	public SidenRequest(HttpServerExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.of(exchange);
	}

	@Override
	public String path() {
		return this.exchange.getRelativePath();
	}

	@Override
	public Optional<String> params(String key) {
		Map<String, String> m = this.exchange
				.getAttachment(PathPredicate.PARAMS);
		return m == null ? Optional.empty() : Optional.ofNullable(m.get(key));
	}

	@Override
	public Map<String, String> params() {
		Map<String, String> m = this.exchange
				.getAttachment(PathPredicate.PARAMS);
		if (m == null) {
			return Collections.emptyMap();
		}
		return m;
	}

	@Override
	public Optional<String> query(String key) {
		Deque<String> v = this.exchange.getQueryParameters().get(key);
		return getFirst(v);
	}

	Optional<String> getFirst(Deque<String> v) {
		if (v == null || v.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(v.getFirst());
	}

	@Override
	public Optional<String> header(String key) {
		HeaderValues v = this.exchange.getRequestHeaders().get(key);
		return getFirst(v);
	}

	@Override
	public List<String> headers(String key) {
		HeaderValues hv = this.exchange.getRequestHeaders().get(key);
		if (hv == null) {
			return Collections.emptyList();
		}
		return hv;
	}

	Map<String, List<String>> headers = null;

	Map<String, List<String>> translateHeaders() {
		if (this.headers == null) {
			Map<String, List<String>> newone = new HashMap<>();
			this.exchange.getRequestHeaders().forEach(
					hv -> newone.put(hv.getHeaderName().toString(), hv));
			this.headers = Collections.unmodifiableMap(newone);
		}
		return this.headers;
	}

	@Override
	public Map<String, List<String>> headers() {
		return translateHeaders();
	}

	Map<String, Cookie> cookies = null;

	Map<String, Cookie> translateCookies() {
		if (this.cookies == null) {
			Map<String, Cookie> newone = new HashMap<>();
			this.exchange.getRequestCookies().forEach((k, v) -> {
				newone.put(k, new SidenCookie(v));
			});
			this.cookies = Collections.unmodifiableMap(newone);
		}
		return this.cookies;
	}

	@Override
	public Map<String, Cookie> cookies() {
		return translateCookies();
	}

	@Override
	public Optional<Cookie> cookie(String key) {
		Cookie c = translateCookies().get(key);
		return Optional.ofNullable(c);
	}

	@Override
	public Optional<String> form(String key) {
		List<String> list = translateForms().get(key);
		if (list == null || list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(0));
	}

	@Override
	public List<String> forms(String key) {
		return translateForms().getOrDefault(key, Collections.emptyList());
	}

	Map<String, List<String>> forms = null;

	Map<String, List<String>> translateForms() {
		if (this.forms == null) {
			this.forms = translateForms(
					Predicates.not(FormData.FormValue::isFile),
					FormData.FormValue::getValue);
		}
		return this.forms;
	}

	@Override
	public Map<String, List<String>> forms() {
		return translateForms();
	}

	<T> Map<String, List<T>> translateForms(Predicate<FormData.FormValue> pred,
			Function<FormData.FormValue, T> translator) {
		FormData fd = this.exchange.getAttachment(FormDataParser.FORM_DATA);
		if (fd == null) {
			return Collections.emptyMap();
		}
		Map<String, List<T>> newone = new HashMap<>();
		fd.forEach(k -> {
			List<T> list = fd.get(k).stream().filter(pred).map(translator)
					.collect(Collectors.toList());
			newone.put(k, list);
		});
		return Collections.unmodifiableMap(newone);
	}

	Map<String, List<File>> files = null;

	Map<String, List<File>> translateFiles() {
		if (this.files == null) {
			this.files = translateForms(FormData.FormValue::isFile,
					FormData.FormValue::getFile);
		}
		return this.files;
	}

	@Override
	public Optional<File> file(String key) {
		List<File> list = translateFiles().get(key);
		if (list == null || list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(0));
	}

	@Override
	public List<File> files(String key) {
		return translateFiles().getOrDefault(key, Collections.emptyList());
	}

	@Override
	public Map<String, List<File>> files() {
		return translateFiles();
	}

	static Charset parseCharset(HttpServerExchange exchange) {
		// TODO support force charset
		OptionMap config = exchange.getAttachment(Core.CONFIG);
		Charset defaultCs = config.get(Config.CHARSET);
		String cs = exchange.getRequestCharset();
		if (defaultCs.displayName().equalsIgnoreCase(cs) == false && cs != null
				&& Charset.isSupported(cs)) {
			return Charset.forName(cs);
		}
		return defaultCs;
	}

	@Override
	public String body() {
		long length = exchange.getRequestContentLength();
		if (length < 1) {
			return "";
		}
		FormData existing = exchange.getAttachment(FormDataParser.FORM_DATA);
		if (existing != null) {
			return "";
		}
		StreamSourceChannel channel = exchange.getRequestChannel();
		if (channel == null) {
			return "";
		}
		try (Pooled<ByteBuffer> pooled = exchange.getConnection()
				.getBufferPool().allocate()) {
			Charset charset = parseCharset(exchange);
			CharsetDecoder decoder = charset.newDecoder();
			StringBuilder builder = new StringBuilder();
			final ByteBuffer buffer = pooled.getResource();
			int read = 0;
			do {
				buffer.clear();
				read = Channels.readBlocking(channel, buffer);
				if (0 < buffer.position()) {
					buffer.flip();
					builder.append(decoder.decode(buffer));
				}
				if (read < 1) {
					break;
				}
				length -= read;
			} while (0 < length);
			return new String(builder);
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	static final AttachmentKey<Session> SESSION = AttachmentKey
			.create(Session.class);

	@Override
	public Session session() {
		return new SidenSession(this.exchange,
				Sessions.getOrCreateSession(this.exchange));
	}

	@Override
	public Optional<Session> current() {
		return Optional.ofNullable(Sessions.getSession(this.exchange)).map(
				s -> new SidenSession(exchange, s));
	}

	@Override
	public boolean xhr() {
		return this.exchange.getRequestHeaders().contains(
				SecurityHeaders.REQUESTED_WITH);
	}

	@Override
	public String protocol() {
		return this.exchange.getProtocol().toString();
	}

	@Override
	public String scheme() {
		return this.exchange.getRequestScheme();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key) {
		return Optional.ofNullable((T) this.attrs.get(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key, T newone) {
		return Optional.ofNullable((T) this.attrs.put(key, newone));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<Attr> iterator() {
		Iterator<Map.Entry<String, Object>> itr = this.attrs.entrySet()
				.iterator();
		return new Iterator<AttributeContainer.Attr>() {
			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public AttributeContainer.Attr next() {
				Map.Entry<String, Object> ent = itr.next();
				return new AttributeContainer.Attr() {
					@Override
					public String name() {
						return ent.getKey();
					}

					@Override
					public <T> T value() {
						return (T) ent.getValue();
					}

					@Override
					public <T> T remove() {
						itr.remove();
						return (T) ent.getValue();
					}
				};
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> remove(String key) {
		return Optional.ofNullable((T) this.attrs.remove(key));
	}

	@Override
	public HttpServerExchange raw() {
		return this.exchange;
	}

	@Override
	@SuppressWarnings("resource")
	public String toString() {
		Formatter fmt = new Formatter();
		fmt.format("REQUEST{%s %s", method(), this.exchange.getRequestPath());
		fmt.format(" ,Headers:[%s]", headers());
		fmt.format(" ,Cookies:[%s]", cookies());
		fmt.format(" ,Forms:[%s]}", forms());

		return fmt.toString();
	}
}
