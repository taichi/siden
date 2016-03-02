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

import io.undertow.server.handlers.MetricsHandler
import java.beans.ConstructorProperties
import java.util.*

/**
 * @author taichi
 */
data class RequestMetrics
@ConstructorProperties("metricsStartDate", "totalRequestTime", "maxRequestTime", "minRequestTime", "totalRequests")
constructor(val metricsStartDate: Date, val totalRequestTime: Long,
            val maxRequestTime: Long, val minRequestTime: Long, val totalRequests: Long) {

    companion object {

        fun to(handler: MetricsHandler): RequestMXBean {
            return object : RequestMXBean {

                override fun reset() {
                    handler.reset()
                }

                override val metrics: RequestMetrics
                    get() {
                        val result = handler.metrics
                        return RequestMetrics(result.metricsStartDate,
                                result.totalRequestTime,
                                result.maxRequestTime.toLong(),
                                result.minRequestTime.toLong(),
                                result.totalRequests)
                    }
            }
        }
    }
}
