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
import io.undertow.server.session.SessionConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Optional;

import ninja.siden.AttributeContainer;
import ninja.siden.Session;

/**
 * @author taichi
 */
public class SidenSession implements Session {

	final HttpServerExchange exchange;
	final io.undertow.server.session.Session delegate;

	public SidenSession(HttpServerExchange exchange,
			io.undertow.server.session.Session delegate) {
		this.exchange = exchange;
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key, T newone) {
		return Optional.ofNullable((T) this.delegate.setAttribute(key, newone));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> attr(String key) {
		return Optional.ofNullable((T) this.delegate.getAttribute(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> remove(String key) {
		return Optional.ofNullable((T) this.delegate.removeAttribute(key));
	}

	@Override
	public Iterator<Attr> iterator() {
		Iterator<String> names = this.delegate.getAttributeNames().iterator();
		return new Iterator<AttributeContainer.Attr>() {
			@Override
			public boolean hasNext() {
				return names.hasNext();
			}

			@Override
			public AttributeContainer.Attr next() {
				String name = names.next();
				return new AttributeContainer.Attr() {

					@Override
					public <T> T value() {
						Optional<T> o = SidenSession.this.attr(name);
						return o.get();
					}

					@Override
					public <T> T remove() {
						Optional<T> o = SidenSession.this.remove(name);
						return o.get();
					}

					@Override
					public String name() {
						return name;
					}
				};
			}
		};
	}

	@Override
	public String id() {
		return this.delegate.getId();
	}

	@Override
	public void invalidate() {
		this.delegate.invalidate(this.exchange);
	}

	@Override
	public Session regenerate() {
		SessionConfig config = this.exchange
				.getAttachment(SessionConfig.ATTACHMENT_KEY);
		this.delegate.changeSessionId(this.exchange, config);
		return this;
	}

	@Override
	public io.undertow.server.session.Session raw() {
		return this.delegate;
	}

	@SuppressWarnings("resource")
	@Override
	public String toString() {
		Formatter fmt = new Formatter();
		fmt.format("Session{ id:%s", id());
		SimpleDateFormat sdf = new SimpleDateFormat();
		fmt.format(",CreatationTime:%s",
				sdf.format(new Date(raw().getCreationTime())));
		fmt.format(",LastAccessedTime:%s",
				sdf.format(new Date(raw().getLastAccessedTime())));
		fmt.format(",values:[");
		forEach(a -> fmt.format(" %s=%s", a.name(), a.value()));
		fmt.format("]}");

		return fmt.toString();
	}
}
