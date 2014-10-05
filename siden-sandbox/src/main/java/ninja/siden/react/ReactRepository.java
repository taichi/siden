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
package ninja.siden.react;

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import ninja.siden.Renderer;
import ninja.siden.RendererRepository;

import org.boon.json.JsonFactory;

/**
 * @author taichi
 */
public class ReactRepository implements RendererRepository {

	final JsEngine engine = new JsEngine();

	public ReactRepository(List<Path> scripts) {
		engine.initialize(scripts);
	}

	@Override
	public Renderer find(String component) throws Exception {
		return new Renderer() {
			@Override
			public void render(Object model, HttpServerExchange sink)
					throws IOException {

				String props = JsonFactory.toJson(model);
				ReactComponent rc = new ReactComponent(engine, component,
						props, component + "-container");
				rc.toHtml();
				rc.toClientJs();

			}
		};
	}

}
