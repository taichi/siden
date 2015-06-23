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
package ninja.siden.def;

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;

import java.util.Objects;

import ninja.siden.Filter;
import ninja.siden.FilterChain;
import ninja.siden.Request;
import ninja.siden.Response;

/**
 * @author taichi
 */
public class FilterDef implements Predicate, Filter {

	final Predicate predicate;

	final Filter filter;

	public FilterDef(Predicate predicate, Filter filter) {
		this.predicate = Objects.requireNonNull(predicate);
		this.filter = Objects.requireNonNull(filter);
	}

	@Override
	public boolean resolve(HttpServerExchange value) {
		return this.predicate.resolve(value);
	}

	@Override
	public void filter(Request req, Response res, FilterChain chain)
			throws Exception {
		this.filter.filter(req, res, chain);
	}
}
