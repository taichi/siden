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
import io.undertow.util.DateUtils;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;

import ninja.siden.Config;
import ninja.siden.Cookie;
import ninja.siden.Renderer;
import ninja.siden.RendererRepository;
import ninja.siden.Response;
import ninja.siden.util.Suppress;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class SidenResponse implements Response {
	
	final HttpServerExchange exchange;

	public SidenResponse(HttpServerExchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public Response status(int code) {
		this.exchange.setResponseCode(code);
		return this;
	}

	@Override
	public Response header(String name, String... values) {
		HeaderMap hm = this.exchange.getResponseHeaders();
		hm.remove(name);
		hm.addAll(new HttpString(name), Arrays.asList(values));
		return this;
	}

	@Override
	public Response header(String name, long date) {
		this.exchange.getResponseHeaders().put(new HttpString(name),
				DateUtils.toDateString(new Date(date)));
		return this;
	}

	@Override
	public Response headers(Map<String, String> headers) {
		HeaderMap hm = this.exchange.getResponseHeaders();
		headers.forEach((k, v) -> {
			hm.put(new HttpString(k), v);
		});
		return this;
	}

	@Override
	public Cookie cookie(String name, String value) {
		io.undertow.server.handlers.Cookie c = new io.undertow.server.handlers.CookieImpl(
				name, value);
		this.exchange.setResponseCookie(c);
		return new SidenCookie(c);
	}

	@Override
	public Cookie removeCookie(String name) {
		return new SidenCookie(this.exchange.getResponseCookies().remove(name));
	}

	@Override
	public Response type(String contentType) {
		this.exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,
				contentType);
		return this;
	}

	@Override
	public HttpServerExchange raw() {
		return this.exchange;
	}

	@Override
	public ExchangeState redirect(String location) {
		return this.redirect(StatusCodes.FOUND, location);
	}

	@Override
	public ExchangeState redirect(int code, String location) {
		this.exchange.setResponseCode(code);
		this.exchange.getResponseHeaders().put(Headers.LOCATION, location);
		this.exchange.endExchange();
		return ExchangeState.Redirected;
	}

	@Override
	public <MODEL> ExchangeState render(MODEL model, Renderer<MODEL> renderer) {
		return Suppress.get(() -> {
			renderer.render(model, this.exchange);
			return ExchangeState.Rendered;
		});
	}

	@Override
	public <MODEL> ExchangeState render(MODEL model, String template) {
		OptionMap config = this.exchange.getAttachment(Core.CONFIG);
		RendererRepository repo = config.get(Config.RENDERER_REPOSITORY);
		return render(model, repo.find(template));
	}

	@Override
	@SuppressWarnings("resource")
	public String toString() {
		Formatter fmt = new Formatter();
		fmt.format("RESPONSE{ResponseCode:%d", this.exchange.getResponseCode());
		fmt.format(" ,Headers:[%s]", this.exchange.getResponseHeaders());
		fmt.format(" ,Cookies:[");
		this.exchange.getResponseCookies().forEach((k, c) -> {
			fmt.format("%s", new SidenCookie(c));
		});
		return fmt.format("]}").toString();
	}
}
