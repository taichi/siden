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

import java.util.Date;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import ninja.siden.util.ExceptionalConsumer;
import ninja.siden.util.ExceptionalFunction;
import ninja.siden.util.LongAccumulators;

/**
 * @author taichi
 */
public class RequestMeter {

	static final AtomicLongFieldUpdater<RequestMeter> startTimeUpdater = AtomicLongFieldUpdater
			.newUpdater(RequestMeter.class, "startTime");

	volatile long startTime;
	LongAdder totalRequestTime;
	LongAccumulator maxRequestTime;
	LongAccumulator minRequestTime;
	LongAdder totalRequests;

	public RequestMeter() {
		this.startTime = System.currentTimeMillis();
		this.totalRequestTime = new LongAdder();
		this.maxRequestTime = LongAccumulators.max();
		this.minRequestTime = LongAccumulators.min();
		this.totalRequests = new LongAdder();
	}

	protected void accept(final long requestTime) {
		this.totalRequestTime.add(requestTime);
		this.maxRequestTime.accumulate(requestTime);
		this.minRequestTime.accumulate(requestTime);
		this.totalRequests.increment();
	}

	public <EX extends Exception> void accept(
			ExceptionalConsumer<RequestMeter, EX> fn) throws EX {
		apply(m -> {
			fn.accept(m);
			return null;
		});
	}

	public <R, EX extends Exception> R apply(
			ExceptionalFunction<RequestMeter, R, EX> fn) throws EX {
		final long start = System.currentTimeMillis();
		try {
			return fn.apply(this);
		} finally {
			accept(System.currentTimeMillis() - start);
		}
	}

	public void reset() {
		startTimeUpdater.set(this, System.currentTimeMillis());
		this.totalRequestTime.reset();
		this.maxRequestTime.reset();
		this.minRequestTime.reset();
		this.totalRequests.reset();
	}

	public RequestMetrics toMetrics() {
		return new RequestMetrics(new Date(this.startTime),
				this.totalRequestTime.sum(), this.maxRequestTime.get(),
				this.minRequestTime.get(), this.totalRequests.sum());
	}
}