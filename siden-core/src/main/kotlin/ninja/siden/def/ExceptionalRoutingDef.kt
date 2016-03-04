/*
 * Copyright 2015 SATO taichi
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
package ninja.siden.def

import ninja.siden.*

/**
 * @author taichi
 */
class ExceptionalRoutingDef<EX : Throwable>(val type: Class<EX>,
                                            val route: (EX, Request, Response) -> Any) : RendererCustomizer<ExceptionalRoutingDef<EX>> {
    var renderer: Renderer<*>? = null
        internal set

    override fun <MODEL> render(renderer: Renderer<MODEL>): ExceptionalRoutingDef<EX> {
        this.renderer = renderer
        return this
    }
}
