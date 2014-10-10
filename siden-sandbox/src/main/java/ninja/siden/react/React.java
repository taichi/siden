package ninja.siden.react;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ninja.siden.App;
import ninja.siden.Renderer;
import ninja.siden.RoutingCustomizer;

import org.boon.Lists;
import org.boon.Maps;
import org.boon.json.JsonFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;

public class React {
	static int id = 0;

	public static void main(String[] args) {
		List<Map<String, String>> comments = Lists.list(Maps.map("id",
				String.valueOf(++id), "author", "Pete Hunt", "text",
				"This is one comment"), Maps.map("id", String.valueOf(++id),
				"author", "Jordan Walke", "text", "This is *another* comment"));

		JsEngine engine = new JsEngine();
		engine.initialize(Lists.list(
				Paths.get("assets", "shims.js"),
				Paths.get("assets", "react.js"),
				Paths.get("assets", "showdown.min.js"),
				Paths.get("build", "tutorial1.js")));

		Handlebars handlebars = new Handlebars(new FileTemplateLoader("assets"));

		App app = new App();
		app.get(Pattern.compile("/(index.html?)?"),
				(q, s) -> {
					String props = JsonFactory.toJson(Maps.map("initdata",
							comments, "url", "comments.json", "pollInterval",
							2000));
					ReactComponent rc = new ReactComponent(engine,
							"CommentBox", props, "content");
					Template t = handlebars.compile("index");
					String html = rc.toHtml().toString();
					System.out.println(html);
					return s.render(
							Maps.map("rendered", rc.toHtml(), "clientjs",
									rc.toClientJs()), Renderer.of(t::apply));
					// return Paths.get("assets", "index.html");
				}).type("text/html");
		json(app.get("/comments.json", (q, s) -> comments));
		json(app.post("/comments.json", (req, res) -> {
			Map<String, String> m = new HashMap<>();
			m.put("id", String.valueOf(++id));
			req.form("author").ifPresent(s -> m.put("author", s));
			req.form("text").ifPresent(s -> m.put("text", s));
			comments.add(m);
			return comments;
		}));

		app.assets("/build", "build");
		app.assets("/static", "assets");
		app.listen();
	}

	static void json(RoutingCustomizer route) {
		route.render(Renderer.of(JsonFactory::toJson)).type("application/json");
	}
	// MEMO
	// npm install -g react-tools
	// jsx --watch assets\src build

	// TODO
	// http://facebook.github.io/react/
	// https://github.com/facebook/react/wiki/Complementary-Tools
	// http://cdnjs.com/libraries/react/
	// https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi

	// https://github.com/BinaryMuse/fluxxor
	// https://github.com/webpack/react-webpack-server-side-example
	// https://github.com/webpack/react-starter

	// Similar projects
	// https://github.com/KnisterPeter/jreact/blob/master/src/main/java/de/matrixweb/jreact/JReact.java

	// Overruns
	// https://github.com/yahoo/flux-examples
}
