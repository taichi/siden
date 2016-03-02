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

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.util.HttpString
import io.undertow.util.Methods
import ninja.siden.HttpMethod

/**
 * @author taichi
 */
class MethodOverrideHandler(internal var next: HttpHandler) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        if (Methods.POST.equals(exchange.requestMethod)) {
            val newMethod = exchange.requestHeaders.getFirst(HEADER)
            val opt = HttpMethod.find(newMethod)
            if (opt.isPresent) {
                exchange.requestMethod = opt.get()
            } else {
                val fv = exchange.getAttachment(FormDataParser.FORM_DATA)?.getFirst(FORM)
                if (fv?.isFile == false) {
                    exchange.requestMethod = HttpMethod.find(fv?.value).orElse(Methods.POST)
                }
            }
        }
        this.next.handleRequest(exchange)
    }

    companion object {

        @JvmField val HEADER = HttpString("X-HTTP-Method-Override")

        @JvmField val FORM = "_method"
    }
}
