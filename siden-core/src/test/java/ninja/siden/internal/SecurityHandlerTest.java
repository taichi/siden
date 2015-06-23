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

import static org.junit.Assert.assertNotNull;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import mockit.integration.junit4.JMockit;
import ninja.siden.Config;
import ninja.siden.SecurityHeaders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class SecurityHandlerTest {

	HttpServerExchange exchange;
	HttpHandler target;

	@Before
	public void setUp() {
		this.exchange = new HttpServerExchange(null);
		this.exchange.putAttachment(Core.CONFIG, Config.defaults().getMap());

		this.target = new SecurityHandler(Testing.mustCall());
	}

	void assertHeader(HttpString name) {
		assertNotNull(this.exchange.getResponseHeaders().get(name));
	}

	@Test
	public void testHeaders() throws Exception {
		this.target.handleRequest(this.exchange);
		assertHeader(SecurityHeaders.FRAME_OPTIONS);
		assertHeader(SecurityHeaders.XSS_PROTECTION);
		assertHeader(SecurityHeaders.CONTENT_TYPE_OPTIONS);
	}

}
