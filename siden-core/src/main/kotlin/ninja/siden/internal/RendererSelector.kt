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

import io.undertow.io.IoCallback
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.resource.URLResource
import ninja.siden.Config
import ninja.siden.Renderer
import org.jboss.logging.Logger
import org.xnio.IoUtils
import org.xnio.streams.ReaderInputStream
import org.xnio.streams.Streams
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate

/**
 * @author taichi
 */
class RendererSelector<T> @JvmOverloads constructor(
        val renderers: List<RendererSelector.SelectableRenderer> = RendererSelector.defaultRenderers()) : Renderer<T> {

    override fun render(model: T, sink: HttpServerExchange) {
        for (pr in renderers) {
            if (pr.test(model)) {
                pr.render(model as Any, sink)
                return
            }
        }
    }

    interface SelectableRenderer : Renderer<Any>, Predicate<Any>

    internal class ReaderRenderer : SelectableRenderer {
        val delegate = InputStreamRenderer()

        override fun test(model: Any?) = model is Reader

        override fun render(model: Any, sink: HttpServerExchange) {
            val reader = model as Reader
            SecurityHandler.addContentType(sink)
            val config = sink.getAttachment(Core.CONFIG)
            delegate.render(ReaderInputStream(reader, config.get(Config.CHARSET)), sink)
        }
    }

    internal class InputStreamRenderer : SelectableRenderer {
        override fun test(model: Any?) = model is InputStream

        override fun render(model: Any, sink: HttpServerExchange) {
            SecurityHandler.addContentType(sink)
            ninja.siden.Renderer.ofStream { o: Any, out: OutputStream ->
                val ins = o as InputStream
                try {
                    Streams.copyStream(ins, out, false)
                } finally {
                    IoUtils.safeClose(ins)
                }
            }.render(model, sink)
        }
    }

    internal class URIRenderer : SelectableRenderer {
        val delegate = URLRenderer()

        override fun test(t: Any?) = t is URI

        override fun render(model: Any, sink: HttpServerExchange) {
            val uri = model as URI
            this.delegate.render(uri.toURL(), sink)
        }
    }

    internal class URLRenderer : SelectableRenderer {

        override fun test(t: Any?) = t is URL

        override fun render(model: Any, sink: HttpServerExchange) {
            val url = model as URL
            val config = sink.getAttachment(Core.CONFIG)
            val mm = config.get(Config.MIME_MAPPINGS)
            // TODO proxy?
            val resource = URLResource(url, url.openConnection(), url.path)
            SecurityHandler.addContentType(sink, resource.getContentType(mm))
            resource.serve(sink.responseSender, sink, IoCallback.END_EXCHANGE)
        }
    }

    internal class PathRenderer : SelectableRenderer {
        var delegate = URLRenderer()

        override fun test(t: Any?) = t is Path

        override fun render(model: Any, sink: HttpServerExchange) {
            val path = model as Path
            this.delegate.render(path.toUri().toURL(), sink)
        }
    }

    internal class FileRenderer : SelectableRenderer {
        var delegate = URLRenderer()

        override fun test(t: Any?) = t is File

        override fun render(model: Any, sink: HttpServerExchange) {
            val file = model as File
            if (file.exists()) {
                this.delegate.render(file.toURI().toURL(), sink)
            } else {
                LOG.error(file.absolutePath + " not exists")
                sink.responseCode = 500
            }
        }
    }

    internal class FileChannelRenderer : SelectableRenderer {

        override fun test(t: Any?) = t is FileChannel

        override fun render(model: Any, sink: HttpServerExchange) {
            val channel = model as FileChannel
            SecurityHandler.addContentType(sink)
            sink.responseSender.transferFrom(channel, IoCallback.END_EXCHANGE) // TODO close
        }
    }

    internal class ByteBufferRenderer : SelectableRenderer {

        override fun test(t: Any?) = t is ByteBuffer

        override fun render(model: Any, sink: HttpServerExchange) {
            val s = model as ByteBuffer
            SecurityHandler.addContentType(sink)
            sink.responseSender.send(s)
        }
    }

    internal class ByteArrayRenderer : SelectableRenderer {

        override fun test(t: Any?) = t is ByteArray

        override fun render(model: Any, sink: HttpServerExchange) {
            val ary = model as ByteArray
            SecurityHandler.addContentType(sink)
            sink.responseSender.send(ByteBuffer.wrap(ary))
        }
    }

    class StringRenderer : SelectableRenderer {

        override fun test(t: Any?) = t is String

        override fun render(model: Any, sink: HttpServerExchange) {
            val config = sink.getAttachment(Core.CONFIG)
            val s = model.toString()
            SecurityHandler.addContentType(sink,
                    config.get(Config.DEFAULT_CONTENT_TYPE))
            sink.responseSender.send(s, config.get(Config.CHARSET))
        }
    }

    internal class CharSequenceRenderer : SelectableRenderer {
        var deleagte = StringRenderer()

        override fun test(t: Any?) = t is CharSequence

        override fun render(model: Any, sink: HttpServerExchange) {
            this.deleagte.render(model, sink)
        }
    }

    internal class ToStringRenderer : SelectableRenderer {
        var deleagte = StringRenderer()

        override fun test(t: Any) = true

        override fun render(model: Any, sink: HttpServerExchange) {
            this.deleagte.render(Objects.toString(model), sink)
        }
    }

    companion object {

        internal val LOG = Logger.getLogger(RendererSelector::class.java)

        internal fun defaultRenderers(): List<SelectableRenderer> {
            return Arrays.asList(StringRenderer(), FileRenderer(),
                    PathRenderer(), FileChannelRenderer(),
                    ByteArrayRenderer(), ByteBufferRenderer(),
                    URIRenderer(), URLRenderer(),
                    ReaderRenderer(), InputStreamRenderer(),
                    CharSequenceRenderer(), ToStringRenderer())
        }
    }
}
