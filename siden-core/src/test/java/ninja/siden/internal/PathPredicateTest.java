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
package ninja.siden.internal;

import io.undertow.server.HttpServerExchange;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author taichi
 */
@RunWith(JMockit.class)
public class PathPredicateTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        Testing.useALL(PathPredicate.class);
    }

    HttpServerExchange exchange;

    void setUpRequest(String path) {
        this.exchange = new MockUp<HttpServerExchange>() {
            @Mock(invocations = 1)
            public String getRelativePath() {
                return path;
            }
        }.getMockInstance();
        this.exchange.setRelativePath(path);
    }

    @Test
    public void matchCompletely() throws Exception {
        setUpRequest("/foo/bar");

        PathPredicate target = new PathPredicate("/foo/bar");
        assertTrue(target.resolve(this.exchange));
    }

    @Test
    public void matchPartial() throws Exception {
        setUpRequest("/foo/bar/baz");

        PathPredicate target = new PathPredicate("/foo/bar");
        assertTrue(target.resolve(this.exchange));
    }

    @Test
    public void unmatchPartial() throws Exception {
        setUpRequest("/foo/baz/bar");

        PathPredicate target = new PathPredicate("/foo/bar");
        assertFalse(target.resolve(this.exchange));
    }

    @Test
    public void matchWithOneVars() throws Exception {
        setUpRequest("/foo/aaa");

        PathPredicate target = new PathPredicate("/foo/:bar");
        assertTrue(target.resolve(this.exchange));
        Map<String, String> m = this.exchange
                .getAttachment(PathPredicate.PARAMS);
        assertEquals("aaa", m.get("bar"));
    }

    @Test
    public void matchWithDots() throws Exception {
        setUpRequest("/foo/aaa.json");

        PathPredicate target = new PathPredicate("/foo/:bar.:ext");
        assertTrue(target.resolve(this.exchange));
        Map<String, String> m = this.exchange
                .getAttachment(PathPredicate.PARAMS);
        assertEquals("aaa", m.get("bar"));
        assertEquals("json", m.get("ext"));
    }

    @Test
    public void matchWithMultiVars() throws Exception {
        setUpRequest("/foo/aaa/baz/ccc");

        PathPredicate target = new PathPredicate("/foo/:bar/baz/:foo");
        assertTrue(target.resolve(this.exchange));
        Map<String, String> m = this.exchange
                .getAttachment(PathPredicate.PARAMS);

        assertEquals("aaa", m.get("bar"));
        assertEquals("ccc", m.get("foo"));
    }

    @Test
    public void matchWithMultiVarsByRegex() throws Exception {
        setUpRequest("/foo/aaa/baz/ccc");

        PathPredicate target = new PathPredicate(
                Pattern.compile("/foo/(?<bar>\\w+)/baz/(?<foo>\\w+)"));
        assertTrue(target.resolve(this.exchange));

        Map<String, String> m = this.exchange
                .getAttachment(PathPredicate.PARAMS);
        assertEquals("aaa", m.get("bar"));
        assertEquals("ccc", m.get("foo"));
    }

}
