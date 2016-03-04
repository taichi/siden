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

import io.undertow.predicate.Predicate
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import io.undertow.util.Methods
import java.util.*

/**
 * @author taichi
 */
enum class HttpMethod private constructor(internal val rawdata: HttpString) : Predicate {

    GET(Methods.GET), HEAD(Methods.HEAD), POST(Methods.POST), PUT(Methods.PUT),
    DELETE(Methods.DELETE), TRACE(Methods.TRACE), OPTIONS(Methods.OPTIONS), CONNECT(Methods.CONNECT),
    PATCH(HttpString("PATCH")), LINK(HttpString("LINK")), UNLINK(HttpString("UNLINK"));

    override fun resolve(value: HttpServerExchange): Boolean {
        return this.rawdata.equals(value.requestMethod)
    }

    companion object {

        internal val methods: MutableMap<HttpString, HttpMethod> = HashMap()

        init {
            for (hm in HttpMethod.values()) {
                methods.put(hm.rawdata, hm)
            }
        }

        fun of(exchange: HttpServerExchange): HttpMethod {
            return methods[exchange.requestMethod] ?: GET
        }

        fun find(method: String?): Optional<HttpString> {
            if(method.isNullOrEmpty()) {
                return Optional.empty<HttpString>()
            }
            val m = method!!.toUpperCase()
            return Optional.ofNullable(methods[HttpString(m)]).map({ it!!.rawdata })
        }
    }
}
