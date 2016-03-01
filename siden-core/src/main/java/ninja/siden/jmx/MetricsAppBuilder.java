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
import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Route;
import ninja.siden.WebSocketFactory;
import ninja.siden.def.*;
import ninja.siden.util.ExactlyOnceCloseable;
import org.xnio.OptionMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * @author taichi
 */
public class MetricsAppBuilder extends DefaultAppBuilder {

    public MetricsAppBuilder(OptionMap config) {
        super(config);
    }

    @Override
    public void apply(AppContext context, RoutingDef rd) {
        RoutingDef def = new RoutingDef(rd.getTemplate(), rd.getPredicate(),
                rd.getMethod(), makeRouteTracker(context, rd));
        if (rd.getRenderer() != null) {
            def.render(rd.getRenderer());
        }
        def.type(rd.getType());
        def.setAccepts(rd.getAccepts());
        def.setMatches(rd.getMatches());
        def.addTo(this.getRouter());
    }

    protected Route makeRouteTracker(AppContext context, RoutingDef original) {
        RouteTracker tracker = new RouteTracker(original.getRoute());
        register(context.getRoot(), tracker, Arrays.asList("type", "Request",
                "path",
                ObjectName.quote(context.getPrefix() + original.getTemplate()),
                "method", original.getMethod().name()));
        return tracker;
    }

    @Override
    public void apply(AppContext context, SubAppDef def) {
        MetricsAppBuilder kids = new MetricsAppBuilder(this.getConfig());
        def.getApp().accept(new AppContext(context, def), kids);
        this.getSubapp().addPrefixPath(def.getPrefix(), kids.getFilters());
    }

    @Override
    public void apply(AppContext context, WebSocketDef original) {
        WebSocketDef def = new WebSocketDef(original.getTemplate(),
                original.getPredicate(), makeWebSocketTracker(context, original));
        def.addTo(this.getWebsockets());
    }

    protected WebSocketFactory makeWebSocketTracker(AppContext context, WebSocketDef def) {
        WebSocketTracker tracker = new WebSocketTracker(def.getFactory());
        register(
                context.getRoot(),
                tracker,
                Arrays.asList("type", "WebSocket", "path",
                        ObjectName.quote(context.getPrefix() + def.getTemplate())));
        return tracker;
    }

    @Override
    protected HttpHandler makeSessionHandler(App root, OptionMap config, HttpHandler next) {
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
            ExactlyOnceCloseable ec = ExactlyOnceCloseable.wrap(() -> server.unregisterMBean(name));
            root.stopOn(app -> ec.close());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
