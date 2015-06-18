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
package example;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Renderer;
import ninja.siden.RendererRepository;
import ninja.siden.util.Suppress;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

/**
 * @author taichi
 */
public class UseHandlebars {

	public static void main(String[] args) throws Exception {
		App app = App.configure(conf -> {
			conf.set(Config.RENDERER_REPOSITORY, new HandleBarsRepo());
			return conf;
		});

		// see. https://github.com/jknack/handlebars.java
		Handlebars engine = new Handlebars();
		Template t = engine.compileInline("Hello {{this}}!");

		// use handlebars simply
		app.get("/bars",
				(req, res) -> res.render("john", Renderer.of(t::apply)));

		// read template from templates/say/hello.html
		app.get("/hello",
				(req, res) -> res.render(new User("peter"), "say/hello"));
		
		app.listen().addShutdownHook();
	}

	static class HandleBarsRepo implements RendererRepository {
		final Handlebars engine;

		public HandleBarsRepo() {
			TemplateLoader loader = new ClassPathTemplateLoader();
			loader.setPrefix("/templates");
			loader.setSuffix(".html");
			engine = new Handlebars(loader);
		}

		@Override
		public <T> Renderer<T> find(String path) {
			Template t = Suppress.get(() -> engine.compile(path));
			return Renderer.of(t::apply);
		}
	}

}
