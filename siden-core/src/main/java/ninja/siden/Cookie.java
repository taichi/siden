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

import java.util.Date;

/**
 * @author taichi
 */
public interface Cookie {

	String name();

	String value();

	Cookie value(final String value);

	String path();

	Cookie path(final String path);

	String domain();

	Cookie domain(final String domain);

	Integer maxAge();

	Cookie maxAge(final Integer maxAge);

	boolean discard();

	Cookie discard(final boolean discard);

	boolean secure();

	Cookie secure(final boolean secure);

	int version();

	Cookie version(final int version);

	boolean httpOnly();

	Cookie httpOnly(final boolean httpOnly);

	Date expires();

	Cookie expires(final Date expires);

	String comment();

	Cookie comment(final String comment);
}
