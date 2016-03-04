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

import ninja.siden.App
import ninja.siden.Stoppable
import org.apache.http.client.methods.HttpGet
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays

import org.junit.Assert.assertEquals

/**
 * @author taichi
 */
@RunWith(Parameterized::class)
class RendererSelectorTest(val name: String, internal var actual: Any, internal var port: Int) {

    lateinit var app: App

    var stopper: Stoppable? = null

    @Before
    fun setUp() {
        this.app = App()
    }

    @After
    fun tearDown() {
        this.stopper?.stop()
    }

    internal fun runServer(result: Any) {
        this.app.get("/renderer/$name", { req, res -> result })
        this.stopper = this.app.listen(port)
    }

    internal fun make(): HttpGet {
        return HttpGet("http://localhost:$port/renderer/$name")
    }

    @Throws(Exception::class)
    internal fun request() {
        Testing.request(make()) { response ->
            assertEquals(200, response.statusLine.statusCode.toLong())
            assertEquals("Hello", Testing.read(response))
        }
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        runServer(this.actual)
        request()
    }

    companion object {

        var port = 8000

        @Parameters(name = "{0}")
        @Throws(Exception::class)
        @JvmStatic fun parameters(): Iterable<Array<Any>> {
            return Arrays.asList(*arrayOf(
                    arrayOf("String", "Hello", port++),
                    arrayOf("File", tmp().toFile(), port++),
                    arrayOf<Any>("Path", tmp(), port++),
                    arrayOf("FileChannel", FileChannel.open(tmp()), port++),
                    arrayOf<Any>("byteArray", "Hello".toByteArray(), port++),
                    arrayOf<Any>("ByteBuffer", ByteBuffer.wrap("Hello".toByteArray()), port++),
                    arrayOf("URI", tmp().toUri(), port++),
                    arrayOf<Any>("URL", tmp().toUri().toURL(), port++),
                    arrayOf("Reader", Files.newBufferedReader(tmp()), port++),
                    arrayOf("InputStream", ByteArrayInputStream("Hello".toByteArray()), port++),
                    arrayOf<Any>("CharSequence", StringBuilder("Hello"), port++)))
        }

        @Throws(Exception::class)
        internal fun tmp(): Path {
            val path = Files.createTempFile(RendererSelectorTest::class.java.name, ".tmp")
            Files.write(path, "Hello".toByteArray())
            return path
        }
    }

}
