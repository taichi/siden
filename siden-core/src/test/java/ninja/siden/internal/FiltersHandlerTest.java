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

import static org.junit.Assert.assertTrue;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpServerExchange;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import ninja.siden.Filter;
import ninja.siden.FilterChain;
import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.def.FilterDef;
import ninja.siden.internal.FiltersHandler.SimpleChain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class FiltersHandlerTest {

	HttpServerExchange exchange;

	@Mocked
	Request request;

	@Mocked
	Response response;

	@Before
	public void setUp() {
		this.exchange = new HttpServerExchange(null);
		this.exchange.putAttachment(Core.REQUEST, this.request);
		this.exchange.putAttachment(Core.RESPONSE, this.response);
	}

	@Test
	public void testNoFilter() throws Exception {
		new MockUp<SimpleChain>() {
			@Mock(invocations = 0)
			public Object next() throws Exception {
				return null;
			}
		};
		boolean[] is = { false };
		FiltersHandler target = new FiltersHandler(exc -> {
			is[0] = true;
		});
		target.handleRequest(this.exchange);
		assertTrue(is[0]);
	}

	@Test
	public void testSimpleCall() throws Exception {
		Filter filter = new MockUp<Filter>() {
			@Mock(invocations = 2)
			public void filter(Request req, Response res, FilterChain chain)
					throws Exception {
				chain.next();
			}
		}.getMockInstance();

		FiltersHandler target = new FiltersHandler(Testing.mustCall());
		target.add(new FilterDef(Predicates.truePredicate(), filter));
		target.add(new FilterDef(Predicates.falsePredicate(),
				(req, res, ch) -> {
					throw new AssertionError();
				}));
		target.add(new FilterDef(Predicates.truePredicate(), filter));

		target.handleRequest(this.exchange);
	}
}
