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

import io.undertow.Handlers;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.util.Headers;

import java.io.File;
import java.net.URL;

import ninja.siden.Config;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class AssetsHandler implements HttpHandler {

	final OptionMap config;
	final PathHandler delegate = Handlers.path();

	public AssetsHandler(OptionMap config) {
		this.config = config;
	}

	static void handleFavicon(HttpServerExchange exchange) throws Exception {
		URL url = AssetsHandler.class.getClassLoader().getResource(
				"favicon.ico");
		URLResource resource = new URLResource(url, url.openConnection(),
				url.getPath());
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/x-icon");
		resource.serve(exchange.getResponseSender(), exchange,
				IoCallback.END_EXCHANGE);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		this.delegate.handleRequest(exchange);
	}

	public Assets add(String path, File root) {
		ResourceManager rm = new FileResourceManager(root,
				config.get(Config.TRANSFER_MIN_SIZE));
		ResourceHandler rh = new ResourceHandler(rm);
		rh.setMimeMappings(config.get(Config.MIME_MAPPINGS));
		this.delegate.addPrefixPath(path, rh);
		return new Assets(rh);
	}

	public void useDefaultFavicon() {
		this.delegate
				.addExactPath("/favicon.ico", AssetsHandler::handleFavicon);
	}
}
