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
package ninja.siden.jmx

import ninja.siden.jmx.*
import org.junit.Test

import javax.management.ObjectName
import java.util.Arrays

import org.junit.Assert.assertEquals

/**
 * @author taichi
 */
class ObjectNamesTest {

    @Test
    fun toObjectName() {
        val name = "aaa.bbb:type=Z".toObjectName()
        assertEquals("aaa.bbb", name.domain)
        assertEquals("Z", name.getKeyProperty("type"))
    }

    @Test
    fun withMap() {
        val list = Arrays.asList("type", "Z", "aaa", "bbb", "ccc", "ddd", "bbb", "zzz")
        val name = "aaa.bbb".toObjectName(list)
        assertEquals("aaa.bbb:type=Z,aaa=bbb,ccc=ddd,bbb=zzz", name.toString())
    }

}
