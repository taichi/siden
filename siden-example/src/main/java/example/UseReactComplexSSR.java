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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ninja.siden.App;
import ninja.siden.Renderer;
import ninja.siden.RoutingCustomizer;
import ninja.siden.react.React;

import org.boon.Maps;
import org.boon.json.JsonFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * React.js Server Side Rendering Example on JVM.
 * 
 * @author taichi
 */
public class UseReactComplexSSR {
	static int id = 0;

	public static void main(String[] args) {
		// test data
		List<Map<String, Object>> comments = new ArrayList<>();
		comments.add(Maps.map("id", ++id, "author",
				"Pete Hunt", "text", "This is one comment"));
		comments.add(Maps.map("id", ++id, "author",
				"Jordan Walke", "text", "This is *another* comment"));

		// setup template engine
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache template = mf.compile("assets/react.mustache");
		Renderer renderer = Renderer.of((m, w) -> template.execute(w, m));

		// setup react server side rendering
		React rc = new React("CommentBox", "content", Arrays.asList(
		// https://github.com/paulmillr/console-polyfill
		// Nashorn doesn't contain console object.
				Paths.get("assets", "console-polyfill.js"),
				// http://facebook.github.io/react/
				Paths.get("assets", "react.js"),
				// https://github.com/showdownjs/showdown
				Paths.get("assets", "showdown.min.js"),
				// npm install -g react-tools
				// jsx --watch assets\src build
				Paths.get("build", "comments.js")));

		App app = new App();
		app.get(Pattern.compile("/(index.html?)?"),
				(q, s) -> {
					String props = JsonFactory.toJson(Maps.map("initdata",
							comments, "url", "comments.json"));
					Map<String, Object> model = new HashMap<>();
					model.put("rendered", rc.toHtml(props));
					model.put("clientjs", rc.toClientJs(props));
					return s.render(model, renderer);
				}).type("text/html");

		// JSON API
		json(app.get("/comments.json", (q, s) -> comments));
		json(app.post("/comments.json", (req, res) -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", ++id);
			req.form("author").ifPresent(s -> m.put("author", s));
			req.form("text").ifPresent(s -> m.put("text", s));
			comments.add(m);
			return comments;
		}));

		app.assets("build");
		app.assets("/static", "assets");
		app.listen();
	}

	static void json(RoutingCustomizer route) {
		route.render(Renderer.of(JsonFactory::toJson)).type("application/json");
	}
}
