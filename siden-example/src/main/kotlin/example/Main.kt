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

import ninja.siden.*

import org.boon.json.JsonFactory

/**
 * @author taichi
 */
fun main(args: Array<String>) {
    val app = App()

    // simple get
    app.get("/hello", Route { req, res -> "Hello world !!" })

    // receive Ajax request only
    app.get("/ajax", Route { req, res -> "{ 'name' : 'ajax' }" }).match( { it.xhr })

    // simple logging filter
    app.use (Filter { req, res, chain ->
        System.out.printf("%s %s %n", req.method, req.path)
        chain.next()
    })

    // exception handling

    app.error(MyException::class.java, ExceptionalRoute { ex, req, res -> ex.extramessage() })

    app.get("/err",Route { req, res -> throw MyException() })

    // response code handling
    app.error(402, Route { req, res -> "Payment Required. Payment Required!!" })
    app.get("/402", Route { req, res -> 402 })
    app.get("/payment", Route { req, res -> res.status(402) })

    // json api on top of Boon JSON
    // see. https://github.com/boonproject/boon
    app.get("/users/:name", Route { req, res -> req.params("name").map( { User(it) }) })
            .render(Renderer.of { value: Any, appendable: Appendable -> JsonFactory.toJson(value, appendable) })
            .type("application/json")

    // use static resources
    // GET /javascripts/jquery.js
    // GET /style.css
    // GET /favicon.ico
    app.assets("/", "assets/")

    // GET /static/javascripts/jquery.js
    // GET /static/style.css
    // GET /static/favicon.ico
    app.assets("/static", "assets/")

    app.get("/", Route { req, res -> "Siden Example Application is running." })

    // sub application
    val sub = App()
    // GET /secret/admin
    sub.get("/admin", Route { req, res -> "I'm in secret area" })
    app.use("/secret", sub)

    app.listen().addShutdownHook()
}
class MyException : Exception() {

    fun extramessage(): String {
        return "MyException has come!!"
    }

    companion object {
        private val serialVersionUID = -2530468497731983302L
    }
}
