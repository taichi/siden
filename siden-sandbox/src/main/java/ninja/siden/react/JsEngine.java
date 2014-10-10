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
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import ninja.siden.util.Io;
import ninja.siden.util.Loggers;
import ninja.siden.util.Supress;

/**
 * @author taichi
 */
public class JsEngine {
	
	static final Logger LOG = Loggers.from(JsEngine.class);

	final ScriptEngineManager manager;
	ScriptEngine engine;
	Bindings global;

	public JsEngine() {
		manager = new ScriptEngineManager();
	}

	ScriptEngine newEngine() {
		return manager.getEngineByExtension("js");
	}

	public void initialize(List<Path> scripts) {
		this.engine = newEngine();
		this.global = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		scripts.forEach(this::eval);
	}

	public Object eval(String script) {
		LOG.finest(() -> global.keySet().toString());
		return Supress.get(() -> engine.eval(script, global));
	}

	public Object eval(Path path) {
		LOG.finest(() -> global.keySet().toString());
		return Io.using(() -> Files.newBufferedReader(path), r -> {
			return engine.eval(r, global);
		});
	}
}
