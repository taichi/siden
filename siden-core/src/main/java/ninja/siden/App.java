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

import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import ninja.siden.def.AppBuilder;
import ninja.siden.def.AppContext;
import ninja.siden.def.AppDef;
import ninja.siden.def.DefaultAppBuilder;
import ninja.siden.internal.LambdaWebSocketFactory;
import ninja.siden.internal.PathPredicate;
import ninja.siden.jmx.MetricsAppBuilder;
import ninja.siden.util.Publisher;

import org.jboss.logging.Logger;
import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class App {

	static final Logger LOG = Logger.getLogger(App.class);

	protected AppDef def;

	protected Publisher<App> stop = new Publisher<>();

	public App() {
		this(Config.defaults().getMap());
	}

	protected App(OptionMap config) {
		Objects.requireNonNull(config);
		this.def = new AppDef(config);
	}

	public static App configure(
			Function<OptionMap.Builder, OptionMap.Builder> fn) {
		OptionMap.Builder omb = fn.apply(Config.defaults());
		return new App(omb.getMap());
	}

	// request handling
	public RoutingCustomizer get(String path, Route route) {
		return this.def.add(HttpMethod.GET, path, route);
	}

	public RoutingCustomizer head(String path, Route route) {
		return this.def.add(HttpMethod.HEAD, path, route);
	}

	public RoutingCustomizer post(String path, Route route) {
		return this.def.add(HttpMethod.POST, path, route);
	}

	public RoutingCustomizer put(String path, Route route) {
		return this.def.add(HttpMethod.PUT, path, route);
	}

	public RoutingCustomizer delete(String path, Route route) {
		return this.def.add(HttpMethod.DELETE, path, route);
	}

	public RoutingCustomizer trace(String path, Route route) {
		return this.def.add(HttpMethod.TRACE, path, route);
	}

	public RoutingCustomizer options(String path, Route route) {
		return this.def.add(HttpMethod.OPTIONS, path, route);
	}

	public RoutingCustomizer connect(String path, Route route) {
		return this.def.add(HttpMethod.CONNECT, path, route);
	}

	public RoutingCustomizer patch(String path, Route route) {
		return this.def.add(HttpMethod.PATCH, path, route);
	}

	public RoutingCustomizer link(String path, Route route) {
		return this.def.add(HttpMethod.LINK, path, route);
	}

	public RoutingCustomizer unlink(String path, Route route) {
		return this.def.add(HttpMethod.UNLINK, path, route);
	}

	public void websocket(String path, WebSocketFactory factory) {
		this.def.add(path, new PathPredicate(path), factory);
	}

	public WebSocketCustomizer websocket(String path) {
		LambdaWebSocketFactory factory = new LambdaWebSocketFactory();
		websocket(path, factory);
		return factory;
	}

	public RoutingCustomizer get(Pattern p, Route route) {
		return this.def.add(HttpMethod.GET, p, route);
	}

	public RoutingCustomizer head(Pattern p, Route route) {
		return this.def.add(HttpMethod.HEAD, p, route);
	}

	public RoutingCustomizer post(Pattern p, Route route) {
		return this.def.add(HttpMethod.POST, p, route);
	}

	public RoutingCustomizer put(Pattern p, Route route) {
		return this.def.add(HttpMethod.PUT, p, route);
	}

	public RoutingCustomizer delete(Pattern p, Route route) {
		return this.def.add(HttpMethod.DELETE, p, route);
	}

	public RoutingCustomizer trace(Pattern p, Route route) {
		return this.def.add(HttpMethod.TRACE, p, route);
	}

	public RoutingCustomizer options(Pattern p, Route route) {
		return this.def.add(HttpMethod.OPTIONS, p, route);
	}

	public RoutingCustomizer connect(Pattern p, Route route) {
		return this.def.add(HttpMethod.CONNECT, p, route);
	}

	public RoutingCustomizer patch(Pattern p, Route route) {
		return this.def.add(HttpMethod.PATCH, p, route);
	}

	public RoutingCustomizer link(Pattern p, Route route) {
		return this.def.add(HttpMethod.LINK, p, route);
	}

	public RoutingCustomizer unlink(Pattern p, Route route) {
		return this.def.add(HttpMethod.UNLINK, p, route);
	}

	public void websocket(Pattern path, WebSocketFactory factory) {
		this.def.add(path.pattern(), new PathPredicate(path), factory);
	}

	public WebSocketCustomizer websocket(Pattern path) {
		LambdaWebSocketFactory factory = new LambdaWebSocketFactory();
		websocket(path, factory);
		return factory;
	}

	/**
	 * serve static resources on {@code root} from {@code root} directory.
	 * 
	 * @param root
	 * @return
	 */
	public AssetsCustomizer assets(String root) {
		return this.assets("/" + root, root);
	}

	/**
	 * mount static resources on {@code path} from {@code root} directory
	 * 
	 * @param path
	 * @param root
	 * @return
	 */
	public AssetsCustomizer assets(String path, String root) {
		return this.def.add(path, root);
	}

	// Filter requests
	public void use(Filter filter) {
		this.def.add(Predicates.truePredicate(), filter);
	}

	public void use(String path, Filter filter) {
		this.def.add(new PathPredicate(path), filter);
	}

	public <T extends Throwable> RendererCustomizer<?> error(Class<T> type,
			ExceptionalRoute<T> route) {
		return this.def.add(type, route);
	}

	public RendererCustomizer<?> error(int statusCode, Route route) {
		return this.def.add(statusCode, route);
	}

	/**
	 * mount sub application on {@code path}
	 * 
	 * @param path
	 * @param sub
	 */
	public void use(String path, App sub) {
		this.def.add(path, sub.def);
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
		return listen("0.0.0.0", port);
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
		Undertow server = builder.setHandler(buildHandlers()).build();
		server.start();
		stop.on(stop -> server.stop());
		return new Stoppable() {

			@Override
			public void stop() {
				stop.post(App.this);
			}

			@Override
			public void addShutdownHook() {
				Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
			}
		};
	}

	protected HttpHandler buildHandlers() {
		AppBuilder visitor = newBuilder().apply(this.def.config());
		visitor.begin();
		this.def.accept(new AppContext(this), visitor);
		return visitor.end(this);
	}

	protected Function<OptionMap, AppBuilder> newBuilder() {
		return Config.isInDev(this.def.config()) ? DefaultAppBuilder::new
				: MetricsAppBuilder::new;
	}

	public void stopOn(Consumer<App> fn) {
		stop.on(fn);
	}
}
