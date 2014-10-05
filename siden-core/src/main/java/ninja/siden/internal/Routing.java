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

import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import ninja.siden.Renderer;
import ninja.siden.Request;
import ninja.siden.Route;
import ninja.siden.RoutingCustomizer;

/**
 * @author taichi
 */
public class Routing implements RoutingCustomizer {
	Predicate predicate;
	Route route;
	Renderer renderer;

	public Routing(Predicate predicate, Route route, Renderer renderer) {
		this.predicate = predicate;
		this.route = route;
		this.renderer = renderer;
	}

	@Override
	public RoutingCustomizer type(String type) {
		predicate = Predicates.and(this.predicate, MIMEPredicate.accept(type));
		Route prev = this.route;
		route = (req, res) -> {
			Object result = prev.handle(req, res);
			res.type(type);
			return result;
		};
		return this;
	}

	@Override
	public RoutingCustomizer accept(String type) {
		predicate = Predicates.and(this.predicate,
				MIMEPredicate.contentType(type));
		return this;
	}

	@Override
	public RoutingCustomizer match(java.util.function.Predicate<Request> fn) {
		predicate = Predicates.and(this.predicate, exchange -> {
			Request request = exchange.getAttachment(Core.REQUEST);
			return fn.test(request);
		});
		return this;
	}

	@Override
	public RoutingCustomizer render(Renderer renderer) {
		this.renderer = renderer;
		return this;
	}

}