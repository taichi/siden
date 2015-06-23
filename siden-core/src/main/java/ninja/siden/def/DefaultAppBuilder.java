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

import io.undertow.Handlers;
import io.undertow.predicate.PredicatesHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.internal.Core;
import ninja.siden.internal.FiltersHandler;
import ninja.siden.internal.MethodOverrideHandler;
import ninja.siden.internal.RoutingHandler;
import ninja.siden.internal.SecurityHandler;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class DefaultAppBuilder implements AppBuilder {

	protected OptionMap config;

	protected PathHandler assets;

	protected RoutingHandler router;

	protected PathHandler subapp;

	protected PredicatesHandler websockets;

	protected FiltersHandler filters;

	public DefaultAppBuilder(OptionMap config) {
		this.config = config;
		this.assets = Handlers.path();
		this.router = new RoutingHandler(this.assets);
		this.subapp = new PathHandler(this.router);
		this.websockets = new PredicatesHandler(this.subapp);
		this.filters = new FiltersHandler(this.websockets);
	}

	@Override
	public void begin() {
		if (config.get(Config.SIDEN_FAVICON, false)) {
			AssetDef.useDefaultFavicon(this.assets);
		}
	}

	@Override
	public void apply(AppContext context, AssetDef def) {
		def.addTo(this.assets, this.config);
	}

	@Override
	public void apply(AppContext context, RoutingDef def) {
		def.addTo(this.router, this.config);
	}

	@Override
	public void apply(AppContext context, ErrorCodeRoutingDef def) {
		this.router.add(def);
	}

	@Override
	public void apply(AppContext context, ExceptionalRoutingDef<?> def) {
		this.router.add(def);
	}

	@Override
	public void apply(AppContext context, SubAppDef def) {
		DefaultAppBuilder kids = new DefaultAppBuilder(this.config);
		def.app().accept(new AppContext(context, def), kids);
		this.subapp.addPrefixPath(def.prefix(), kids.filters);
	}

	@Override
	public void apply(AppContext context, WebSocketDef def) {
		def.addTo(this.websockets, this.config);
	}

	@Override
	public void apply(AppContext context, FilterDef def) {
		this.filters.add(def);
	}

	@Override
	public HttpHandler end(App root) {
		return makeSharedHandlers(root, this.config, this.filters);
	}

	protected HttpHandler makeSharedHandlers(App root, OptionMap config,
			HttpHandler next) {
		HttpHandler hh = next;
		if (config.get(Config.METHOD_OVERRIDE)) {
			hh = new MethodOverrideHandler(hh);
		}
		hh = makeSessionHandler(root, config, hh);
		hh = makeFormHandler(root, config, hh);

		if (Config.isInDev(config)) {
			hh = Handlers.disableCache(hh);
		}

		hh = new SecurityHandler(hh);
		return new Core(config, hh);
	}

	protected HttpHandler makeSessionHandler(App root, OptionMap config,
			HttpHandler next) {
		InMemorySessionManager sessionManager = new InMemorySessionManager(
				"SessionManagerOfSiden", config.get(Config.MAX_SESSIONS));
		sessionManager.setDefaultSessionTimeout(config
				.get(Config.DEFAULT_SESSION_TIMEOUT_SECONDS));
		SessionCookieConfig sessionConfig = new SessionCookieConfig();
		sessionConfig.setCookieName(config.get(Config.SESSION_COOKIE_NAME));
		return new SessionAttachmentHandler(next, sessionManager, sessionConfig);
	}

	protected HttpHandler makeFormHandler(App root, OptionMap config,
			HttpHandler next) {
		FormParserFactory.Builder builder = FormParserFactory.builder(false);
		FormEncodedDataDefinition form = new FormEncodedDataDefinition();
		String cn = config.get(Config.CHARSET).name();
		form.setDefaultEncoding(cn);

		MultiPartParserDefinition mult = new MultiPartParserDefinition(
				config.get(Config.TEMP_DIR));
		mult.setDefaultEncoding(cn);
		mult.setMaxIndividualFileSize(config.get(Config.MAX_FILE_SIZE));

		builder.addParsers(form, mult);

		EagerFormParsingHandler efp = new EagerFormParsingHandler(
				builder.build());
		return efp.setNext(next);
	}
}
