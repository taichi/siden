package ninja.siden.react/*
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

import java.nio.file.Path

/**
 * @author taichi
 */
class React(internal val name: String, internal val containerId: String, scripts: List<Path>) {

    internal val engine = JsEngine()

    init {
        this.engine.initialize(scripts)
    }

    internal fun makeScript(encodedProps: String): String {
        val stb = StringBuilder()
        stb.append("ninja.siden.react.React.renderToString(")
        appendInitializer(stb, encodedProps)
        stb.append(")")
        return stb.toString()
    }

    fun toHtml(encodedProps: String): StringBuilder {
        val stb = StringBuilder()
        stb.append("<div id=\"")
        stb.append(this.containerId)
        stb.append("\">")
        stb.append(this.engine.eval(makeScript(encodedProps)))
        stb.append("</div>")
        return stb
    }

    fun toClientJs(encodedProps: String): StringBuilder {
        val stb = StringBuilder()
        stb.append("ninja.siden.react.React.render(")
        appendInitializer(stb, encodedProps)
        stb.append(", document.getElementById(")
        stb.append("\"")
        stb.append(this.containerId)
        stb.append("\"));")
        return stb
    }

    internal fun appendInitializer(a: StringBuilder, encodedProps: String) {
        a.append(this.name)
        a.append('(')
        a.append(encodedProps)
        a.append(')')
    }
}
