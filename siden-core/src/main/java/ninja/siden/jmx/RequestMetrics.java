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
package ninja.siden.jmx;

import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.MetricsHandler.MetricResult;

import java.beans.ConstructorProperties;
import java.util.Date;

/**
 * @author taichi
 */
public class RequestMetrics {

	Date metricsStartDate;

	long totalRequestTime;
	long maxRequestTime;
	long minRequestTime;
	long totalRequests;

	@ConstructorProperties({ "metricsStartDate", "totalRequestTime",
			"maxRequestTime", "minRequestTime", "totalRequests" })
	public RequestMetrics(Date metricsStartDate, long totalRequestTime,
			long maxRequestTime, long minRequestTime, long totalRequests) {
		super();
		this.metricsStartDate = metricsStartDate;
		this.totalRequestTime = totalRequestTime;
		this.maxRequestTime = maxRequestTime;
		this.minRequestTime = minRequestTime;
		this.totalRequests = totalRequests;
	}

	public static RequestMXBean to(MetricsHandler handler) {
		return new RequestMXBean() {

			@Override
			public void reset() {
				handler.reset();
			}

			@Override
			public RequestMetrics getMetrics() {
				MetricResult result = handler.getMetrics();
				return new RequestMetrics(result.getMetricsStartDate(),
						result.getTotalRequestTime(),
						result.getMaxRequestTime(), result.getMinRequestTime(),
						result.getTotalRequests());
			}
		};
	}

	public Date getMetricsStartDate() {
		return this.metricsStartDate;
	}

	public long getTotalRequestTime() {
		return this.totalRequestTime;
	}

	public long getMaxRequestTime() {
		return this.maxRequestTime;
	}

	public long getMinRequestTime() {
		return this.minRequestTime;
	}

	public long getTotalRequests() {
		return this.totalRequests;
	}
}
