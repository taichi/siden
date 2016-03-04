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
import ninja.siden.react.*;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * ninja.siden.react.React.js Server Side Rendering Example on JVM.
 * 
 * @author taichi
 */
public class UseReactSSR {

	public static void main(String[] args) {
		// setup react server side rendering
		React rc = new React("HelloMessage", "content", Arrays.asList(
				// https://github.com/paulmillr/console-polyfill
				// Nashorn don't contain console object.
				Paths.get("assets", "console-polyfill.js"),
				// https://github.com/facebook/react
				Paths.get("assets", "react.js"),
				// npm install -g react-tools
				// jsx -x jsx assets build
				// siden-react don't support jsx compile.
				Paths.get("build", "hello.js")));

		App app = new App();
		app.get("/", (q, s) -> {
				// serialized json
				String props = "{\"name\":\"john\"}";
				// server side rendering
				return "<html><body>" + rc.toHtml(props) + "</body></html>";
			}).type("text/html");
		app.listen().addShutdownHook();
	}
}
