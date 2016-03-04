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
package ninja.siden.util

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import java.util.function.UnaryOperator
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author taichi
 */
class ExactlyOnceCloseable(@Volatile internal var delegate: AutoCloseable) : AutoCloseable {

    override fun close() {
        try {
            UPDATER.getAndUpdate(this, UnaryOperator { c -> AutoCloseable { } }) .close()
        } catch (ignore: Exception) {
            Logger.getLogger(ExactlyOnceCloseable::class.java.name).log(
                    Level.FINER, ignore.message, ignore)
        }

    }

    companion object {

        internal val UPDATER = AtomicReferenceFieldUpdater.newUpdater(ExactlyOnceCloseable::class.java, AutoCloseable::class.java,
                "delegate")

        fun wrap(c: AutoCloseable): ExactlyOnceCloseable {
            return ExactlyOnceCloseable(c)
        }
    }
}
