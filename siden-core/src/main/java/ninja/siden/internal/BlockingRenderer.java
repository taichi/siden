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

import io.undertow.server.HttpServerExchange;

import java.io.IOException;

import ninja.siden.Renderer;

/**
 * @author taichi
 */
public class BlockingRenderer implements Renderer {

	final Renderer renderer;

	public BlockingRenderer(Renderer renderer) {
		super();
		this.renderer = renderer;
	}

	@Override
	public void render(Object model, HttpServerExchange sink)
			throws IOException {
		sink.startBlocking();
		if (sink.isInIoThread()) {
			sink.dispatch(exchange -> {
				renderer.render(model, exchange);
			});
		} else {
			renderer.render(model, sink);
		}
	}
}
