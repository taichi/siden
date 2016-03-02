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

import ninja.siden.Cookie
import java.util.*

/**
 * @author taichi
 */
data class SidenCookie(val delegate: io.undertow.server.handlers.Cookie) : Cookie {
    override val name: String
        get() = delegate.name
    override var value: String
        get() = delegate.value
        set(value) {
            delegate.value = value
        }
    override var path: String
        get() = delegate.path
        set(value) {
            delegate.path = value
        }
    override var domain: String
        get() = delegate.domain
        set(value) {
            delegate.domain = value
        }
    override var maxAge: Int
        get() = delegate.maxAge
        set(value) {
            delegate.maxAge = value
        }
    override var discard: Boolean
        get() = delegate.isDiscard
        set(value) {
            delegate.isDiscard = value
        }
    override var secure: Boolean
        get() = delegate.isSecure
        set(value) {
            delegate.isSecure = value
        }
    override var version: Int
        get() = delegate.version
        set(value) {
            delegate.version = value
        }
    override var httpOnly: Boolean
        get() = delegate.isHttpOnly
        set(value) {
            delegate.isHttpOnly = value
        }
    override var expires: Date
        get() = delegate.expires
        set(value) {
            delegate.expires = value
        }
    override var comment: String
        get() = delegate.comment
        set(value) {
            delegate.comment = value
        }
}
