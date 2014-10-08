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
package ninja.siden;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.predicate.PredicatesHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;

import java.io.File;
import java.util.function.Function;

import ninja.siden.internal.AssetsHandler;
import ninja.siden.internal.ConnectionCallback;
import ninja.siden.internal.Core;
import ninja.siden.internal.FiltersHandler;
import ninja.siden.internal.LambdaWebSocketFactory;
import ninja.siden.internal.MethodOverrideHandler;
import ninja.siden.internal.PathPredicate;
import ninja.siden.internal.RoutingHandler;
import ninja.siden.internal.SecurityHandler;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class App {

	AssetsHandler assets;

	RoutingHandler router;

	PredicatesHandler websockets;

	PathHandler subapp;

	FiltersHandler filters;

	HttpHandler shared;

	public App() {
		this(Config.defaults().getMap());
	}

	protected App(OptionMap config) {
		this.assets = new AssetsHandler(config);
		this.router = new RoutingHandler(this.assets);
		this.subapp = new PathHandler(this.router);
		this.websockets = new PredicatesHandler(this.subapp);
		this.filters = new FiltersHandler(this.websockets);
		this.shared = wrap(config, this.filters);
	}

	protected HttpHandler wrap(OptionMap config, HttpHandler handler) {
		HttpHandler hh = handler;
		if (config.get(Config.METHOD_OVERRIDE)) {
			hh = new MethodOverrideHandler(hh);
		}
		hh = makeSessionHandler(config, hh);
		hh = makeFormHandler(config, hh);

		if ("development".equalsIgnoreCase(config.get(Config.ENV))) {
			hh = Handlers.disableCache(hh);
		}

		hh = new SecurityHandler(hh);
		return new Core(config, hh);
	}

	protected HttpHandler makeSessionHandler(OptionMap config, HttpHandler next) {
		InMemorySessionManager sessionManager = new InMemorySessionManager(
				"SessionManagerOfSiden", config.get(Config.MAX_SESSIONS));
		sessionManager.setDefaultSessionTimeout(config
				.get(Config.DEFAULT_SESSION_TIMEOUT_SECONDS));
		SessionCookieConfig sessionConfig = new SessionCookieConfig();
		sessionConfig.setCookieName(config.get(Config.SESSION_COOKIE_NAME));

		return new SessionAttachmentHandler(next, sessionManager, sessionConfig);
	}

	protected HttpHandler makeFormHandler(OptionMap config, HttpHandler next) {
		FormParserFactory.Builder builder = FormParserFactory.builder(false);
		FormEncodedDataDefinition form = new FormEncodedDataDefinition();
		String cn = config.get(Config.CHARSET).name();
		form.setDefaultEncoding(cn);

		MultiPartParserDefinition mult = new MultiPartParserDefinition(
				config.get(Config.TEMP_DIR));
		mult.setDefaultEncoding(cn);
		mult.setMaxIndividualFileSize(config.get(Config.MAX_FILE_SIZE));

		builder.addParsers(form, mult);

		EagerFormParsingHandler efp = new EagerFormParsingHandler(
				builder.build());
		efp.setNext(next);
		return efp;
	}

	public static App configure(
			Function<OptionMap.Builder, OptionMap.Builder> fn) {
		OptionMap.Builder omb = fn.apply(Config.defaults());
		return new App(omb.getMap());
	}

	// request handling
	protected RoutingCustomizer verb(HttpMethod method, String path, Route route) {
		return this.router.add(Predicates.and(method, new PathPredicate(path)),
				route);
	}

	public RoutingCustomizer get(String path, Route route) {
		return verb(HttpMethod.GET, path, route);
	}

	public RoutingCustomizer head(String path, Route route) {
		return verb(HttpMethod.HEAD, path, route);
	}

	public RoutingCustomizer post(String path, Route route) {
		return verb(HttpMethod.POST, path, route);
	}

	public RoutingCustomizer put(String path, Route route) {
		return verb(HttpMethod.PUT, path, route);
	}

	public RoutingCustomizer delete(String path, Route route) {
		return verb(HttpMethod.DELETE, path, route);
	}

	public RoutingCustomizer trace(String path, Route route) {
		return verb(HttpMethod.TRACE, path, route);
	}

	public RoutingCustomizer options(String path, Route route) {
		return verb(HttpMethod.OPTIONS, path, route);
	}

	public RoutingCustomizer connect(String path, Route route) {
		return verb(HttpMethod.CONNECT, path, route);
	}

	public RoutingCustomizer patch(String path, Route route) {
		return verb(HttpMethod.PATCH, path, route);
	}

	public RoutingCustomizer link(String path, Route route) {
		return verb(HttpMethod.LINK, path, route);
	}

	public RoutingCustomizer unlink(String path, Route route) {
		return verb(HttpMethod.UNLINK, path, route);
	}

	public void websocket(String path, WebSocketFactory factory) {
		PathPredicate pp = new PathPredicate(path);
		this.websockets.addPredicatedHandler(pp, next -> {
			return new WebSocketProtocolHandshakeHandler(
					new ConnectionCallback(factory), next);
		});
	}

	public WebSocketCustomizer websocket(String path) {
		LambdaWebSocketFactory factory = new LambdaWebSocketFactory();
		websocket(path, factory);
		return factory;
	}

	/**
	 * serve static resources from root directory.
	 * 
	 * @param root
	 * @return
	 */
	public AssetsCustomizer assets(String root) {
		return this.assets("/", root);
	}

	/**
	 * mount static resources on {@code path} from {@code root} directory
	 * 
	 * @param path
	 * @param root
	 * @return
	 */
	public AssetsCustomizer assets(String path, String root) {
		return this.assets.add(path, new File(root));
	}

	// Filter requests
	public void use(Filter filter) {
		this.filters.add(Predicates.truePredicate(), filter);
	}

	public void use(String path, Filter filter) {
		this.filters.add(new PathPredicate(path), filter);
	}

	public <T extends Throwable> RendererCustomizer<?> error(Class<T> type,
			ExceptionalRoute<T> route) {
		return this.router.add(type, route);
	}

	public RendererCustomizer<?> error(int errorCode, Route route) {
		return this.router.add(errorCode, route);
	}

	/**
	 * mount sub application on {@code path}
	 * 
	 * @param path
	 * @param sub
	 */
	public void use(String path, App sub) {
		this.subapp.addPrefixPath(path, sub.filters);
	}

	/**
	 * listening http at localhost:8080
	 */
	public Stoppable listen() {
		return listen(8080);
	}

	/**
	 * listening http at localhost with port
	 * 
	 * @param port
	 */
	public Stoppable listen(int port) {
		return listen("localhost", port);
	}

	/**
	 * listening http at host and port
	 * 
	 * @param host
	 * @param port
	 */
	public Stoppable listen(String host, int port) {
		return listen((b) -> b.addHttpListener(port, host));
	}

	/**
	 * listening more complex server
	 * 
	 * @param fn
	 */
	public Stoppable listen(Function<Undertow.Builder, Undertow.Builder> fn) {
		Undertow.Builder builder = fn.apply(Undertow.builder());
		Undertow server = builder.setHandler(this.shared).build();
		server.start();
		return server::stop;
	}

	@FunctionalInterface
	public interface Stoppable {
		void stop();
	}
}
