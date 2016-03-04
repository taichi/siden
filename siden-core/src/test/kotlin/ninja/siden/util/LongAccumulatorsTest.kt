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

import org.junit.experimental.runners.Enclosed
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

import java.util.concurrent.atomic.LongAccumulator

import org.junit.Assert.assertEquals

/**
 * @author taichi
 */
@RunWith(Enclosed::class)
class LongAccumulatorsTest {

    @RunWith(Theories::class)
    class Max {

        @Theory
        fun test(fixture: IntArray) {
            val la = LongAccumulators.max()
            la.accumulate(fixture[0].toLong())
            la.accumulate(fixture[1].toLong())
            assertEquals(fixture[2].toLong(), la.get())
        }

        companion object {
            @DataPoints
            @JvmField
            var fixtures = arrayOf(intArrayOf(10, 11, 11), intArrayOf(10, 9, 10))
        }
    }

    @RunWith(Theories::class)
    class Min {

        @Theory
        fun test(fixture: IntArray) {
            val la = LongAccumulators.min()
            la.accumulate(fixture[0].toLong())
            la.accumulate(fixture[1].toLong())
            assertEquals(fixture[2].toLong(), la.get())
        }

        companion object {
            @DataPoints
            @JvmField
            val fixtures = arrayOf(intArrayOf(10, 11, 10), intArrayOf(10, 9, 9), intArrayOf(-10, 7, 7))
        }
    }
}
