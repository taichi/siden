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
package ninja.siden

import io.undertow.Undertow
import io.undertow.predicate.Predicates
import io.undertow.server.HttpHandler
import ninja.siden.def.AppBuilder
import ninja.siden.def.AppContext
import ninja.siden.def.AppDef
import ninja.siden.def.DefaultAppBuilder
import ninja.siden.internal.LambdaWebSocketFactory
import ninja.siden.internal.PathPredicate
import ninja.siden.jmx.MetricsAppBuilder
import ninja.siden.util.Publisher
import org.jboss.logging.Logger
import org.xnio.OptionMap
import java.util.regex.Pattern

/**
 * @author taichi
 */
class App(val config: OptionMap = Config.defaults().map) {

    protected val def: AppDef = AppDef(config)

    protected val stop = Publisher<App>()

    // request handling
    fun get(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.GET, path, route)

    fun head(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.HEAD, path, route)

    fun post(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.POST, path, route)

    fun put(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.PUT, path, route)

    fun delete(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.DELETE, path, route)

    fun trace(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.TRACE, path, route)

    fun options(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.OPTIONS, path, route)

    fun connect(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.CONNECT, path, route)

    fun patch(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.PATCH, path, route)

    fun link(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.LINK, path, route)

    fun unlink(path: String, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.UNLINK, path, route)

    fun websocket(path: String, factory: WebSocketFactory) = this.def.add(path, PathPredicate(path), factory)

    fun websocket(path: String): WebSocketCustomizer {
        val factory = LambdaWebSocketFactory()
        websocket(path, factory)
        return factory
    }

    fun get(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.GET, p, route)

    fun head(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.HEAD, p, route)

    fun post(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.POST, p, route)

    fun put(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.PUT, p, route)

    fun delete(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.DELETE, p, route)

    fun trace(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.TRACE, p, route)

    fun options(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.OPTIONS, p, route)

    fun connect(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.CONNECT, p, route)

    fun patch(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.PATCH, p, route)

    fun link(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.LINK, p, route)

    fun unlink(p: Pattern, route: (Request, Response) -> Any): RoutingCustomizer = this.def.add(HttpMethod.UNLINK, p, route)

    fun websocket(path: Pattern, factory: WebSocketFactory) = this.def.add(path.pattern(), PathPredicate(path), factory)

    fun websocket(path: Pattern): WebSocketCustomizer {
        val factory = LambdaWebSocketFactory()
        websocket(path, factory)
        return factory
    }

    /**
     * serve static resources on `root` from `root` directory.

     * @param root
     * *
     * @return
     */
    fun assets(root: String): AssetsCustomizer = this.assets("/" + root, root)

    /**
     * mount static resources on `path` from `root` directory

     * @param path
     * *
     * @param root
     * *
     * @return
     */
    fun assets(path: String, root: String): AssetsCustomizer = this.def.add(path, root)

    // Filter requests
    fun use(filter: Filter) = this.def.add(Predicates.truePredicate(), filter)

    fun use(path: String, filter: Filter) = this.def.add(PathPredicate(path), filter)

    fun <T : Throwable> error(type: Class<T>, route: (T, Request, Response) -> Any): RendererCustomizer<*>
            = this.def.add(type, route)

    fun error(statusCode: Int, route: (Request, Response) -> Any): RendererCustomizer<*> = this.def.add(statusCode, route)

    /**
     * mount sub application on `path`

     * @param path
     * *
     * @param sub
     */
    fun use(path: String, sub: App) = this.def.add(path, sub.def)

    @JvmOverloads fun listen(port: Int = 8080): Stoppable {
        return listen("0.0.0.0", port)
    }

    @JvmOverloads fun listen(host: String, port: Int,
                             fn: (Undertow.Builder) -> Undertow.Builder = { b -> b.addHttpListener(port, host) }): Stoppable {
        return _listen(fn)
    }

    /**
     * listening more complex server

     * @param fn
     */
    internal fun _listen(fn: (Undertow.Builder) -> Undertow.Builder): Stoppable {
        val builder = fn(Undertow.builder())
        val server = builder.setHandler(buildHandlers()).build()
        server.start()
        stop.on { stop -> server.stop() }
        return object : Stoppable {

            override fun stop() {
                stop.post(this@App)
            }

            override fun addShutdownHook() {
                Runtime.getRuntime().addShutdownHook(Thread(Runnable { this.stop() }))
            }
        }
    }

    protected fun buildHandlers(): HttpHandler {
        val ab = newBuilder()(this.def.config)
        ab.begin()
        this.def.accept(AppContext(this), ab)
        return ab.end(this)
    }

    protected fun newBuilder(): (OptionMap) -> AppBuilder {
        return if (Config.isInDev(this.def.config))
            ::DefaultAppBuilder
        else
            ::MetricsAppBuilder
    }

    fun stopOn(fn: (App) -> Unit) {
        stop.on(fn)
    }

    companion object {
        internal val LOG = Logger.getLogger(App::class.java)

        fun configure(fn: (OptionMap.Builder) -> OptionMap.Builder): App {
            val omb = fn(Config.defaults())
            return App(omb.map)
        }
    }
}
