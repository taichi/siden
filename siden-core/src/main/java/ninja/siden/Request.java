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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author taichi
 */
public interface Request extends AttributeContainer {

	HttpMethod method();

	String path();

	/**
	 * get path parameter
	 * 
	 * @param key
	 * @return
	 */
	Optional<String> params(String key);

	Map<String, String> params();

	/**
	 * get query parameter
	 * 
	 * @param key
	 * @return
	 */
	Optional<String> query(String key);

	Optional<String> header(String name);

	List<String> headers(String name);

	Map<String, List<String>> headers();

	Map<String, Cookie> cookies();

	Optional<Cookie> cookie(String name);

	Optional<String> form(String key);

	List<String> forms(String key);

	Map<String, List<String>> forms();

	Optional<File> file(String key);

	List<File> files(String key);

	Map<String, List<File>> files();

	/**
	 * get current session or create new session.
	 * 
	 * @return session
	 */
	Session session();

	/**
	 * get current session
	 * 
	 * @return session or empty
	 */
	Optional<Session> current();

	boolean xhr();

	String protocol();

	String scheme();

	HttpServerExchange raw();

}
