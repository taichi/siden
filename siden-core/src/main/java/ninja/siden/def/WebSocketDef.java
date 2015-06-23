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
import io.undertow.predicate.PredicatesHandler;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import ninja.siden.WebSocketFactory;
import ninja.siden.internal.ConnectionCallback;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class WebSocketDef {

	final String template;
	final Predicate predicate;
	final WebSocketFactory factory;

	public WebSocketDef(String template, Predicate predicate,
			WebSocketFactory factory) {
		super();
		this.template = template;
		this.predicate = predicate;
		this.factory = factory;
	}

	public String template() {
		return this.template;
	}

	public Predicate predicate() {
		return this.predicate;
	}

	public WebSocketFactory factory() {
		return this.factory;
	}

	public void addTo(PredicatesHandler ph, OptionMap config) {
		ph.addPredicatedHandler(this.predicate(),
				next -> new WebSocketProtocolHandshakeHandler(
						new ConnectionCallback(this.factory()), next));
	}
}
