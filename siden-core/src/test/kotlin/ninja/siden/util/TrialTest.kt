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
package ninja.siden.util

import org.junit.Test

import java.io.File
import java.io.IOException
import java.util.Optional

import org.junit.Assert.assertEquals

/**
 * @author taichi
 */
class TrialTest {

    @Test
    @Throws(Exception::class)
    fun success() {
        val ret = Optional.of("aaa")
                .map(Trial.of<String, File> { ok(it) })
                .map({ t -> t.either({ f -> 200 }, { ioex -> 400 }) })
                .map({ i -> i!! + 10 }).get()
        assertEquals(210, ret.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun failed() {
        val ret = Optional.of("aaa")
                .map(Trial.of<String, File> { ng(it) })
                .map({ t -> t.either({ f -> 200 }, { ioex -> 400 }) })
                .map({ i -> i!! + 11 }).get()

        assertEquals(411, ret.toLong())
    }

    companion object {

        internal fun ok(s: String): File {
            return File(s)
        }

        internal fun ng(s: String): File {
            throw IOException(s)
        }
    }

}
