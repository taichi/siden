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
import ninja.siden.Renderer;
import ninja.siden.Request;

import org.boon.json.JsonFactory;

/**
 * @author taichi
 */
public class Main {

	public static void main(String[] args) throws Exception {
		App app = new App();

		// simple get
		app.get("/hello", (req, res) -> "Hello world !!");

		// receive Ajax request only
		app.get("/ajax", (req, res) -> "{ 'name' : 'ajax' }").match(
				Request::xhr);

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
		app.get("/users/:name", (req, res) -> req.params("name").map(User::new))
				.render(Renderer.of(JsonFactory::toJson))
				.type("application/json");

		// use static resources
		// GET /javascripts/jquery.js
		// GET /style.css
		// GET /favicon.ico
		app.assets("/", "assets/");

		// GET /static/javascripts/jquery.js
		// GET /static/style.css
		// GET /static/favicon.ico
		app.assets("/static", "assets/");
		
		app.get("/", (req, res) -> "Siden Example Application is running.");

		// sub application
		App sub = new App();
		// GET /secret/admin
		sub.get("/admin", (req, res) -> "I'm in secret area");
		app.use("/secret", sub);

		Runtime.getRuntime().addShutdownHook(new Thread(app.listen()::stop));
	}
}
