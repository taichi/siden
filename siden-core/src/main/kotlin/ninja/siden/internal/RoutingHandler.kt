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
package ninja.siden.internal

import io.undertow.Undertow
import io.undertow.predicate.Predicate
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import ninja.siden.App
import ninja.siden.Config
import ninja.siden.Renderer
import ninja.siden.Route
import ninja.siden.def.ErrorCodeRoutingDef
import ninja.siden.def.ExceptionalRoutingDef
import java.util.*
import kotlin.reflect.KClass

/**
 * @author taichi
 */
class RoutingHandler(internal val next: HttpHandler) : HttpHandler {

    internal val routings: MutableList<Routing> = ArrayList()

    internal val exceptionalMappings: MutableMap<Class<out Throwable>, ExceptionalRoutingDef<out Throwable>> = HashMap()

    internal val errorCodeMappings = HashMap<Int, MutableList<ErrorCodeRoutingDef>>()

    override fun handleRequest(exchange: HttpServerExchange) {
        for (route in routings) {
            if (route.predicate.resolve(exchange)) {
                val hh = HttpHandler { ex ->
                    handle(ex, Route { request, response -> route.route.handle(request, response) },
                            route.renderer)
                }
                if (exchange.isInIoThread) {
                    exchange.dispatch(hh)
                } else {
                    hh.handleRequest(exchange)
                }
                return
            }
        }
        this.next.handleRequest(exchange)
    }

    internal fun handle(responseCode: Int, exchange: HttpServerExchange): Boolean {
        if (exchange.isRequestChannelAvailable) {
            exchange.responseCode = responseCode
            val list = this.errorCodeMappings[responseCode] ?:emptyList<ErrorCodeRoutingDef>()
            for (route in list) {
                if (handle(exchange, Route { request, response -> route.route.handle(request, response) }, route.renderer)) {
                    return true
                }
            }
        }
        return false
    }

    internal fun <T : Throwable> find(exception: T): ExceptionalRoutingDef<T>? {
        var c: Class<*>? = exception.javaClass
        while (c != null) {
            @Suppress("UNCHECKED_CAST")
            val route = this.exceptionalMappings[c] as ExceptionalRoutingDef<T>?
            if (route != null) {
                return route
            }
            c = c.superclass
        }
        return null
    }

    internal fun <T : Throwable> handle(exception: T,
                                        exchange: HttpServerExchange): Boolean {
        val def = find(exception)
        return def != null && handle(exchange, Route { req, res ->
            exchange.responseCode = 500
            def.route.handle(exception, req, res)
        }, def.renderer)
    }

    internal fun handle(exchange: HttpServerExchange, fn: Route, renderer: Renderer<*>?): Boolean {
        try {
            val request = exchange.getAttachment(Core.REQUEST)
            val response = exchange.getAttachment(Core.RESPONSE)
            var model: Any? = fn.handle(request, response)
            if (model != null) {
                if (model is Optional<*>) {
                    model = model.orElse(null)
                }
                if (model is Int) {
                    return handle(model, exchange)
                }
                if (model is ExchangeState) {
                    return true
                }
                if (model != null && contains(model.javaClass) == false) {
                    resolve(renderer, exchange).render(model, exchange)
                    return true
                }
            }
            return handle(exchange.responseCode, exchange)
        } catch (ex: Exception) {
            if (exchange.isRequestChannelAvailable) {
                if (handle(ex, exchange)) {
                    return true
                }
            }
            throw ex
        }

    }

    @Suppress("UNCHECKED_CAST")
    internal fun resolve(renderer: Renderer<*>?, exchange: HttpServerExchange): Renderer<Any> {
        if (renderer == null) {
            val config = exchange.getAttachment(Core.CONFIG)
            return config.get(Config.DEFAULT_RENDERER) as Renderer<Any>
        }
        return renderer as Renderer<Any>
    }

    internal operator fun contains(clazz: Class<*>): Boolean {
        val n = clazz.name
        return ignorePackages.any( { n.startsWith(it) })
    }

    fun add(predicate: Predicate, route: Route, renderer: Renderer<*>?) {
        this.routings.add(Routing(predicate, route, renderer))
    }

    fun add(model: ExceptionalRoutingDef<*>) {
        this.exceptionalMappings.put(model.type, model)
    }

    fun add(model: ErrorCodeRoutingDef) {
        var list = this.errorCodeMappings[model.code]
        if (list == null) {
            list = ArrayList<ErrorCodeRoutingDef>()
        }
        list.add(model)
        this.errorCodeMappings.put(model.code, list)
    }

    internal data class Routing(var predicate: Predicate, var route: Route, var renderer: Renderer<*>?)

    companion object {

        internal val ignorePackages: MutableSet<String> = HashSet()

        internal val KClass<*>.packageName: String get() = this.java.`package`.name

        init {
            ignorePackages.add(App::class.packageName)
            ignorePackages.add(Undertow::class.packageName)
        }
    }
}
