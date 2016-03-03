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

import io.undertow.util.StringWriteChannelListener
import io.undertow.websockets.client.WebSocketClient
import io.undertow.websockets.core.*
import mockit.integration.junit4.JMockit
import ninja.siden.App
import ninja.siden.Stoppable
import ninja.siden.WebSocketCustomizer
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.xnio.*
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * @author taichi
 */
@RunWith(JMockit::class)
class WebSocketTest {

    companion object {

        lateinit var worker: XnioWorker

        @BeforeClass
        @JvmStatic
        fun before() {
            val xnio = Xnio.getInstance(WebSocketTest::class.java.classLoader)
            worker = xnio.createWorker(OptionMap.builder().set(Options.WORKER_IO_THREADS, 2).set(Options.CONNECTION_HIGH_WATER, 1000000).set(Options.CONNECTION_LOW_WATER, 1000000).set(Options.WORKER_TASK_CORE_THREADS, 30).set(Options.WORKER_TASK_MAX_THREADS, 30).set(Options.TCP_NODELAY, true).set(Options.CORK, true).map)
        }

        @AfterClass
        @JvmStatic
        fun after() {
            worker.shutdown()
        }

        internal var counter = 2000
    }

    lateinit var app: App

    lateinit var stopper: Stoppable

    lateinit var buffer: Pool<ByteBuffer>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        this.app = App()
        this.buffer = ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 256, 256)
    }

    @After
    fun tearDown() {
        this.stopper.stop()
    }

    internal open inner class TestListener(var latch: CountDownLatch) : AbstractReceiveListener() {

        override fun onError(channel: WebSocketChannel, error: Throwable?) {
            super.onError(channel, error)
            error!!.printStackTrace()
            this.latch.countDown()
        }
    }

    internal abstract inner class TT @Throws(Exception::class)
    constructor() {
        var port = counter++

        init {
            run()
        }

        fun run() {
            val closelatch = CountDownLatch(2)
            server(app.websocket("/ws").onClose { c, buf -> closelatch.countDown() })
            stopper = app.listen(port)
            val latch = CountDownLatch(1)
            val channel = WebSocketClient.connectionBuilder(
                    worker,
                    buffer,
                    URI(String.format("http://localhost:%d/ws",
                            port))).connect().get()

            channel.addCloseTask { c -> closelatch.countDown() }
            channel.receiveSetter.set(listener(latch))
            channel.resumeReceives()
            try {
                assertion(channel, latch)
            } finally {
                if (channel.isOpen) {
                    channel.sendClose()
                    closelatch.await(1, TimeUnit.SECONDS)
                }
            }
        }

        internal abstract fun server(ws: WebSocketCustomizer)

        internal abstract fun listener(latch: CountDownLatch): TestListener

        internal abstract fun assertion(channel: WebSocketChannel, latch: CountDownLatch)
    }

    @Test
    @Throws(Exception::class)
    fun testString() {
        val queue = LinkedBlockingQueue<String>()
        object : TT() {
            override fun server(ws: WebSocketCustomizer) {
                ws.onText { con, str ->
                    con.send(str).get()
                    con.close()
                }
            }

            override fun listener(latch: CountDownLatch): TestListener {
                return object : TestListener(latch) {
                    @Throws(IOException::class)
                    override fun onFullTextMessage(channel: WebSocketChannel?,
                                                   message: BufferedTextMessage?) {
                        queue.add(message!!.data)
                    }
                }
            }

            @Throws(Exception::class)
            override fun assertion(channel: WebSocketChannel, latch: CountDownLatch) {
                StringWriteChannelListener("TestTest").setup(channel.send(WebSocketFrameType.TEXT))
                assertEquals("TestTest", queue.take())
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testByteBuffer() {
        val ref = AtomicReference<Array<ByteBuffer>>()
        object : TT() {
            override fun server(ws: WebSocketCustomizer) {
                ws.onBinary { con, bin ->
                    val buf = WebSockets.mergeBuffers(*bin)
                    con.send(buf)
                }
            }

            override fun listener(latch: CountDownLatch): TestListener {
                return object : TestListener(latch) {
                    @Throws(IOException::class)
                    override fun onFullBinaryMessage(
                            channel: WebSocketChannel?,
                            message: BufferedBinaryMessage?) {
                        ref.set(message!!.data.resource)
                        latch.countDown()
                    }
                }
            }

            @Throws(Exception::class)
            override fun assertion(channel: WebSocketChannel, latch: CountDownLatch) {
                StringWriteChannelListener("TestTest").setup(channel.send(WebSocketFrameType.BINARY))
                latch.await(3, TimeUnit.SECONDS)
                val buf = ByteBuffer.wrap("TestTest".toByteArray())
                assertEquals(buf, WebSockets.mergeBuffers(*ref.get()))
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testPong() {
        val ref = AtomicReference<Array<ByteBuffer>>()
        object : TT() {
            override fun server(ws: WebSocketCustomizer) {
                ws.onPong { con, bin ->
                    val buf = WebSockets.mergeBuffers(*bin)
                    con.send(buf)
                }
            }

            override fun listener(latch: CountDownLatch): TestListener {
                return object : TestListener(latch) {
                    @Throws(IOException::class)
                    override fun onFullBinaryMessage(
                            channel: WebSocketChannel?,
                            message: BufferedBinaryMessage?) {
                        ref.set(message!!.data.resource)
                        latch.countDown()
                    }
                }
            }

            @Throws(Exception::class)
            override fun assertion(channel: WebSocketChannel, latch: CountDownLatch) {
                StringWriteChannelListener("TestTest").setup(channel.send(WebSocketFrameType.PONG))
                latch.await(3, TimeUnit.SECONDS)
                val buf = ByteBuffer.wrap("TestTest".toByteArray())
                assertEquals(buf, WebSockets.mergeBuffers(*ref.get()))
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testPing() {
        val ref = AtomicReference<Array<ByteBuffer>>()
        object : TT() {
            override fun server(ws: WebSocketCustomizer) {
                ws.onPing { con, bin ->
                    val buf = WebSockets.mergeBuffers(*bin)
                    con.send(buf)
                }
            }

            override fun listener(latch: CountDownLatch): TestListener {
                return object : TestListener(latch) {
                    @Throws(IOException::class)
                    override fun onFullBinaryMessage(channel: WebSocketChannel?, message: BufferedBinaryMessage?) {
                        ref.set(message!!.data.resource)
                        latch.countDown()
                    }
                }
            }

            @Throws(Exception::class)
            override fun assertion(channel: WebSocketChannel, latch: CountDownLatch) {
                StringWriteChannelListener("TestTest").setup(channel.send(WebSocketFrameType.PING))
                latch.await(3, TimeUnit.SECONDS)
                val buf = ByteBuffer.wrap("TestTest".toByteArray())
                assertEquals(buf, WebSockets.mergeBuffers(*ref.get()))
            }
        }
    }
}
