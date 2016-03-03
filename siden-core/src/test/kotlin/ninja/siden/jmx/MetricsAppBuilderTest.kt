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
package ninja.siden.jmx

import ninja.siden.App
import ninja.siden.Config
import ninja.siden.Stoppable
import ninja.siden.internal.Testing
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectName

/**
 * @author taichi
 */
class MetricsAppBuilderTest {

    lateinit var server: MBeanServer
    lateinit var target: App
    lateinit var stopper: Stoppable

    @Before
    fun setUp() {
        this.server = ManagementFactory.getPlatformMBeanServer()
        this.target = App.configure { b -> b.set(Config.ENV, "prod") }
    }

    @After
    fun tearDown() {
        this.stopper.stop()
    }

    protected fun listen() {
        this.stopper = this.target.listen(port++)
    }

    @Test
    fun session() {
        this.listen()

        val on = ObjectName("ninja.siden:type=Session")
        val info = server.getMBeanInfo(on)
        val attr = info.attributes[0]
        assertEquals("Metrics", attr.name)
    }

    @Test
    fun global() {
        this.listen()

        val on = ObjectName("ninja.siden:type=Request,name=Global")
        val info = server.getMBeanInfo(on)
        val attr = info.attributes[0]
        assertEquals("Metrics", attr.name)
    }

    @Test
    fun routes() {
        target["/aaa", { req, res -> "abc" }]
        this.listen()

        val on = ObjectName(
                "ninja.siden:type=Request,path=\"/aaa\",method=GET")
        val info = server.getMBeanInfo(on)
        val attr = info.attributes[0]
        assertEquals("Metrics", attr.name)
    }

    @Test
    fun nestedRoutes() {
        val sub = App()
        sub.head("/def") { req, res -> "def" }
        target.use("/abc", sub)
        this.listen()
        val abc = ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD")
        server.getMBeanInfo(abc)
    }

    @Test
    fun nestedRoutesTwoTimes() {
        val sub = App()
        sub.head("/def") { req, res -> "def" }
        target.use("/abc", sub)
        target.use("/efg", sub)
        this.listen()

        val abc = ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD")
        server.getMBeanInfo(abc)

        val efg = ObjectName(
                "ninja.siden:type=Request,path=\"/efg/def\",method=HEAD")
        server.getMBeanInfo(efg)
    }

    @Test
    fun deeplyNestedRoutes() {
        val subsub = App()
        subsub["/jkl", { req, res -> "eee" }]
        val sub = App()
        sub.head("/def") { req, res -> "def" }
        sub.use("/ghi", subsub)

        target.use("/abc", sub)
        this.listen()

        val def = ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD")
        server.getMBeanInfo(def)

        val jkl = ObjectName(
                "ninja.siden:type=Request,path=\"/abc/ghi/jkl\",method=GET")
        server.getMBeanInfo(jkl)
    }

    @Test
    fun websockets() {
        val sub = App()
        sub.websocket("/ws").onText { c, s -> c.send(s) }
        target.use("/aaa", sub)
        this.listen()

        val ws = ObjectName(
                "ninja.siden:type=WebSocket,path=\"/aaa/ws\"")
        server.getMBeanInfo(ws)
    }

    companion object {

        @BeforeClass
        fun beforeClass() {
            Testing.useALL(App::class.java)
        }

        internal var port = 9000
    }

}
