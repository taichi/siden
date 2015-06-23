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
package ninja.siden.def;

import ninja.siden.ExceptionalRoute;
import ninja.siden.Renderer;
import ninja.siden.RendererCustomizer;

/**
 * @author taichi
 */
public class ExceptionalRoutingDef<EX extends Throwable> implements
		RendererCustomizer<ExceptionalRoutingDef<EX>> {

	Class<EX> type;
	ExceptionalRoute<EX> route;
	Renderer<?> renderer;

	public ExceptionalRoutingDef(Class<EX> type, ExceptionalRoute<EX> route) {
		super();
		this.type = type;
		this.route = route;
	}

	@Override
	public <MODEL> ExceptionalRoutingDef<EX> render(Renderer<MODEL> renderer) {
		this.renderer = renderer;
		return this;
	}

	public Class<EX> type() {
		return this.type;
	}

	public ExceptionalRoute<EX> route() {
		return this.route;
	}

	public Renderer<?> renderer() {
		return this.renderer;
	}
}
