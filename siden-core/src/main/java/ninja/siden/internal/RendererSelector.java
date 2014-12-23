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
package ninja.siden.internal;

import io.undertow.io.IoCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.util.MimeMappings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import ninja.siden.Config;
import ninja.siden.Renderer;

import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.streams.ReaderInputStream;
import org.xnio.streams.Streams;

/**
 * @author taichi
 */
public class RendererSelector<T> implements Renderer<T> {

	List<SelectableRenderer<T>> renderers;

	public RendererSelector() {
		this(defaultRenderers());
	}

	public RendererSelector(List<SelectableRenderer<T>> renderers) {
		this.renderers = renderers;
	}

	public static <T> List<SelectableRenderer<T>> defaultRenderers() {
		return Arrays.asList(new StringRenderer<T>(), new FileRenderer<T>(),
				new PathRenderer<T>(), new FileChannelRenderer<T>(),
				new ByteArrayRenderer<T>(), new ByteBufferRenderer<T>(),
				new URIRenderer<T>(), new URLRenderer<T>(),
				new ReaderRenderer<T>(), new InputStreamRenderer<T>(),
				new CharSequenceRenderer<T>(), new ToStringRenderer<T>());
	}

	@Override
	public void render(T model, HttpServerExchange sink) throws IOException {
		for (SelectableRenderer<T> pr : renderers) {
			if (pr.test(model)) {
				pr.render(model, sink);
				return;
			}
		}
	}

	interface SelectableRenderer<T> extends Renderer<T>, Predicate<T> {
	}

	static class ReaderRenderer<T> implements SelectableRenderer<T> {
		final InputStreamRenderer<T> delegate = new InputStreamRenderer<T>();

		@Override
		public boolean test(Object model) {
			return Reader.class.isAssignableFrom(model.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			Reader reader = (Reader) model;
			SecurityHandler.addContentType(sink);
			OptionMap config = sink.getAttachment(Core.CONFIG);
			delegate.render(
					new ReaderInputStream(reader, config.get(Config.CHARSET)),
					sink);
		}
	}

	static class InputStreamRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object model) {
			return InputStream.class.isAssignableFrom(model.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			SecurityHandler.addContentType(sink);
			Renderer.ofStream((Object o, OutputStream out) -> {
				InputStream in = (InputStream) o;
				try {
					Streams.copyStream(in, out, false);
				} finally {
					IoUtils.safeClose(in);
				}
			}).render(model, sink);
		}
	}

	static class URIRenderer<T> implements SelectableRenderer<T> {
		final Renderer<URL> delegate = new URLRenderer<URL>();

		@Override
		public boolean test(Object t) {
			return URI.class == t.getClass();
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			URI uri = (URI) model;
			this.delegate.render(uri.toURL(), sink);
		}
	}

	static class URLRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object t) {
			return URL.class == t.getClass();
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			URL url = (URL) model;
			OptionMap config = sink.getAttachment(Core.CONFIG);
			MimeMappings mm = config.get(Config.MIME_MAPPINGS);
			// TODO proxy?
			URLResource resource = new URLResource(url, url.openConnection(),
					url.getPath());
			SecurityHandler.addContentType(sink, resource.getContentType(mm));
			resource.serve(sink.getResponseSender(), sink,
					IoCallback.END_EXCHANGE);
		}
	}

	static class PathRenderer<T> implements SelectableRenderer<T> {
		Renderer<URL> delegate = new URLRenderer<URL>();

		@Override
		public boolean test(Object t) {
			return Path.class.isAssignableFrom(t.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			Path path = (Path) model;
			this.delegate.render(path.toUri().toURL(), sink);
		}
	}

	static class FileRenderer<T> implements SelectableRenderer<T> {
		Renderer<URL> delegate = new URLRenderer<URL>();

		@Override
		public boolean test(Object t) {
			return File.class.isAssignableFrom(t.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			File file = (File) model;
			this.delegate.render(file.toURI().toURL(), sink);
		}
	}

	static class FileChannelRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object t) {
			return FileChannel.class.isAssignableFrom(t.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			FileChannel channel = (FileChannel) model;
			SecurityHandler.addContentType(sink);
			sink.getResponseSender().transferFrom(channel,
					IoCallback.END_EXCHANGE);
		}
	}

	static class ByteBufferRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object t) {
			return ByteBuffer.class.isAssignableFrom(t.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			ByteBuffer s = (ByteBuffer) model;
			SecurityHandler.addContentType(sink);
			sink.getResponseSender().send(s);
		}
	}

	static class ByteArrayRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object t) {
			return byte[].class == t.getClass();
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			byte[] ary = (byte[]) model;
			SecurityHandler.addContentType(sink);
			sink.getResponseSender().send(ByteBuffer.wrap(ary));
		}
	}

	public static class StringRenderer<T> implements SelectableRenderer<T> {

		@Override
		public boolean test(Object t) {
			return String.class == t.getClass();
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			OptionMap config = sink.getAttachment(Core.CONFIG);
			String s = model.toString();
			SecurityHandler.addContentType(
					sink,
					String.format("text/plain; charset=%s",
							config.get(Config.CHARSET)));
			sink.getResponseSender().send(s, config.get(Config.CHARSET));
		}
	}

	static class CharSequenceRenderer<T> implements SelectableRenderer<T> {
		StringRenderer<T> deleagte = new StringRenderer<T>();

		@Override
		public boolean test(Object t) {
			return CharSequence.class.isAssignableFrom(t.getClass());
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			this.deleagte.render(model, sink);
		}
	}

	static class ToStringRenderer<T> implements SelectableRenderer<T> {
		StringRenderer<T> deleagte = new StringRenderer<T>();

		@Override
		public boolean test(Object t) {
			return true;
		}

		@Override
		public void render(Object model, HttpServerExchange sink)
				throws IOException {
			this.deleagte.render(Objects.toString(model), sink);
		}
	}
}
