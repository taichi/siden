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

import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.Route;

/**
 * @author taichi
 */
public class RouteTracker implements Route, RequestMXBean {

	Route original;
	RequestMeter totalResult = new RequestMeter();

	public RouteTracker(Route original) {
		this.original = original;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		return totalResult.apply(m -> original.handle(request, response));
	}

	@Override
	public void reset() {
		this.totalResult.reset();
	}

	@Override
	public RequestMetrics getMetrics() {
		return this.totalResult.toMetrics();
	}
}
