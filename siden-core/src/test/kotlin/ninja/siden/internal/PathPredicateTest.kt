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
package ninja.siden.internal

import io.undertow.server.HttpServerExchange
import mockit.Mock
import mockit.MockUp
import mockit.integration.junit4.JMockit
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

/**
 * @author taichi
 */
@RunWith(JMockit::class)
class PathPredicateTest {

    lateinit var exchange: HttpServerExchange

    internal fun setUpRequest(path: String) {
        this.exchange = object : MockUp<HttpServerExchange>() {
            val relativePath: String
                @Mock(invocations = 1)
                get() = path
        }.mockInstance
        this.exchange.relativePath = path
    }

    @Test
    @Throws(Exception::class)
    fun matchCompletely() {
        setUpRequest("/foo/bar")

        val target = PathPredicate("/foo/bar")
        assertTrue(target.resolve(this.exchange))
    }

    @Test
    @Throws(Exception::class)
    fun matchPartial() {
        setUpRequest("/foo/bar/baz")

        val target = PathPredicate("/foo/bar")
        assertTrue(target.resolve(this.exchange))
    }

    @Test
    @Throws(Exception::class)
    fun unmatchPartial() {
        setUpRequest("/foo/baz/bar")

        val target = PathPredicate("/foo/bar")
        assertFalse(target.resolve(this.exchange))
    }

    @Test
    @Throws(Exception::class)
    fun matchWithOneVars() {
        setUpRequest("/foo/aaa")

        val target = PathPredicate("/foo/:bar")
        assertTrue(target.resolve(this.exchange))
        val m = this.exchange.getAttachment(PathPredicate.PARAMS)
        assertEquals("aaa", m["bar"])
    }

    @Test
    @Throws(Exception::class)
    fun matchWithDots() {
        setUpRequest("/foo/aaa.json")

        val target = PathPredicate("/foo/:bar.:ext")
        assertTrue(target.resolve(this.exchange))
        val m = this.exchange.getAttachment(PathPredicate.PARAMS)
        assertEquals("aaa", m["bar"])
        assertEquals("json", m["ext"])
    }

    @Test
    @Throws(Exception::class)
    fun matchWithMultiVars() {
        setUpRequest("/foo/aaa/baz/ccc")

        val target = PathPredicate("/foo/:bar/baz/:foo")
        assertTrue(target.resolve(this.exchange))
        val m = this.exchange.getAttachment(PathPredicate.PARAMS)

        assertEquals("aaa", m["bar"])
        assertEquals("ccc", m["foo"])
    }

    @Test
    @Throws(Exception::class)
    fun matchWithMultiVarsByRegex() {
        setUpRequest("/foo/aaa/baz/ccc")

        val target = PathPredicate(
                Pattern.compile("/foo/(?<bar>\\w+)/baz/(?<foo>\\w+)"))
        assertTrue(target.resolve(this.exchange))

        val m = this.exchange.getAttachment(PathPredicate.PARAMS)
        assertEquals("aaa", m["bar"])
        assertEquals("ccc", m["foo"])
    }

    companion object {

        @BeforeClass
        @Throws(Exception::class)
        @JvmStatic
        fun beforeClass() {
            Testing.useALL(PathPredicate::class.java)
        }
    }

}
