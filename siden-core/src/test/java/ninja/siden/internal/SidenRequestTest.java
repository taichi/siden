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
package ninja.siden.internal;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import ninja.siden.App;
import ninja.siden.Stoppable;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author taichi
 */
public class SidenRequestTest {

	static int port = 7000;

	App app;

	Stoppable stopper;

	@Before
	public void setUp() {
		this.app = new App();

	}

	@After
	public void tearDown() {
		this.stopper.stop();
		port++;
	}

	@Test
	public void largeStringBody() throws Exception {
		StringBuilder stb = new StringBuilder();
		for (int i = 0; i < 1500; i++) {
			stb.append("0123456789");
		}
		stringBody(stb.toString());
	}

	@Test
	public void stringBodyWithLF() throws Exception {
		StringBuilder stb = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			stb.append("abcd\r\n");
		}
		stringBody(stb.toString());
	}

	@Test
	public void stringBodySwithJapanese() throws Exception {
		stringBody("abcd～～wayway美豚");
	}

	@Test
	public void smallStringBody() throws Exception {
		stringBody("hoge");
	}

	@Test
	public void noBody() throws Exception {
		stringBody("");
	}

	public void stringBody(String body) throws Exception {
		CountDownLatch latch = new CountDownLatch(2);
		BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
		this.app.post("/string", (req, res) -> {
			String content = req.body();
			queue.add(content);
			latch.countDown();
			return content;
		});
		this.stopper = this.app.listen(port);

		HttpPost post = new HttpPost(String.format(
				"http://localhost:%d/string", port));
		post.setHeader("Content-Type", "application/string;charset=UTF-8");
		post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

		Testing.request(post, response -> {
			try {
				assertEquals(200, response.getStatusLine().getStatusCode());
				if (body.isEmpty() == false) {
					assertEquals(body, Testing.read(response));
				}
			} finally {
				latch.countDown();
			}
		});
		assertEquals(body, queue.poll());
		latch.await();
	}
}
