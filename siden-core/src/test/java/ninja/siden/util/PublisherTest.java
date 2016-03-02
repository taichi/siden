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
package ninja.siden.util;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * @author taichi
 */
public class PublisherTest {

    Publisher<String> target;

    @Before
    public void setUp() throws Exception {
        this.target = new Publisher<>();
    }

    @Test
    public void on() throws Exception {
        String[] called = {null};
        target.on(s -> called[0] = s);
        target.post("aaa");
        assertEquals("aaa", called[0]);
        target.post("bbb");
        assertEquals("aaa", called[0]);
    }

    @Test
    public void off() throws Exception {
        String[] called = {"aaa"};
        Consumer<String> fn = s -> called[0] = s;
        target.on(fn);
        target.off(fn);
        target.post("bbb");
        assertEquals("aaa", called[0]);
    }
}