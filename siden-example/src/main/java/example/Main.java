package example;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Renderer;
import ninja.siden.RendererRepository;
import ninja.siden.Request;

import org.boon.json.JsonFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

public class Main {

	public static void main(String[] args) throws Exception {
		App app = new App();

		// simple get
		app.get("/hello", (req, res) -> "Hello world !! "
				+ req.raw().getResponseCode());

		// receive Ajax request only
		app.get("/ajax", (req, res) -> "{ 'name' : 'ajax' }")
				.match(Request::xhr).type("application/json");

		// simple logging filter
		app.use((req, res, chain) -> {
			System.out.printf("%s %s %n", req.method(), req.path());
			chain.next();
		});

		// exception handling
		class MyException extends Exception {
			private static final long serialVersionUID = -2530468497731983302L;

			public String extramessage() {
				return "MyException has come!!";
			}
		}
		app.error(MyException.class, (ex, req, res) -> {
			return ex.extramessage();
		});

		app.get("/err", (req, res) -> {
			throw new MyException();
		});

		// response code handling
		app.error(402, (req, res) -> "Payment Required. Payment Required!!");
		app.get("/402", (req, res) -> 402);
		app.get("/payment", (req, res) -> res.status(402));

		// json api on top of Boon JSON 
		// see. https://github.com/boonproject/boon
		app.get("/users", (req, res) -> new User("john"))
				.render(Renderer.of(JsonFactory::toJson))
				.type("application/json");

		// use static resources
		// GET /javascripts/jquery.js
		// GET /style.css
		// GET /favicon.ico
		app.assets("assets/");

		// GET /static/javascripts/jquery.js
		// GET /static/style.css
		// GET /static/favicon.ico
		app.assets("/static", "assets/");

		// sub application
		App sub = new App();
		// GET /secret/admin
		sub.get("/admin", (req, res) -> "I'm in secret area");
		app.use("/secret", sub);

		// use template engines
		// see. https://github.com/jknack/handlebars.java
		Handlebars engine = new Handlebars();
		Template t = engine.compileInline("Hello {{this}}!");

		app.get("/bars",
				(req, res) -> res.render("heyhey", Renderer.of(t::apply)));

		class HandleBarsRepo implements RendererRepository {
			final Handlebars engine;

			public HandleBarsRepo() {
				TemplateLoader loader = new ClassPathTemplateLoader();
				loader.setPrefix("/templates");
				loader.setSuffix(".html");
				engine = new Handlebars(loader);
			}

			@Override
			public Renderer find(String path) throws Exception {
				Template t = engine.compile(path);
				return Renderer.of(t::apply);
			}
		}

		App hbapp = App.configure(conf -> {
			conf.set(Config.RENDERER_REPOSITORY, new HandleBarsRepo());
			return conf;
		});
		hbapp.get("/handlebars", // read template from templates/say/hello.html
				(req, res) -> res.render(new User("peter"), "say/hello"));
		app.use("/hbs", hbapp);

		app.listen(8080);
	}
}
