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
package ninja.siden.def

import io.undertow.predicate.Predicate
import ninja.siden.*
import ninja.siden.internal.PathPredicate
import org.xnio.OptionMap
import java.util.*
import java.util.regex.Pattern

/**
 * @author taichi
 */
class AppDef(internal var config:

             OptionMap) {

    internal var assets: MutableList<AssetDef> = ArrayList()

    internal var router: MutableList<RoutingDef> = ArrayList()

    internal var errorRouter: MutableList<ErrorCodeRoutingDef> = ArrayList()

    internal var exceptionRouter: MutableList<ExceptionalRoutingDef<*>> = ArrayList()

    internal var subapp: MutableList<SubAppDef> = ArrayList()

    internal var websockets: MutableList<WebSocketDef> = ArrayList()

    internal var filters: MutableList<FilterDef> = ArrayList()

    fun config(): OptionMap {
        return this.config
    }

    fun add(path: String, root: String): AssetsCustomizer {
        val def = AssetDef(path, root)
        this.assets.add(def)
        return def
    }

    fun add(method: HttpMethod, path: String, route: Route): RoutingCustomizer {
        return this.add(path, PathPredicate(path), method, route)
    }

    fun add(method: HttpMethod, p: Pattern, route: Route): RoutingCustomizer {
        return this.add(p.pattern(), PathPredicate(p), method, route)
    }

    fun add(template: String, path: PathPredicate,
            method: HttpMethod, route: Route): RoutingCustomizer {
        val def = RoutingDef(template, path, method, route)
        this.router.add(def)
        return def
    }

    fun add(template: String, predicate: Predicate,
            factory: WebSocketFactory) {
        this.websockets.add(WebSocketDef(template, predicate, factory))
    }

    fun add(predicate: Predicate, filter: Filter) {
        this.filters.add(FilterDef(predicate, filter))
    }

    fun add(prefix: String, subapp: AppDef) {
        this.subapp.add(SubAppDef(prefix, subapp))
    }

    fun <T : Throwable> add(type: Class<T>,
                            route: ExceptionalRoute<T>): RendererCustomizer<*> {
        val def = ExceptionalRoutingDef(type, route)
        this.exceptionRouter.add(def)
        return def
    }

    fun add(errorCode: Int, route: Route): RendererCustomizer<*> {
        val def = ErrorCodeRoutingDef(errorCode, route)
        this.errorRouter.add(def)
        return def
    }

    fun accept(context: AppContext, ab: AppBuilder) {
        this.assets.forEach { d -> ab.apply(context, d) }
        this.router.forEach { d -> ab.apply(context, d) }
        this.errorRouter.forEach { d -> ab.apply(context, d) }
        this.exceptionRouter.forEach { d -> ab.apply(context, d) }
        this.subapp.forEach { d -> ab.apply(context, d) }
        this.websockets.forEach { d -> ab.apply(context, d) }
        this.filters.forEach { d -> ab.apply(context, d) }
    }
}
