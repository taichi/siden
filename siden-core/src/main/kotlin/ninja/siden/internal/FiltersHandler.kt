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

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import ninja.siden.FilterChain
import ninja.siden.Request
import ninja.siden.Response
import ninja.siden.def.FilterDef
import java.util.*

/**
 * @author taichi
 */
class FiltersHandler(internal var next:

                     HttpHandler) : HttpHandler {

    internal var filters = ArrayList<FilterDef>()

    override fun handleRequest(exchange: HttpServerExchange) {
        if (filters.size < 1) {
            next.handleRequest(exchange)
            return
        }
        val chain = SimpleChain(exchange)
        chain.next()
    }

    fun add(model: FilterDef) {
        this.filters.add(model)
    }

    internal enum class ChainState {
        HasNext, NoMore
    }

    internal inner class SimpleChain(var exchange: HttpServerExchange) : FilterChain {

        var cursor: Int = 0

        val request: Request

        val response: Response

        init {
            this.request = exchange.getAttachment(Core.REQUEST)
            this.response = exchange.getAttachment(Core.RESPONSE)
        }

        override fun next(): Any {
            var index = cursor++
            while (index < filters.size) {
                val f = filters[index]
                if (f.resolve(exchange)) {
                    f.filter(request, response, this)
                    return ChainState.HasNext
                }
                index = cursor++
            }
            next.handleRequest(exchange)
            return ChainState.NoMore
        }
    }
}