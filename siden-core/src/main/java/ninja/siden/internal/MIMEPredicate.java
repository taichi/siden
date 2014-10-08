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

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.QValueParser;

import java.util.List;

/**
 * @author taichi
 */
public class MIMEPredicate implements Predicate {

	final HttpString name;

	final String contentType;

	public static Predicate accept(String type) {
		return new MIMEPredicate(Headers.ACCEPT, type);
	}

	public static Predicate contentType(String type) {
		return new MIMEPredicate(Headers.CONTENT_TYPE, type);
	}

	public MIMEPredicate(HttpString name, String contentType) {
		super();
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public boolean resolve(HttpServerExchange value) {
		final List<String> res = value.getRequestHeaders().get(name);
		if (res == null || res.isEmpty()) {
			return false;
		}
		final List<List<QValueParser.QValueResult>> found = QValueParser
				.parse(res);
		return found
				.stream()
				.flatMap(List::stream)
				.anyMatch(
						v -> v.getValue().equals("*")
								|| v.getValue().equals("*/*")
								|| v.getValue().equals(contentType));
	}
}
