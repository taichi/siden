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
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.util.Optional;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import ninja.siden.Config;
import ninja.siden.Renderer;
import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class RoutingHandlerTest {

	HttpServerExchange exchange;

	@Before
	public void setUp() {
		this.exchange = new HttpServerExchange(null);
	}

	@Test
	public void testNotMatch() throws Exception {
		RoutingHandler target = new RoutingHandler(Testing.mustCall());
		target.add(Predicates.falsePredicate(), (q, s) -> "");
		target.handleRequest(this.exchange);
	}

	@Test
	public void testSimpleMatch() throws Exception {
		RoutingHandler target = new RoutingHandler(Testing.empty());
		target.add(Predicates.truePredicate(), (q, s) -> "Hello").render(
				new MockUp<Renderer<Object>>() {
					@Mock(invocations = 1)
					void render(Object model, HttpServerExchange sink)
							throws IOException {
						assertEquals("Hello", model);
					}
				}.getMockInstance());
		target.handleRequest(this.exchange);
	}

	@Test
	public void testReturnOptinal() throws Exception {
		RoutingHandler target = new RoutingHandler(Testing.empty());
		target.add(Predicates.truePredicate(), (q, s) -> Optional.of("Hello"))
				.render(new MockUp<Renderer<Object>>() {
					@Mock(invocations = 1)
					void render(Object model, HttpServerExchange sink)
							throws IOException {
						assertEquals("Hello", model);
					}
				}.getMockInstance());
		target.handleRequest(this.exchange);
	}

	@Test
	public void testReturnStatusCode() throws Exception {
		RoutingHandler target = new RoutingHandler(Testing.empty());
		target.add(400, new MockUp<Route>() {
			@Mock(invocations = 1)
			Object handle(Request request, Response response) throws Exception {
				return "Hey";
			}
		}.getMockInstance()).render(new MockUp<Renderer<Object>>() {
			@Mock(invocations = 1)
			void render(Object model, HttpServerExchange sink)
					throws IOException {
				assertEquals("Hey", model);
			}
		}.getMockInstance());

		target.add(Predicates.truePredicate(), (q, s) -> 400).render(
				new MockUp<Renderer<Object>>() {
					@Mock(invocations = 0)
					void render(Object model, HttpServerExchange sink)
							throws IOException {
					}
				}.getMockInstance());

		target.handleRequest(this.exchange);
	}

	@Test
	public void testSetStatusCode() throws Exception {
		RoutingHandler target = new RoutingHandler(Testing.empty());
		target.add(402, new MockUp<Route>() {
			@Mock(invocations = 1)
			Object handle(Request request, Response response) throws Exception {
				return "Hey";
			}
		}.getMockInstance()).render(new MockUp<Renderer<Object>>() {
			@Mock(invocations = 1)
			void render(Object model, HttpServerExchange sink)
					throws IOException {
				assertEquals("Hey", model);
			}
		}.getMockInstance());

		this.exchange.putAttachment(Core.CONFIG, Config.defaults().getMap());
		this.exchange.putAttachment(Core.RESPONSE, new SidenResponse(
				this.exchange));

		target.add(Predicates.truePredicate(), (q, s) -> s.status(402)).render(
				new MockUp<Renderer<Object>>() {
					@Mock(invocations = 0)
					void render(Object model, HttpServerExchange sink)
							throws IOException {
					}
				}.getMockInstance());

		target.handleRequest(this.exchange);
		assertEquals(402, this.exchange.getResponseCode());
	}

	@Test
	public void testEspeciallyPkgsAreNotRender() throws Exception {
		this.exchange.putAttachment(Core.CONFIG, Config.defaults().getMap());
		this.exchange.putAttachment(Core.RESPONSE, new SidenResponse(
				this.exchange));

		RoutingHandler target = new RoutingHandler(Testing.empty());
		target.add(Predicates.truePredicate(),
				(q, s) -> s.cookie("hoge", "fuga")).render(
				new MockUp<Renderer<Object>>() {
					@Mock(invocations = 0)
					void render(Object model, HttpServerExchange sink)
							throws IOException {
					}
				}.getMockInstance());

		target.handleRequest(this.exchange);
	}

	static class MyIoException extends IOException {
		private static final long serialVersionUID = 1L;
	}

	@Test
	public void testExceptionalRouting() throws Exception {
		this.exchange = new MockUp<HttpServerExchange>() {
			@Mock
			public boolean isRequestChannelAvailable() {
				return true;
			}
		}.getMockInstance();
		RoutingHandler target = new RoutingHandler(Testing.empty());

		target.add(IOException.class, (ex, res, req) -> {
			assertEquals(MyIoException.class, ex.getClass());
			return "Hey";
		}).render(new MockUp<Renderer<Object>>() {
			@Mock(invocations = 1)
			void render(Object model, HttpServerExchange sink)
					throws IOException {
				assertEquals("Hey", model);
			}
		}.getMockInstance());

		target.add(Predicates.truePredicate(), (q, s) -> {
			throw new MyIoException();
		}).render(new MockUp<Renderer<Object>>() {
			@Mock(invocations = 0)
			void render(Object model, HttpServerExchange sink)
					throws IOException {
			}
		}.getMockInstance());

		target.handleRequest(this.exchange);

		assertEquals(500, this.exchange.getResponseCode());
	}
}
