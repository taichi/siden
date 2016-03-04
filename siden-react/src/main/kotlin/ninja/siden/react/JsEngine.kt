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
package ninja.siden.react

import org.jboss.logging.Logger

import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author taichi
 */
class JsEngine {

    internal val manager = ScriptEngineManager()

    internal fun newEngine(): ScriptEngine {
        return manager.getEngineByExtension("js")
    }

    fun initialize(scripts: List<Path>) {
        val se = newEngine()
        se.eval("var global = this;")
        scripts.forEach { p -> eval(se, p) }
        this.manager.bindings = se.getBindings(ScriptContext.ENGINE_SCOPE)
    }

    fun eval(script: String): Any {
        LOG.debug(manager.bindings.keys)
        val engine = newEngine()
        return engine.eval(script)
    }

    fun eval(path: Path): Any {
        LOG.debug(manager.bindings.keys)
        return eval(newEngine(), path)
    }

    internal fun eval(engine: ScriptEngine, path: Path): Any {
        return Files.newBufferedReader(path).use {
            engine.eval(it)
        }
    }

    companion object {
        internal val LOG = Logger.getLogger(JsEngine::class.java)
    }
}
