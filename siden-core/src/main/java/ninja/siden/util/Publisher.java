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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author taichi
 */
public class Publisher<E> {

    List<Consumer<E>> listeners = Collections
            .synchronizedList(new ArrayList<>());

    public void on(Consumer<E> fn) {
        this.listeners.add(fn);
    }

    public void off(Consumer<E> fn) {
        this.listeners.remove(fn);
    }

    public void post(E event) {
        for (Iterator<Consumer<E>> i = this.listeners.iterator(); i.hasNext(); ) {
            try {
                i.next().accept(event);
            } finally {
                i.remove();
            }
        }
    }
}
