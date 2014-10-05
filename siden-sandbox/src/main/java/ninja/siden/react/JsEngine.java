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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import ninja.siden.util.Io;
import ninja.siden.util.Supress;

/**
 * @author taichi
 */
public class JsEngine {

	final ScriptEngineManager manager;
	ScriptEngine engine;

	public JsEngine() {
		manager = new ScriptEngineManager();
	}

	ScriptEngine newEngine() {
		return manager.getEngineByName("nashorn");
	}

	public void initialize(List<Path> scripts) {
		this.engine = newEngine();
		eval("var global = this;");
		scripts.stream().map(
				p -> Io.using(() -> Files.newBufferedReader(p), engine::eval));
	}

	public Object eval(String script) {
		return Supress.get(() -> engine.eval(script));
	}
}
