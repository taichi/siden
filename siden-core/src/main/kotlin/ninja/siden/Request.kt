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
import java.io.File
import java.util.*

/**
 * @author taichi
 */
interface Request : AttributeContainer {

    val method: HttpMethod

    val path: String

    /**
     * get path parameter

     * @param key
     * *
     * @return
     */
    fun params(key: String): Optional<String>

    fun params(): Map<String, String>

    /**
     * get query parameter

     * @param key
     * *
     * @return
     */
    fun query(key: String): Optional<String>

    fun header(name: String): Optional<String>

    fun headers(name: String): List<String>

    val headers: Map<String, List<String>>

    val cookies: Map<String, Cookie>

    fun cookie(name: String): Optional<Cookie>

    fun form(key: String): Optional<String>

    fun forms(key: String): List<String>

    val forms: Map<String, List<String>>

    fun file(key: String): Optional<File>

    fun files(key: String): List<File>

    val files: Map<String, List<File>>

    fun body(): Optional<String>

    /**
     * get current session or create new session.

     * @return session
     */
    val session: Session

    /**
     * get current session

     * @return session or empty
     */
    val current: Session?

    val xhr: Boolean

    val protocol: String

    val scheme: String

    val raw: HttpServerExchange

}
