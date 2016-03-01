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
package ninja.siden.internal;

import io.undertow.util.StringWriteChannelListener;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import mockit.integration.junit4.JMockit;
import ninja.siden.App;
import ninja.siden.Stoppable;
import ninja.siden.WebSocketCustomizer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.xnio.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class WebSocketTest {

    App app;

    Stoppable stopper;

    static XnioWorker worker;

    Pool<ByteBuffer> buffer;

    @BeforeClass
    public static void before() throws Exception {
        Xnio xnio = Xnio.getInstance(WebSocketTest.class.getClassLoader());
        worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true).set(Options.CORK, true)
                .getMap());
    }

    @AfterClass
    public static void after() throws Exception {
        worker.shutdown();
    }

    @Before
    public void setUp() throws Exception {
        this.app = new App();
        this.buffer = new ByteBufferSlicePool(
                BufferAllocator.BYTE_BUFFER_ALLOCATOR, 256, 256);
    }

    @After
    public void tearDown() {
        this.stopper.stop();
    }

    class TestListener extends AbstractReceiveListener {
        CountDownLatch latch;

        public TestListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        protected void onError(WebSocketChannel channel, Throwable error) {
            super.onError(channel, error);
            error.printStackTrace();
            this.latch.countDown();
        }
    }

    static int counter = 2000;

    abstract class TT {
        int port = counter++;

        TT() throws Exception {
            run();
        }

        void run() throws Exception {
            CountDownLatch closelatch = new CountDownLatch(2);
            server(app.websocket("/ws").onClose(
                    (c, buf) -> closelatch.countDown()));
            stopper = app.listen(port);
            CountDownLatch latch = new CountDownLatch(1);
            WebSocketChannel channel = WebSocketClient
                    .connectionBuilder(
                            worker,
                            buffer,
                            new URI(String.format("http://localhost:%d/ws",
                                    port))).connect().get();

            channel.addCloseTask(c -> closelatch.countDown());
            channel.getReceiveSetter().set(listener(latch));
            channel.resumeReceives();
            try {
                assertion(channel, latch);
            } finally {
                if (channel.isOpen()) {
                    channel.sendClose();
                    closelatch.await(1, TimeUnit.SECONDS);
                }
            }
        }

        abstract void server(WebSocketCustomizer ws);

        abstract TestListener listener(CountDownLatch latch);

        abstract void assertion(WebSocketChannel channel, CountDownLatch latch)
                throws Exception;
    }

    @Test
    public void testString() throws Exception {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        new TT() {
            @Override
            void server(WebSocketCustomizer ws) {
                ws.onText((con, str) -> {
                    con.send(str).get();
                    con.close();
                });
            }

            @Override
            TestListener listener(CountDownLatch latch) {
                return new TestListener(latch) {
                    @Override
                    protected void onFullTextMessage(WebSocketChannel channel,
                                                     BufferedTextMessage message) throws IOException {
                        queue.add(message.getData());
                    }
                };
            }

            @Override
            void assertion(WebSocketChannel channel, CountDownLatch latch)
                    throws Exception {
                new StringWriteChannelListener("TestTest").setup(channel
                        .send(WebSocketFrameType.TEXT));
                assertEquals("TestTest", queue.take());
            }
        };
    }

    @Test
    public void testByteBuffer() throws Exception {
        AtomicReference<ByteBuffer[]> ref = new AtomicReference<>();
        new TT() {
            @Override
            void server(WebSocketCustomizer ws) {
                ws.onBinary((con, bin) -> {
                    ByteBuffer buf = WebSockets.mergeBuffers(bin);
                    con.send(buf);
                });
            }

            @Override
            TestListener listener(CountDownLatch latch) {
                return new TestListener(latch) {
                    @Override
                    protected void onFullBinaryMessage(
                            WebSocketChannel channel,
                            BufferedBinaryMessage message) throws IOException {
                        ref.set(message.getData().getResource());
                        latch.countDown();
                    }
                };
            }

            @Override
            void assertion(WebSocketChannel channel, CountDownLatch latch)
                    throws Exception {
                new StringWriteChannelListener("TestTest").setup(channel
                        .send(WebSocketFrameType.BINARY));
                latch.await(3, TimeUnit.SECONDS);
                ByteBuffer buf = ByteBuffer.wrap("TestTest".getBytes());
                assertEquals(buf, WebSockets.mergeBuffers(ref.get()));
            }
        };
    }

    @Test
    public void testPong() throws Exception {
        AtomicReference<ByteBuffer[]> ref = new AtomicReference<>();
        new TT() {
            @Override
            void server(WebSocketCustomizer ws) {
                ws.onPong((con, bin) -> {
                    ByteBuffer buf = WebSockets.mergeBuffers(bin);
                    con.send(buf);
                });
            }

            @Override
            TestListener listener(CountDownLatch latch) {
                return new TestListener(latch) {
                    @Override
                    protected void onFullBinaryMessage(
                            WebSocketChannel channel,
                            BufferedBinaryMessage message) throws IOException {
                        ref.set(message.getData().getResource());
                        latch.countDown();
                    }
                };
            }

            @Override
            void assertion(WebSocketChannel channel, CountDownLatch latch)
                    throws Exception {
                new StringWriteChannelListener("TestTest").setup(channel
                        .send(WebSocketFrameType.PONG));
                latch.await(3, TimeUnit.SECONDS);
                ByteBuffer buf = ByteBuffer.wrap("TestTest".getBytes());
                assertEquals(buf, WebSockets.mergeBuffers(ref.get()));
            }
        };
    }

    @Test
    public void testPing() throws Exception {
        AtomicReference<ByteBuffer[]> ref = new AtomicReference<>();
        new TT() {
            @Override
            void server(WebSocketCustomizer ws) {
                ws.onPing((con, bin) -> {
                    ByteBuffer buf = WebSockets.mergeBuffers(bin);
                    con.send(buf);
                });
            }

            @Override
            TestListener listener(CountDownLatch latch) {
                return new TestListener(latch) {
                    @Override
                    protected void onFullBinaryMessage(
                            WebSocketChannel channel,
                            BufferedBinaryMessage message) throws IOException {
                        ref.set(message.getData().getResource());
                        latch.countDown();
                    }
                };
            }

            @Override
            void assertion(WebSocketChannel channel, CountDownLatch latch)
                    throws Exception {
                new StringWriteChannelListener("TestTest").setup(channel
                        .send(WebSocketFrameType.PING));
                latch.await(3, TimeUnit.SECONDS);
                ByteBuffer buf = ByteBuffer.wrap("TestTest".getBytes());
                assertEquals(buf, WebSockets.mergeBuffers(ref.get()));
            }
        };
    }
}
