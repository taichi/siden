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
package ninja.siden.internal

import ninja.siden.App
import ninja.siden.Route
import ninja.siden.Stoppable
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch

/**
 * @author taichi
 */
class SidenRequestTest {

    lateinit var app: App

    var stopper: Stoppable? = null

    @Before
    fun setUp() {
        this.app = App()

    }

    @After
    fun tearDown() {
        this.stopper?.stop()
        port++
    }

    @Test
    @Throws(Exception::class)
    fun largeStringBody() {
        val stb = StringBuilder()
        for (i in 0..1499) {
            stb.append("0123456789")
        }
        stringBody(stb.toString())
    }

    @Test
    @Throws(Exception::class)
    fun stringBodyWithLF() {
        val stb = StringBuilder()
        for (i in 0..99) {
            stb.append("abcd\r\n")
        }
        val s = stb.toString()
        val opt = stringBody(s)
        assertEquals(s, opt.get())
    }

    @Test
    @Throws(Exception::class)
    fun stringBodySwithJapanese() {
        val s = "abcd～～wayway美豚"
        val opt = stringBody(s)
        assertEquals(s, opt.get())
    }

    @Test
    @Throws(Exception::class)
    fun smallStringBody() {
        assertTrue(stringBody("hoge").isPresent)
    }

    @Test
    @Throws(Exception::class)
    fun noBody() {
        assertFalse(stringBody("").isPresent)
    }

    @Throws(Exception::class)
    fun stringBody(body: String): Optional<String> {
        val latch = CountDownLatch(2)
        val queue = ArrayBlockingQueue<Optional<String>>(1)
        this.app.post("/string", Route { req, res ->
            val content = req.body()
            queue.add(content)
            latch.countDown()
            content
        })
        this.stopper = this.app.listen(port=port)

        val post = HttpPost(String.format(
                "http://localhost:%d/string", port))
        post.setHeader("Content-Type", "application/string;charset=UTF-8")
        post.entity = StringEntity(body, StandardCharsets.UTF_8)

        Testing.request(post) { response ->
            try {
                assertEquals(200, response.statusLine.statusCode.toLong())
                if (body.isEmpty() == false) {
                    assertEquals(body, Testing.read(response))
                }
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        return queue.poll()
    }

    companion object {

        internal var port = 7000
    }
}
