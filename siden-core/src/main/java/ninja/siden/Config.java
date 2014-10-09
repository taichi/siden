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

import io.undertow.util.MimeMappings;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import ninja.siden.internal.RendererSelector;

import org.xnio.Option;
import org.xnio.OptionMap;

/**
 * @author taichi
 */
public interface Config {

	/**
	 * Environment mode, defaults to System.getenv(SIDEN_ENV) (SIDEN_ENV
	 * environment variable) or "development"
	 */
	Option<String> ENV = Option.simple(Config.class, "ENV", String.class);

	/**
	 * Use _method magic to allow put/delete forms in browsers that don't
	 * support it.
	 * 
	 * @see <a
	 *      href="http://jxck.hatenablog.com/entry/why-form-dosent-support-put-delete">why-form-dosent-support-put-delete</a>
	 */
	Option<Boolean> METHOD_OVERRIDE = Option.simple(Config.class,
			"METHOD_OVERRIDE", Boolean.class);

	Option<Renderer> DEFAULT_RENDERER = Option.simple(Config.class,
			"DEFAULT_RENDERER", Renderer.class);

	Option<RendererRepository> RENDERER_REPOSITORY = Option.simple(
			Config.class, "RENDERER_REPOSITORY", RendererRepository.class);

	Option<Charset> CHARSET = Option.simple(Config.class, "CHARSET",
			Charset.class);

	Option<MimeMappings> MIME_MAPPINGS = Option.simple(Config.class,
			"MIME_MAPPINGS", MimeMappings.class);

	Option<Long> TRANSFER_MIN_SIZE = Option.simple(Config.class,
			"TRANSFER_MIN_SIZE", Long.class);

	// multipart request options
	Option<Long> MAX_FILE_SIZE = Option.simple(Config.class, "MAX_FILE_SIZE",
			Long.class);

	Option<File> TEMP_DIR = Option.simple(Config.class, "TEMP_DIR", File.class);

	Option<String> SESSION_COOKIE_NAME = Option.simple(Config.class,
			"SESSION_COOKIE_NAME", String.class);

	Option<Integer> MAX_SESSIONS = Option.simple(Config.class, "MAX_SESSIONS",
			Integer.class);

	Option<Integer> DEFAULT_SESSION_TIMEOUT_SECONDS = Option.simple(
			Config.class, "DEFAULT_SESSION_TIMEOUT_SECONDS", Integer.class);
	
	// WebSocket Options
	Option<Long> WS_MAX_IDLE_TIMEOUT = Option.simple(Config.class, "WS_MAX_IDLE_TIMEOUT", Long.class);
	
	Option<Integer> WS_BINARY_MESSAGE_BUFFER_SIZE = Option.simple(Config.class, "WS_BINARY_MESSAGE_BUFFER_SIZE", Integer.class);

	Option<Integer> WS_TEXT_MESSAGE_BUFFER_SIZE = Option.simple(Config.class, "WS_TEXT_MESSAGE_BUFFER_SIZE", Integer.class);
	
	// Security Options

	/**
	 * default value is X-Frame-Options: SAMEORIGIN
	 * 
	 * @see <a
	 *      href="https://developer.mozilla.org/en-US/docs/Web/HTTP/X-Frame-Options">HTTP/X-Frame-Options</a>
	 */
	Option<String> FRAME_OPTIONS = Option.simple(Config.class, "FRAME_OPTIONS",
			String.class);

	/**
	 * X-XSS-Protection: 1; mode=block
	 */
	Option<Boolean> USE_XSS_PROTECTION = Option.simple(Config.class,
			"USE_XSS_PROTECTION", Boolean.class);

	/**
	 * X-Content-Type-Options: nosniff
	 */
	Option<Boolean> USE_CONTENT_TYPE_OPTIONS = Option.simple(Config.class,
			"USE_CONTENT_TYPE_OPTIONS", Boolean.class);

	static OptionMap.Builder defaults() {
		OptionMap.Builder omb = OptionMap.builder();
		omb.set(ENV,
				Objects.toString(System.getenv("SIDEN_ENV"), "development"));
		omb.set(METHOD_OVERRIDE, false);
		omb.set(DEFAULT_RENDERER, new RendererSelector());
		omb.set(RENDERER_REPOSITORY, RendererRepository.EMPTY);
		omb.set(CHARSET, StandardCharsets.UTF_8);

		MimeMappings.Builder mmb = MimeMappings.builder();
		mmb.addMapping("ico", "image/x-icon");
		omb.set(MIME_MAPPINGS, mmb.build());

		omb.set(TRANSFER_MIN_SIZE, 16 * 1024);

		omb.set(MAX_FILE_SIZE, -1);
		omb.set(TEMP_DIR, new File(System.getProperty("java.io.tmpdir")));

		omb.set(SESSION_COOKIE_NAME, "sid");
		omb.set(MAX_SESSIONS, -1);
		omb.set(DEFAULT_SESSION_TIMEOUT_SECONDS, 30 * 60);
		
		omb.set(WS_MAX_IDLE_TIMEOUT, 0L);
		omb.set(WS_BINARY_MESSAGE_BUFFER_SIZE, -1);
		omb.set(WS_TEXT_MESSAGE_BUFFER_SIZE, -1);

		omb.set(FRAME_OPTIONS, "SAMEORIGIN");
		omb.set(USE_XSS_PROTECTION, true);
		omb.set(USE_CONTENT_TYPE_OPTIONS, true);

		return omb;
	}
}
