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

import io.undertow.io.IoCallback;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import io.undertow.util.Headers;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import ninja.siden.AssetsCustomizer;
import ninja.siden.Config;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class AssetDef implements AssetsCustomizer {

	final String path;

	final String root;

	Integer cacheTime;

	boolean directoryListing = false;

	boolean canonicalizePaths = true;

	Predicate cachable = Predicates.truePredicate();

	Predicate allowed = Predicates.truePredicate();

	String[] welcomeFiles;

	ClassLoader loadFrom;

	public AssetDef(String path, String root) {
		this.path = Objects.requireNonNull(path);
		this.root = Objects.requireNonNull(root);
	}

	@Override
	public AssetsCustomizer cacheTime(Integer time) {
		this.cacheTime = Objects.requireNonNull(time);
		return this;
	}

	@Override
	public AssetsCustomizer directoryListing(boolean is) {
		this.directoryListing = is;
		return this;
	}

	public AssetsCustomizer setCanonicalizePaths(boolean canonicalizePaths) {
		this.canonicalizePaths = canonicalizePaths;
		return this;
	}

	@Override
	public AssetsCustomizer welcomeFiles(String... files) {
		this.welcomeFiles = Objects.requireNonNull(files);
		return this;
	}

	@Override
	public AssetsCustomizer from(ClassLoader loader) {
		this.loadFrom = Objects.requireNonNull(loader);
		return this;
	}

	public void addTo(PathHandler ph, OptionMap config) {
		ResourceHandler rh = new ResourceHandler(newResourceManager(config));
		rh.setMimeMappings(config.get(Config.MIME_MAPPINGS));
		if (this.cacheTime != null) {
			rh.setCacheTime(this.cacheTime);
		}
		rh.setDirectoryListingEnabled(this.directoryListing);
		rh.setCanonicalizePaths(this.canonicalizePaths);
		if (this.welcomeFiles != null) {
			rh.setWelcomeFiles(this.welcomeFiles);
		}

		ph.addPrefixPath(this.path, rh);
	}

	protected ResourceManager newResourceManager(OptionMap config) {
		if (this.loadFrom == null) {
			return new FileResourceManager(new File(this.root),
					config.get(Config.TRANSFER_MIN_SIZE));
		}
		return new ClassPathResourceManager(this.loadFrom, this.root);
	}

	public static void useDefaultFavicon(PathHandler ph) {
		ph.addExactPath(
				"/favicon.ico",
				ex -> {
					URL url = AssetDef.class.getClassLoader().getResource(
							"favicon.ico");
					URLResource resource = new URLResource(url, url
							.openConnection(), url.getPath());
					ex.getResponseHeaders().put(Headers.CONTENT_TYPE,
							"image/x-icon");
					resource.serve(ex.getResponseSender(), ex,
							IoCallback.END_EXCHANGE);
				});
	}
}
