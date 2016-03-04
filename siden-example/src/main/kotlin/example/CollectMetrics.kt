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
package example

import ninja.siden.App
import ninja.siden.Config

fun main(args: Array<String>) {
    // development environments don't need metrics.
    val app = App.configure { b -> b.set(Config.ENV, "stable") }

    app.get("/", { req, res -> "hello" })

    val sub = App()
    sub.get("/hoi", { req, res -> "HOIHOI" })
    sub.websocket("/ws").onText { c, s -> c.send(s) }

    app.use("/aaa", sub)
    app.use("/bbb", sub)

    app.listen().addShutdownHook()
}