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

import java.nio.file.Path;
import java.util.List;

/**
 * @author taichi
 */
public class React {

	final JsEngine engine = new JsEngine();

	final String name;

	final String containerId;

	public React(String name, String containerId, List<Path> scripts) {
		super();
		this.name = name;
		this.containerId = containerId;
		this.engine.initialize(scripts);
	}

	String makeScript(String encodedProps) {
		StringBuilder stb = new StringBuilder();
		stb.append("React.renderComponentToString(");
		appendInitializer(stb, encodedProps);
		stb.append(")");
		return stb.toString();
	}

	public StringBuilder toHtml(String encodedProps) {
		StringBuilder stb = new StringBuilder();
		stb.append("<div id=\"");
		stb.append(this.containerId);
		stb.append("\">");
		stb.append(this.engine.eval(makeScript(encodedProps)));
		stb.append("</div>");
		return stb;
	}

	public StringBuilder toClientJs(String encodedProps) {
		StringBuilder stb = new StringBuilder();
		stb.append("React.renderComponent(");
		appendInitializer(stb, encodedProps);
		stb.append(", document.getElementById(");
		stb.append("\"");
		stb.append(this.containerId);
		stb.append("\"));");
		return stb;
	}

	void appendInitializer(StringBuilder a, String encodedProps) {
		a.append(this.name);
		a.append('(');
		a.append(encodedProps);
		a.append(')');
	}
}
