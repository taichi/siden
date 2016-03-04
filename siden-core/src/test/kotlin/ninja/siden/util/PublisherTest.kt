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
package ninja.siden.util

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * @author taichi
 */
class PublisherTest {

    lateinit var target: Publisher<String>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        this.target = Publisher<String>()
    }

    @Test
    @Throws(Exception::class)
    fun on() {
        val called = arrayOf("")
        target.on { s -> called[0] = s }
        target.post("aaa")
        assertEquals("aaa", called[0])
        target.post("bbb")
        assertEquals("aaa", called[0])
    }

    @Test
    @Throws(Exception::class)
    fun off() {
        val called = arrayOf("aaa")
        val fn = { s: String -> called[0] = s }
        target.on(fn)
        target.off(fn)
        target.post("bbb")
        assertEquals("aaa", called[0])
    }
}
