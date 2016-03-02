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
package ninja.siden.jmx

import ninja.siden.util.LongAccumulators
import java.util.*
import java.util.concurrent.atomic.AtomicLongFieldUpdater
import java.util.concurrent.atomic.LongAdder

/**
 * @author taichi
 */
class RequestMeter {

    @Volatile internal var startTime = System.currentTimeMillis()
    internal val totalRequestTime =  LongAdder()
    internal val maxRequestTime= LongAccumulators.max()
    internal val minRequestTime= LongAccumulators.min()
    internal val totalRequests = LongAdder()

    fun <R> record(fn: (RequestMeter) -> R): R {
        val start = System.currentTimeMillis()
        try {
            return fn(this)
        } finally {
            record(System.currentTimeMillis() - start)
        }
    }

    protected fun record(requestTime: Long) {
        synchronized(this) {
            this.totalRequestTime.add(requestTime)
            this.maxRequestTime.accumulate(requestTime)
            this.minRequestTime.accumulate(requestTime)
            this.totalRequests.increment()
        }
    }

    fun reset() {
        startTimeUpdater.set(this, System.currentTimeMillis())
        this.totalRequestTime.reset()
        this.maxRequestTime.reset()
        this.minRequestTime.reset()
        this.totalRequests.reset()
    }

    fun toMetrics(): RequestMetrics {
        return RequestMetrics(Date(this.startTime),
                this.totalRequestTime.sum(), this.maxRequestTime.get(),
                this.minRequestTime.get(), this.totalRequests.sum())
    }

    companion object {
        internal val startTimeUpdater = AtomicLongFieldUpdater.newUpdater(RequestMeter::class.java, "startTime")
    }
}