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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.management.ObjectName;

import ninja.siden.jmx.ObjectNames;

import org.junit.Test;

/**
 * @author taichi
 */
public class ObjectNamesTest {

	@Test
	public void to() throws Exception {
		ObjectName name = ObjectNames.to("aaa.bbb:type=Z");
		assertEquals("aaa.bbb", name.getDomain());
		assertEquals("Z", name.getKeyProperty("type"));
	}

	@Test
	public void withMap() throws Exception {
		List<String> list = Arrays.asList("type", "Z", "aaa", "bbb", "ccc",
				"ddd", "bbb", "zzz");
		ObjectName name = ObjectNames.to("aaa.bbb", list);
		assertEquals("aaa.bbb:type=Z,aaa=bbb,ccc=ddd,bbb=zzz", name.toString());
	}

}
