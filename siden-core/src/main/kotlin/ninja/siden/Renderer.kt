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
package ninja.siden

import io.undertow.server.HttpServerExchange
import ninja.siden.internal.BlockingRenderer
import ninja.siden.internal.Core
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

/**
 * @author taichi
 */
@FunctionalInterface
interface Renderer<T> {

    @Throws(IOException::class)
    fun render(model: T, sink: HttpServerExchange)

    companion object {

        fun <MODEL> ofStream(fn: (MODEL, OutputStream) -> Unit): Renderer<MODEL> {
            return BlockingRenderer(object : Renderer<MODEL> {
                override fun render(model: MODEL, sink: HttpServerExchange) {
                    fn(model, sink.outputStream)
                }
            })
        }

        fun <MODEL> of(fn: (MODEL, Writer) -> Unit): Renderer<MODEL> {
            return BlockingRenderer(renderer = object : Renderer<MODEL> {
                override fun render(model: MODEL, sink: HttpServerExchange) {
                    val config = sink.getAttachment(Core.CONFIG)
                    val w = OutputStreamWriter(sink.outputStream, config.get(Config.CHARSET))
                    fn(model, w)
                    w.flush()
                }
            })
        }
    }
}
