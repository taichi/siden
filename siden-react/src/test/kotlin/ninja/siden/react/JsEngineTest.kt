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
package ninja.siden.react

import org.junit.Before
import org.junit.Test

import java.util.Collections

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

/**
 * @author taichi
 */
class JsEngineTest {

    lateinit  var target: JsEngine

    @Before
    fun setUp() {
        this.target = JsEngine()
    }

    @Test
    fun testContainGlobal() {
        this.target.initialize(emptyList())
        assertNotNull(this.target.eval("global"))
    }

    @Test
    @Throws(Exception::class)
    fun testEvalSeparately() {
        this.target.initialize(emptyList())
        this.target.eval("var hoge = 10")
        assertNull(this.target.eval("this['hoge']"))
    }

}
