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

import java.util.Optional;

/**
 * @author taichi
 */
public interface AttributeContainer extends Iterable<AttributeContainer.Attr> {

	/**
	 * @param key
	 *            attribute name
	 * @param newone
	 *            new attribute
	 * @return existing value
	 */
	<T> Optional<T> attr(String key, T newone);

	<T> Optional<T> attr(String key);

	/**
	 * @param key
	 * @return existing value
	 */
	<T> Optional<T> remove(String key);

	interface Attr {
		String name();

		<T> T value();

		<T> T remove();
	}
}
