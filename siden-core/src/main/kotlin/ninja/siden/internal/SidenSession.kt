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
import io.undertow.server.session.SessionConfig
import ninja.siden.AttributeContainer
import ninja.siden.Session
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author taichi
 */
open class SidenSession(internal val exchange: HttpServerExchange?,
                        internal val delegate: io.undertow.server.session.Session) : Session {

    @Suppress("UNCHECKED_CAST")
    override fun <T> attr(key: String, newone: T): Optional<T> {
        return Optional.ofNullable(this.delegate.setAttribute(key, newone) as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> attr(key: String): Optional<T> {
        return Optional.ofNullable(this.delegate.getAttribute(key) as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> remove(key: String): Optional<T> {
        return Optional.ofNullable(this.delegate.removeAttribute(key) as T)
    }

    override fun iterator(): Iterator<AttributeContainer.Attr> {
        val names = this.delegate.attributeNames.iterator()
        return object : Iterator<AttributeContainer.Attr> {
            override fun hasNext(): Boolean {
                return names.hasNext()
            }

            override fun next(): AttributeContainer.Attr {
                val name = names.next()
                return object : AttributeContainer.Attr {

                    override val name: String
                        get() = name

                    override fun <T> value(): T {
                        val o = this@SidenSession.attr<T>(name)
                        return o.get()
                    }

                    override fun <T> remove(): T {
                        val o = this@SidenSession.remove<T>(name)
                        return o.get()
                    }
                }
            }
        }
    }

    override val id: String
        get() = this.delegate.id

    override fun invalidate() {
        this.delegate.invalidate(this.exchange)
    }

    override fun regenerate(): Session {
        val config = this.exchange?.getAttachment(SessionConfig.ATTACHMENT_KEY)
        this.delegate.changeSessionId(this.exchange, config)
        return this
    }

    override val raw: io.undertow.server.session.Session
        get() = this.delegate

    @SuppressWarnings("resource")
    override fun toString(): String {
        val fmt = Formatter()
        fmt.format("Session{ id:$id")
        val sdf = SimpleDateFormat()
        fmt.format(",CreatationTime:%s",
                sdf.format(Date(raw.creationTime)))
        fmt.format(",LastAccessedTime:%s",
                sdf.format(Date(raw.lastAccessedTime)))
        fmt.format(",values:[")
        forEach { a -> fmt.format(" %s=%s", a.name, a.value<Any>()) }
        fmt.format("]}")

        return fmt.toString()
    }
}
