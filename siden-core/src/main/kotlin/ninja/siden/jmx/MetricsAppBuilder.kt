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
package ninja.siden.jmx

import io.undertow.server.HttpHandler
import io.undertow.server.handlers.MetricsHandler
import io.undertow.server.session.InMemorySessionManager
import io.undertow.server.session.SessionAttachmentHandler
import io.undertow.server.session.SessionCookieConfig
import ninja.siden.*
import ninja.siden.def.*
import ninja.siden.util.ExactlyOnceCloseable
import org.xnio.OptionMap
import java.lang.management.ManagementFactory
import javax.management.ObjectName

/**
 * @author taichi
 */
class MetricsAppBuilder(config: OptionMap) : DefaultAppBuilder(config) {

    override fun apply(context: AppContext, def: RoutingDef) {
        val newone = RoutingDef(def.template, def.predicate, def.method, makeRouteTracker(context, def), def.renderer)
        newone.type(newone.type)
        newone.accepts = newone.accepts
        newone.matches = newone.matches
        newone.addTo(this.router)
    }

    protected fun makeRouteTracker(context: AppContext, original: RoutingDef): (Request, Response) -> Any {
        val meter = RequestMeter()
        val tracker = RouteTracker(meter)
        register(context.root, tracker, listOf("type", "Request", "path",
                ObjectName.quote(context.prefix + original.template),
                "method", original.method.name))
        return { q, s -> meter.record { original.route(q, s) } }
    }

    override fun apply(context: AppContext, def: SubAppDef) {
        val kids = MetricsAppBuilder(this.config)
        def.app.accept(AppContext(context, def), kids)
        this.subapp.addPrefixPath(def.prefix, kids.filters)
    }

    override fun apply(context: AppContext, def: WebSocketDef) {
        val newone = WebSocketDef(def.template, def.predicate, makeWebSocketTracker(context, def))
        newone.addTo(this.websockets)
    }

    protected fun makeWebSocketTracker(context: AppContext, def: WebSocketDef): WebSocketFactory {
        val tracker = WebSocketTracker(def.factory)
        register(context.root, tracker, listOf("type", "WebSocket", "path", ObjectName.quote(context.prefix + def.template)))
        return tracker
    }

    override fun makeSessionHandler(root: App, config: OptionMap, next: HttpHandler): HttpHandler {
        val sessionManager = InMemorySessionManager("SessionManagerOfSiden", config.get(Config.MAX_SESSIONS))
        sessionManager.setDefaultSessionTimeout(config.get(Config.DEFAULT_SESSION_TIMEOUT_SECONDS))
        val sessionConfig = SessionCookieConfig()
        sessionConfig.cookieName = config.get(Config.SESSION_COOKIE_NAME)

        register(root, SessionMetrics.to(sessionManager), listOf("type", "Session"))

        return SessionAttachmentHandler(next, sessionManager, sessionConfig)
    }

    override fun makeSharedHandlers(root: App, config: OptionMap,
                                    next: HttpHandler): HttpHandler {
        val shared = super.makeSharedHandlers(root, config, next)
        register(root, RequestMetrics.to(MetricsHandler(shared)), listOf("type", "Request", "name", "Global"))
        return shared
    }

    protected fun register(root: App, bean: Any, attrs: List<String>) {
        val name = "ninja.siden".to(attrs)
        val server = ManagementFactory.getPlatformMBeanServer()
        server.registerMBean(bean, name)
        val ec = ExactlyOnceCloseable.wrap (AutoCloseable { server.unregisterMBean(name) })
        root.stopOn { app -> ec.close() }
    }
}
