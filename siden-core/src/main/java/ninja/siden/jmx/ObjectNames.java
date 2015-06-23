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
package ninja.siden.jmx;

import java.util.Iterator;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author taichi
 */
public interface ObjectNames {

	static ObjectName to(CharSequence name) {
		try {
			return new ObjectName(name.toString());
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static ObjectName to(CharSequence domain, List<String> props) {
		if (props.size() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		StringBuilder stb = new StringBuilder(domain);
		stb.append(":");
		for (Iterator<String> i = props.iterator(); i.hasNext();) {
			stb.append(i.next());
			stb.append('=');
			stb.append(i.next());
			if (i.hasNext()) {
				stb.append(',');
			}
		}
		return to(stb);
	}
}
