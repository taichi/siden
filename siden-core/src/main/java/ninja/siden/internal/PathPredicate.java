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
import io.undertow.util.AttachmentKey;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taichi
 */
public class PathPredicate implements Predicate {

    static final Logger LOG = Logger.getLogger(PathPredicate.class);

    public static final AttachmentKey<Map<String, String>> PARAMS = AttachmentKey
            .create(Map.class);

    static final Pattern NAMED = Pattern
            .compile("\\(\\?\\<(?<name>\\w+)\\>[^)]+\\)");
    static final Pattern SEGMENT = Pattern
            .compile("(?<prefix>[/\\.])?(?::(?<name>\\w+))");

    Pattern template;
    List<String> names = new ArrayList<>();

    public PathPredicate(Pattern template) {
        this.template = template;
        for (Matcher m = NAMED.matcher(template.pattern()); m.find(); ) {
            names.add(m.group("name"));
        }
        LOG.debug(names);
    }

    public PathPredicate(String template) {
        StringBuilder stb = new StringBuilder(template);
        Matcher m = SEGMENT.matcher(stb);
        int index = 0;
        while (index < stb.length() && m.find(index)) {
            names.add(m.group("name"));
            LOG.debug(names);
            String v = makeReplacement(m.group("prefix"));
            index = m.start() + v.length();
            stb.replace(m.start(), m.end(), v);
            m = SEGMENT.matcher(stb);
        }
        stb.append("(?:.*)");
        LOG.debug(stb);
        this.template = Pattern.compile(stb.toString());
    }

    String makeReplacement(String prefix) {
        String pref = Objects.toString(prefix, "");
        if (".".equals(pref)) {
            pref = "\\.";
        }
        return pref + "([^\\/]+)";
    }

    @Override
    public boolean resolve(HttpServerExchange exchange) {
        Matcher m = this.template.matcher(exchange.getRelativePath());
        if (m.matches()) {
            int count = m.groupCount();
            if (count <= names.size()) {
                Map<String, String> newone = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    newone.put(names.get(i), m.group(i + 1));
                }
                exchange.putAttachment(PARAMS, newone);
            }
            return true;
        }
        return false;
    }

}
