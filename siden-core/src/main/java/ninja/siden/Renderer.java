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
package ninja.siden;

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ninja.siden.internal.BlockingRenderer;
import ninja.siden.internal.Core;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
@FunctionalInterface
public interface Renderer<T> {

	void render(T model, HttpServerExchange sink) throws IOException;

	public static <MODEL> Renderer<MODEL> ofStream(
			OutputStreamConsumer<MODEL> fn) {
		return new BlockingRenderer<MODEL>((model, sink) -> fn.render(model,
				sink.getOutputStream()));
	}

	public static <MODEL> Renderer<MODEL> of(WriterConsumer<MODEL> fn) {
		return new BlockingRenderer<MODEL>((model, sink) -> {
			OptionMap config = sink.getAttachment(Core.CONFIG);
			Writer w = new OutputStreamWriter(sink.getOutputStream(),
					config.get(Config.CHARSET));
			fn.render(model, w);
			w.flush();
		});
	}

	@FunctionalInterface
	public interface OutputStreamConsumer<T> {
		void render(T model, OutputStream out) throws IOException;
	}

	@FunctionalInterface
	public interface WriterConsumer<T> {
		void render(T model, Writer out) throws IOException;
	}
}
