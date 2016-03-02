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
import io.undertow.util.AttachmentKey
import org.jboss.logging.Logger
import java.util.*
import java.util.regex.Pattern

/**
 * @author taichi
 */
class PathPredicate : Predicate {

    internal var template: Pattern
    internal var names: MutableList<String> = ArrayList()

    constructor(template: Pattern) {
        this.template = template
        val m = NAMED.matcher(template.pattern())
        while (m.find()) {
            names.add(m.group("name"))
        }
        LOG.debug(names)
    }

    constructor(template: String) {
        val stb = StringBuilder(template)
        var m = SEGMENT.matcher(stb)
        var index = 0
        while (index < stb.length && m.find(index)) {
            names.add(m.group("name"))
            LOG.debug(names)
            val v = makeReplacement(m.group("prefix"))
            index = m.start() + v.length
            stb.replace(m.start(), m.end(), v)
            m = SEGMENT.matcher(stb)
        }
        stb.append("(?:.*)")
        LOG.debug(stb)
        this.template = Pattern.compile(stb.toString())
    }

    internal fun makeReplacement(prefix: String): String {
        var pref = Objects.toString(prefix, "")
        if ("." == pref) {
            pref = "\\."
        }
        return "$pref([^\\/]+)"
    }

    override fun resolve(exchange: HttpServerExchange): Boolean {
        val m = this.template.matcher(exchange.relativePath)
        if (m.matches()) {
            val count = m.groupCount()
            if (count <= names.size) {
                val newone = HashMap<String, String>()
                for (i in 0..count - 1) {
                    newone.put(names[i], m.group(i + 1))
                }
                exchange.putAttachment(PARAMS, newone)
            }
            return true
        }
        return false
    }

    companion object {

        internal val LOG = Logger.getLogger(PathPredicate::class.java)

        @JvmField val PARAMS = AttachmentKey.create<Map<String, String>>(Map::class.java)

        internal val NAMED = Pattern.compile("\\(\\?\\<(?<name>\\w+)\\>[^)]+\\)")
        internal val SEGMENT = Pattern.compile("(?<prefix>[/\\.])?(?::(?<name>\\w+))")
    }

}
