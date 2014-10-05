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

import java.util.Date;
import java.util.Formatter;

import ninja.siden.Cookie;

/**
 * @author taichi
 */
public class SidenCookie implements Cookie {

	final io.undertow.server.handlers.Cookie delegate;

	SidenCookie(io.undertow.server.handlers.Cookie delegate) {
		this.delegate = delegate;
	}

	@Override
	public String name() {
		return this.delegate.getName();
	}

	@Override
	public String value() {
		return this.delegate.getValue();
	}

	@Override
	public Cookie value(String value) {
		this.delegate.setValue(value);
		return this;
	}

	@Override
	public String path() {
		return this.delegate.getPath();
	}

	@Override
	public Cookie path(String path) {
		this.delegate.setPath(path);
		return this;
	}

	@Override
	public String domain() {
		return this.delegate.getDomain();
	}

	@Override
	public Cookie domain(String domain) {
		this.delegate.setDomain(domain);
		return this;
	}

	@Override
	public Integer maxAge() {
		return this.delegate.getMaxAge();
	}

	@Override
	public Cookie maxAge(Integer maxAge) {
		this.delegate.setMaxAge(maxAge);
		return this;
	}

	@Override
	public boolean discard() {
		return this.delegate.isDiscard();
	}

	@Override
	public Cookie discard(boolean discard) {
		this.delegate.setDiscard(discard);
		return this;
	}

	@Override
	public boolean secure() {
		return this.delegate.isSecure();
	}

	@Override
	public Cookie secure(boolean secure) {
		this.delegate.setSecure(secure);
		return this;
	}

	@Override
	public int version() {
		return this.delegate.getVersion();
	}

	@Override
	public Cookie version(int version) {
		this.delegate.setVersion(version);
		return this;
	}

	@Override
	public boolean httpOnly() {
		return this.delegate.isHttpOnly();
	}

	@Override
	public Cookie httpOnly(boolean httpOnly) {
		this.delegate.setHttpOnly(httpOnly);
		return this;
	}

	@Override
	public Date expires() {
		return this.delegate.getExpires();
	}

	@Override
	public Cookie expires(Date expires) {
		this.delegate.setExpires(expires);
		return this;
	}

	@Override
	public String comment() {
		return this.delegate.getComment();
	}

	@Override
	public Cookie comment(String comment) {
		this.delegate.setComment(comment);
		return this;
	}

	@Override
	@SuppressWarnings("resource")
	public String toString() {
		Formatter fmt = new Formatter();
		fmt.format("Cookie{name:%s", name());
		fmt.format(" ,value:%s", value());
		fmt.format(" ,path:%s", path());
		fmt.format(" ,domain:%s", domain());
		fmt.format(" ,maxAge:%d", maxAge());
		fmt.format(" ,discard:%s", discard());
		fmt.format(" ,secure:%s", secure());
		fmt.format(" ,version:%s", version());
		fmt.format(" ,httpOnly:%s", httpOnly());
		fmt.format(" ,expires:%s", expires());
		fmt.format(" ,comment:%s}", comment());
		return fmt.toString();
	}
}
