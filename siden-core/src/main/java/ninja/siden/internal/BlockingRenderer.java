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
import ninja.siden.Renderer;

import java.io.IOException;

/**
 * @author taichi
 */
public class BlockingRenderer<T> implements Renderer<T> {

    final Renderer<T> renderer;

    public BlockingRenderer(Renderer<T> renderer) {
        super();
        this.renderer = renderer;
    }

    @Override
    public void render(T model, HttpServerExchange sink) throws IOException {
        if (sink.isBlocking() == false) {
            sink.startBlocking();
        }
        if (sink.isInIoThread()) {
            sink.dispatch(exchange -> {
                renderer.render(model, exchange);
            });
        } else {
            renderer.render(model, sink);
        }
    }
}
