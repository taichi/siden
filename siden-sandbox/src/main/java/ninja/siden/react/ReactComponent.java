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

/**
 * @author taichi
 */
public class ReactComponent {

	final JsEngine engine;

	final String name;

	final String encodedProps;

	final String containerId;

	public ReactComponent(JsEngine engine, String name, String encodedProps,
			String containerId) {
		super();
		this.engine = engine;
		this.name = name;
		this.encodedProps = encodedProps;
		this.containerId = containerId;
	}

	String makeScript() {
		StringBuilder stb = new StringBuilder();
		stb.append("React.renderComponentToString(");
		appendInitializer(stb);
		stb.append(")");
		return stb.toString();
	}

	public StringBuilder toHtml() {
		StringBuilder stb = new StringBuilder();
		stb.append("<div id=\"");
		stb.append(this.containerId);
		stb.append("\">");
		stb.append(engine.eval(makeScript()));
		stb.append("</div>");
		return stb;
	}

	public StringBuilder toClientJs() {
		StringBuilder stb = new StringBuilder();
		stb.append("React.renderComponent(");
		appendInitializer(stb);
		stb.append(", document.getElementById(");
		stb.append("\"");
		stb.append(this.containerId);
		stb.append("\"));");
		return stb;
	}

	void appendInitializer(StringBuilder a) {
		a.append(this.name);
		a.append('(');
		a.append(this.encodedProps);
		a.append(')');
	}
}
