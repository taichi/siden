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
package example

import java.nio.file.Paths
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.regex.Pattern

import ninja.siden.App
import ninja.siden.Renderer
import ninja.siden.RoutingCustomizer
import ninja.siden.react.React

import org.boon.Maps
import org.boon.json.JsonFactory

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory

/**
 * React.js Server Side Rendering Example on JVM.

 * @author taichi
 */
var id = 0
fun main(args: Array<String>) {

    // test data
    val comments = ArrayList<Map<String, Any>>()
    comments.add(Maps.map("id", ++id, "author",
            "Pete Hunt", "text", "This is one comment"))
    comments.add(Maps.map("id", ++id, "author",
            "Jordan Walke", "text", "This is *another* comment"))

    // setup template engine
    val mf = DefaultMustacheFactory()
    val template = mf.compile("assets/react.mustache")
    val renderer = Renderer.of { m: Any, w -> template.execute(w, m) }

    // setup react server side rendering
    val rc = React("CommentBox", "content", Arrays.asList(
            // https://github.com/paulmillr/console-polyfill
            // Nashorn doesn't contain console object.
            Paths.get("assets", "console-polyfill.js"),
            // http://facebook.github.io/react/
            Paths.get("assets", "react.js"),
            // https://github.com/showdownjs/showdown
            Paths.get("assets", "showdown.min.js"),
            // npm install -g react-tools
            // jsx --watch assets\src build
            Paths.get("build", "comments.js")))

    val app = App()
    app[Pattern.compile("/(index.html?)?"), { q, s ->
        val props = JsonFactory.toJson(Maps.map<String, Any>("initdata",
                comments, "url", "comments.json"))
        val model = HashMap<String, Any>()
        model.put("rendered", rc.toHtml(props))
        model.put("clientjs", rc.toClientJs(props))
        s.render<Any>(model, renderer)
    }].type("text/html")

    // JSON API
    json(app.get("/comments.json", { q, s -> comments }))
    json(app.post("/comments.json") { req, res ->
        val m = HashMap<String, Any>()
        m.put("id", ++id)
        req.form("author").ifPresent { s -> m.put("author", s) }
        req.form("text").ifPresent { s -> m.put("text", s) }
        comments.add(m)
        comments
    })

    app.assets("build")
    app.assets("/static", "assets")
    app.listen().addShutdownHook()
}

internal fun json(route: RoutingCustomizer) {
    route.render(Renderer.of { value: Any, appendable: Appendable -> JsonFactory.toJson(value, appendable) }).type("application/json")
}