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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import ninja.siden.App;
import ninja.siden.Stoppable;

import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author taichi
 */
@RunWith(Parameterized.class)
public class RendererSelectorTest {

	App app;

	Stoppable stopper;

	@Before
	public void setUp() {
		this.app = new App();
	}

	@After
	public void tearDown() {
		this.stopper.stop();
	}

	Object actual;
	int port;

	public RendererSelectorTest(String name, Object data, int port) {
		this.actual = data;
		this.port = port;
	}

	@Parameters(name = "{0}")
	public static Iterable<Object[]> parameters() throws Exception {
		int port = 8000;
		return Arrays.asList(new Object[][] {
				{ "String", "Hello", port++ },
				{ "File", tmp().toFile(), port++ },
				{ "Path", tmp(), port++ },
				{ "FileChannel", FileChannel.open(tmp()), port++ },
				{ "byteArray", "Hello".getBytes(), port++ },
				{ "ByteBuffer", ByteBuffer.wrap("Hello".getBytes()), port++ },
				{ "URI", tmp().toUri(), port++ },
				{ "URL", tmp().toUri().toURL(), port++ },
				{ "Reader", Files.newBufferedReader(tmp()), port++ },
				{ "InputStream", new ByteArrayInputStream("Hello".getBytes()),
						port++ },
				{ "CharSequence", new StringBuilder("Hello"), port++ },

		});
	}

	void runServer(Object result) {
		this.app.get("/renderer", (req, res) -> result);
		this.stopper = this.app.listen(port);
	}

	HttpGet make() {
		return new HttpGet("http://localhost:" + port + "/renderer");
	}

	void request() throws Exception {
		Testing.request(make(), response -> {
			assertEquals(200, response.getStatusLine().getStatusCode());
			assertEquals("Hello", Testing.read(response));
		});
	}

	@Test
	public void test() throws Exception {
		runServer(this.actual);
		request();
	}

	static Path tmp() throws Exception {
		Path path = Files.createTempFile(RendererSelectorTest.class.getName(),
				".tmp");
		Files.write(path, "Hello".getBytes());
		return path;
	}

}
