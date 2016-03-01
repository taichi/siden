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

import io.undertow.io.IoCallback
import io.undertow.predicate.Predicates
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.*
import io.undertow.util.Headers
import ninja.siden.AssetsCustomizer
import ninja.siden.Config
import org.xnio.OptionMap
import java.io.File
import java.util.*

/**
 * @author taichi
 */
class AssetDef(val path: String, val root: String) : AssetsCustomizer {

    var cacheTime: Int = 0
        private set

    var directoryListing: Boolean = false
        private set

    var canonicalizePaths: Boolean = true
        private set

    internal var cachable = Predicates.truePredicate()

    internal var allowed = Predicates.truePredicate()

    internal var welcomeFiles: Array<out String> = emptyArray()

    internal var loadFrom: ClassLoader? = null

    override fun cacheTime(time: Int): AssetsCustomizer {
        this.cacheTime = requireNotNull(time)
        return this
    }

    override fun directoryListing(allow: Boolean): AssetsCustomizer {
        this.directoryListing = allow
        return this
    }

    fun setCanonicalizePaths(canonicalizePaths: Boolean): AssetsCustomizer {
        this.canonicalizePaths = canonicalizePaths
        return this
    }

    override fun welcomeFiles(vararg files: String): AssetsCustomizer {
        this.welcomeFiles = requireNotNull(files)
        return this
    }

    override fun from(loader: ClassLoader): AssetsCustomizer {
        this.loadFrom = Objects.requireNonNull(loader)
        return this
    }

    fun addTo(ph: PathHandler, config: OptionMap) {
        val rh = ResourceHandler(newResourceManager(config))
        rh.mimeMappings = config.get(Config.MIME_MAPPINGS)
        rh.cacheTime = this.cacheTime
        rh.isDirectoryListingEnabled = this.directoryListing
        rh.isCanonicalizePaths = this.canonicalizePaths
        rh.setWelcomeFiles(*this.welcomeFiles)
        ph.addPrefixPath(this.path, rh)
    }

    protected fun newResourceManager(config: OptionMap): ResourceManager {
        if (this.loadFrom == null) {
            return FileResourceManager(File(this.root),
                    config.get(Config.TRANSFER_MIN_SIZE))
        }
        return ClassPathResourceManager(this.loadFrom, this.root)
    }

    companion object {
        fun useDefaultFavicon(ph: PathHandler) {
            ph.addExactPath("/favicon.ico") { ex ->
                val url = AssetDef::class.java.classLoader.getResource(
                        "favicon.ico")
                val resource = URLResource(url, url.openConnection(), url.path)
                ex.responseHeaders.put(Headers.CONTENT_TYPE, "image/x-icon")
                resource.serve(ex.responseSender, ex, IoCallback.END_EXCHANGE)
            }
        }
    }
}
