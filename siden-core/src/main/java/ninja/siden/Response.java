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

import java.util.Map;

/**
 * @author taichi
 */
public interface Response {

	Response status(int code);

	Response header(String name, String... values);

	/**
	 * set RFC1123 date pattern to Response header.
	 * 
	 * @param name
	 * @param date
	 * @return this
	 */
	Response header(String name, long date);

	Response headers(Map<String, String> headers);

	Cookie cookie(String name, String value);

	/**
	 * @param name
	 * @return existing value
	 */
	Cookie removeCookie(String name);

	/**
	 * @param contentType
	 */
	Response type(String contentType);

	Void reidrect(String location);

	Void redirect(int code, String location);

	Void render(Object model, Renderer renderer) throws Exception;

	Void render(Object model, String template) throws Exception;

	HttpServerExchange raw();
}
