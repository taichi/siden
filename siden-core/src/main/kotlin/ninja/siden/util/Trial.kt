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
package ninja.siden.util

import java.util.Optional

/**
 * @author taichi
 */
interface Trial<T> {

    fun success(): Boolean

    fun failure(): Boolean

    fun <U> either(onSuccess: (T) -> U, onFailure: (Exception) -> U): U

    fun filter(predicate: (T) -> Boolean): Optional<Trial<T>>

    fun get(): T

    fun orElse(other: T): T

    fun orElseGet(other: () -> T): T

    fun <U> map(mapper: (T) -> U): Trial<U>

    fun <U> flatMap(mapper: (T) -> Trial<U>): Trial<U>

    class Success<T> internal constructor(internal val value: T) : Trial<T> {

        override fun success(): Boolean {
            return true
        }

        override fun failure(): Boolean {
            return false
        }

        override fun <U> either(onSuccess: (T) -> U, onFailure: (Exception) -> U): U {
            return onSuccess(this.value)
        }

        override fun filter(predicate: (T) -> Boolean): Optional<Trial<T>> {
            return if (predicate(this.value))
                Optional.of<Trial<T>>(this)
            else
                Optional.empty<Trial<T>>()
        }

        override fun get(): T {
            return this.value
        }

        override fun orElse(other: T): T {
            return this.value
        }

        override fun orElseGet(other: () -> T): T {
            return this.value
        }

        override fun <U> map(mapper: (T) -> U): Trial<U> {
            return Success(mapper(this.value))
        }

        override fun <U> flatMap(mapper: (T) -> Trial<U>): Trial<U> {
            return mapper(this.value)
        }
    }

    class Failure<T> internal constructor(internal val exception: Exception) : Trial<T> {
        internal class FailureException(exception: Exception) : RuntimeException(exception) {
            companion object {
                private val serialVersionUID = 6537304884970413146L
            }
        }

        override fun success(): Boolean {
            return false
        }

        override fun failure(): Boolean {
            return true
        }

        override fun <U> either(onSuccess: (T) -> U, onFailure: (Exception) -> U): U {
            return onFailure(this.exception)
        }

        override fun filter(predicate: (T) -> Boolean): Optional<Trial<T>> {
            return Optional.empty<Trial<T>>()
        }

        override fun get(): T {
            throw FailureException(this.exception)
        }

        override fun orElse(other: T): T {
            return other
        }

        override fun orElseGet(other: () -> T): T {
            return other()
        }

        override fun <U> map(mapper: (T) -> U): Trial<U> {
            return Failure(this.exception)
        }

        override fun <U> flatMap(mapper: (T) -> Trial<U>): Trial<U> {
            return Failure(exception)
        }
    }

    companion object {
        fun <T, R> of(fn: (T) -> R): (T) -> Trial<R> {
            return { t ->
                try {
                    Success(fn(t))
                } catch (e: Exception) {
                    Failure<R>(e)
                }
            }
        }
    }
}
