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
import io.undertow.predicate.Predicates
import ninja.siden.*
import ninja.siden.internal.Core
import ninja.siden.internal.MIMEPredicate
import ninja.siden.internal.RoutingHandler
import java.util.*

/**
 * @author taichi
 */
class RoutingDef(val template: String,
                 val predicate: Predicate,
                 val method: HttpMethod,
                 val route: (Request, Response) -> Any,
                 var renderer: Renderer<*>? = null
                 ) : RoutingCustomizer {

    var type: String = ""
        internal set
    var accepts: MutableList<String> = ArrayList()
    var matches = Predicates.truePredicate()

    override fun type(type: String): RoutingCustomizer {
        this.type = type
        return this
    }

    override fun accept(type: String): RoutingCustomizer {
        this.accepts.add(type)
        return this
    }

    override fun match(fn: (Request) -> Boolean): RoutingCustomizer {
        this.matches = Predicates.and(this.matches, Core.adapt(fn))
        return this
    }

    override fun <MODEL> render(renderer: Renderer<MODEL>): RoutingCustomizer {
        this.renderer = renderer
        return this
    }

    fun addTo(rh: RoutingHandler) {
        val list = ArrayList<Predicate>()
        list.add(this.predicate)
        list.add(this.matches)
        var route = this.route
        val t = this.type
        if (t.isNullOrEmpty() == false) {
            list.add(MIMEPredicate.accept(type))
            route = { req, res ->
                val result = this.route(req, res)
                res.type(this.type)
                result
            }
        }
        list.addAll(this.accepts.filter(String::isNotEmpty).map({ MIMEPredicate.contentType(it) }))
        rh.add(Predicates.and(*list.toArray<Predicate>(arrayOfNulls<Predicate>(list.size))), route, this.renderer)
    }
}
