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
package ninja.siden.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.LongAccumulator;

import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(Enclosed.class)
public class LongAccumulatorsTest {

	@RunWith(Theories.class)
	public static class Max {
		@DataPoints
		public static int[][] fixtures = { { 10, 11, 11 }, { 10, 9, 10 } };

		@Theory
		public void test(int[] fixture) {
			LongAccumulator la = LongAccumulators.max();
			la.accumulate(fixture[0]);
			la.accumulate(fixture[1]);
			assertEquals(fixture[2], la.get());
		}
	}

	@RunWith(Theories.class)
	public static class Min {
		@DataPoints
		public static int[][] fixtures = { { 10, 11, 10 }, { 10, 9, 9 },
				{ -10, 7, 7 } };

		@Theory
		public void test(int[] fixture) {
			LongAccumulator la = LongAccumulators.min();
			la.accumulate(fixture[0]);
			la.accumulate(fixture[1]);
			assertEquals(fixture[2], la.get());
		}
	}
}
