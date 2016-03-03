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

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author taichi
 */
@RunWith(Parameterized.class)
public class MIMEPredicateTest {

    HttpServerExchange exchange;

    @Before
    public void setUp() {
        this.exchange = new HttpServerExchange(null);
    }

    String wait;

    String request;

    boolean is;

    public MIMEPredicateTest(String wait, String request, boolean is) {
        this.wait = wait;
        this.request = request;
        this.is = is;
    }

    @Parameters(name = "{0} {1}")
    public static Iterable<Object[]> parameters() throws Exception {
        return Arrays.asList(new Object[][]{
                {"application/json", "application/json", true},
                {"application/json", "APPLICATION/json", true},
                {"application/json", "application/JSON", true},

                {"application/*", "application/json", true},
                {"*/json", "text/html", true},
                {"*/*", "application/json", true},
                {"*", "application/json", true},

                {"application/json", "application/*", true},
                {"text/html", "*/json", true},
                {"application/json", "*/*", true},
                {"application/json", "*", true},

                {"application/json", "application/xml", false},
                {"application/json", "text/json", false},
                {"application/*", "text/json", false},

                {"application/json", "text/*", false},
        });
    }

    @Test
    public void test() throws Exception {
        Predicate p = MIMEPredicate.Companion.accept(wait);
        this.exchange.getRequestHeaders().add(Headers.ACCEPT, request);
        assertEquals(is, p.resolve(exchange));
    }
}
