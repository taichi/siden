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

import ninja.siden.util.Suppress;
import ninja.siden.util.Using;
import org.jboss.logging.Logger;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author taichi
 */
public class JsEngine {

	static final Logger LOG = Logger.getLogger(JsEngine.class);

	final ScriptEngineManager manager;

	public JsEngine() {
		manager = new ScriptEngineManager();
	}

	ScriptEngine newEngine() {
		return manager.getEngineByExtension("js");
	}

	public void initialize(List<Path> scripts) {
		ScriptEngine se = newEngine();
		Suppress.get(() -> se.eval("var global = this;"));
		scripts.forEach(p -> eval(se, p));
		this.manager.setBindings(se.getBindings(ScriptContext.ENGINE_SCOPE));
	}

	public Object eval(String script) {
		LOG.debug(manager.getBindings().keySet());
		ScriptEngine engine = newEngine();
		return Suppress.get(() -> engine.eval(script));
	}

	public Object eval(Path path) {
		LOG.debug(manager.getBindings().keySet());
		return eval(newEngine(), path);
	}

	Object eval(ScriptEngine engine, Path path) {
		return Using.transform(() -> Files.newBufferedReader(path),
				engine::eval);
	}
}
