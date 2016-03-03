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

import io.undertow.predicate.Predicate
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.HttpString
import io.undertow.util.QValueParser
import java.util.regex.Pattern

/**
 * @author taichi
 */
class MIMEPredicate(internal val name: HttpString, contentType: String) : Predicate {

    internal val type: String

    internal val subType: String

    init {
        if (wildCard(contentType)) {
            this.type = "*"
            this.subType = "*"
        } else {
            val m = MIME.matcher(contentType)
            if (m.find()) {
                this.type = m.group("type")
                this.subType = m.group("subtype")
            } else {
                throw IllegalArgumentException("contentType")
            }
        }
    }

    override fun resolve(value: HttpServerExchange): Boolean {
        val res = value.requestHeaders.get(name)
        if (res == null || res.isEmpty()) {
            return false
        }
        val found = QValueParser.parse(res)
        return found.flatMap( { it }).any( { this.match(it) })
    }

    internal fun wildCard(s: String): Boolean {
        return s == "*"
    }

    internal fun match(result: QValueParser.QValueResult): Boolean {
        val v = result.value
        if (wildCard(v) || wildCard(this.type)) {
            return true
        }
        val m = MIME.matcher(v)
        if (m.find() == false) {
            return false
        }
        val t = m.group("type")
        if (wildCard(t)) {
            return true
        }
        if (this.type.equals(t, ignoreCase = true)) {
            val sub = m.group("subtype")
            if (wildCard(sub) || wildCard(this.subType)
                    || this.subType.equals(sub, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    companion object {

        internal val MIME = Pattern.compile("(?<type>[*\\w]+)/(?<subtype>[*-.\\w]+)(;(.*))?")

        fun accept(type: String): Predicate {
            return MIMEPredicate(Headers.ACCEPT, type)
        }

        fun contentType(type: String): Predicate {
            return MIMEPredicate(Headers.CONTENT_TYPE, type)
        }
    }
}
