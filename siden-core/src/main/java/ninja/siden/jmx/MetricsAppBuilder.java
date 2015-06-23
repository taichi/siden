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

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Route;
import ninja.siden.WebSocketFactory;
import ninja.siden.def.AppContext;
import ninja.siden.def.DefaultAppBuilder;
import ninja.siden.def.RoutingDef;
import ninja.siden.def.SubAppDef;
import ninja.siden.def.WebSocketDef;
import ninja.siden.util.ExactlyOnceCloseable;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class MetricsAppBuilder extends DefaultAppBuilder {

	public MetricsAppBuilder(OptionMap config) {
		super(config);
	}

	@Override
	public void apply(AppContext context, RoutingDef rd) {
		RoutingDef def = new RoutingDef(rd.template(), rd.predicate(),
				rd.method(), makeRouteTracker(context, rd));
		if (rd.renderer() != null) {
			def.render(rd.renderer());
		}
		def.type(rd.type());
		def.acceps(rd.accepts());
		def.matches(rd.matches());
		def.addTo(this.router, this.config);
	}

	protected Route makeRouteTracker(AppContext context, RoutingDef original) {
		RouteTracker tracker = new RouteTracker(original.route());
		register(context.root(), tracker, Arrays.asList("type", "Request",
				"path",
				ObjectName.quote(context.prefix() + original.template()),
				"method", original.method().name()));
		return tracker;
	}

	@Override
	public void apply(AppContext context, SubAppDef def) {
		MetricsAppBuilder kids = new MetricsAppBuilder(this.config);
		def.app().accept(new AppContext(context, def), kids);
		this.subapp.addPrefixPath(def.prefix(), kids.filters);
	}

	@Override
	public void apply(AppContext context, WebSocketDef original) {
		WebSocketDef def = new WebSocketDef(original.template(),
				original.predicate(), makeWebSocketTracker(context, original));
		def.addTo(this.websockets, this.config);
	}

	protected WebSocketFactory makeWebSocketTracker(AppContext context,
			WebSocketDef def) {
		WebSocketTracker tracker = new WebSocketTracker(def.factory());
		register(
				context.root(),
				tracker,
				Arrays.asList("type", "WebSocket", "path",
						ObjectName.quote(context.prefix() + def.template())));
		return tracker;
	}

	@Override
	protected HttpHandler makeSessionHandler(App root, OptionMap config,
			HttpHandler next) {
		InMemorySessionManager sessionManager = new InMemorySessionManager(
				"SessionManagerOfSiden", config.get(Config.MAX_SESSIONS));
		sessionManager.setDefaultSessionTimeout(config
				.get(Config.DEFAULT_SESSION_TIMEOUT_SECONDS));
		SessionCookieConfig sessionConfig = new SessionCookieConfig();
		sessionConfig.setCookieName(config.get(Config.SESSION_COOKIE_NAME));

		register(root, SessionMetrics.to(sessionManager),
				Arrays.asList("type", "Session"));

		return new SessionAttachmentHandler(next, sessionManager, sessionConfig);
	}

	@Override
	protected HttpHandler makeSharedHandlers(App root, OptionMap config,
			HttpHandler next) {
		HttpHandler shared = super.makeSharedHandlers(root, config, next);
		register(root, RequestMetrics.to(new MetricsHandler(shared)),
				Arrays.asList("type", "Request", "name", "Global"));
		return shared;
	}

	protected void register(App root, Object bean, List<String> attrs) {
		try {
			ObjectName name = ObjectNames.to("ninja.siden", attrs);
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			server.registerMBean(bean, name);
			ExactlyOnceCloseable ec = ExactlyOnceCloseable.wrap(() -> server
					.unregisterMBean(name));
			root.stopOn(app -> ec.close());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
