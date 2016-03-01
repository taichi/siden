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
package ninja.siden.def

import io.undertow.Handlers
import io.undertow.predicate.PredicatesHandler
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.form.EagerFormParsingHandler
import io.undertow.server.handlers.form.FormEncodedDataDefinition
import io.undertow.server.handlers.form.FormParserFactory
import io.undertow.server.handlers.form.MultiPartParserDefinition
import io.undertow.server.session.InMemorySessionManager
import io.undertow.server.session.SessionAttachmentHandler
import io.undertow.server.session.SessionCookieConfig
import ninja.siden.App
import ninja.siden.Config
import ninja.siden.internal.*
import org.xnio.OptionMap

/**
 * @author taichi
 */
open class DefaultAppBuilder @JvmOverloads constructor(val config: OptionMap,
                                                       val assets: PathHandler = Handlers.path(),
                                                       val router: RoutingHandler = RoutingHandler(assets),
                                                       val subapp: PathHandler = PathHandler(router),
                                                       val websockets: PredicatesHandler = PredicatesHandler(subapp),
                                                       val filters: FiltersHandler = FiltersHandler(websockets)
) : AppBuilder {

    override fun begin() {
        if (config.get(Config.SIDEN_FAVICON, false)) {
            AssetDef.useDefaultFavicon(this.assets)
        }
    }

    override fun apply(context: AppContext, def: AssetDef) {
        def.addTo(this.assets, this.config)
    }

    override fun apply(context: AppContext, def: RoutingDef) {
        def.addTo(this.router)
    }

    override fun apply(context: AppContext, def: ErrorCodeRoutingDef) {
        this.router.add(def)
    }

    override fun apply(context: AppContext, def: ExceptionalRoutingDef<*>) {
        this.router.add(def)
    }

    override fun apply(context: AppContext, def: SubAppDef) {
        val kids = DefaultAppBuilder(this.config)
        def.app.accept(AppContext(context, def), kids)
        this.subapp.addPrefixPath(def.prefix, kids.filters)
    }

    override fun apply(context: AppContext, def: WebSocketDef) {
        def.addTo(this.websockets)
    }

    override fun apply(context: AppContext, def: FilterDef) {
        this.filters.add(def)
    }

    override fun end(root: App): HttpHandler {
        return makeSharedHandlers(root, this.config, this.filters)
    }

    protected open fun makeSharedHandlers(root: App, config: OptionMap,
                                          next: HttpHandler): HttpHandler {
        var hh = next
        if (config.get(Config.METHOD_OVERRIDE)) {
            hh = MethodOverrideHandler(hh)
        }
        hh = makeSessionHandler(root, config, hh)
        hh = makeFormHandler(root, config, hh)

        if (Config.isInDev(config)) {
            hh = Handlers.disableCache(hh)
        } else {
            val gsh = Handlers.gracefulShutdown(hh)
            root.stopOn({
                gsh.shutdown()
                try {
                    gsh.awaitShutdown(config.get(Config.WAIT_FOR_GRACEFUL_SHUTDOWN, 500))
                } catch (e: InterruptedException) {
                    // ignore
                }
            })
            hh = gsh
        }

        hh = SecurityHandler(hh)
        return Core(config, hh)
    }

    protected open fun makeSessionHandler(root: App, config: OptionMap, next: HttpHandler): HttpHandler {
        val sessionManager = InMemorySessionManager(
                "SessionManagerOfSiden", config.get(Config.MAX_SESSIONS))
        sessionManager.setDefaultSessionTimeout(config.get(Config.DEFAULT_SESSION_TIMEOUT_SECONDS))
        val sessionConfig = SessionCookieConfig()
        sessionConfig.cookieName = config.get(Config.SESSION_COOKIE_NAME)
        return SessionAttachmentHandler(next, sessionManager, sessionConfig)
    }

    protected open fun makeFormHandler(root: App, config: OptionMap,
                                       next: HttpHandler): HttpHandler {
        val builder = FormParserFactory.builder(false)
        val form = FormEncodedDataDefinition()
        val cn = config.get(Config.CHARSET).name()
        form.defaultEncoding = cn

        val mult = MultiPartParserDefinition(config.get(Config.TEMP_DIR))
        mult.defaultEncoding = cn
        mult.maxIndividualFileSize = config.get(Config.MAX_FILE_SIZE)

        builder.addParsers(form, mult)

        val efp = EagerFormParsingHandler(
                builder.build())
        return efp.setNext(next)
    }
}
