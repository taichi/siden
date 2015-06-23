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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author taichi
 */
public class ExactlyOnceCloseable implements AutoCloseable {

	static final AtomicReferenceFieldUpdater<ExactlyOnceCloseable, AutoCloseable> UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(ExactlyOnceCloseable.class, AutoCloseable.class,
					"delegate");

	volatile AutoCloseable delegate;

	public ExactlyOnceCloseable(AutoCloseable closeable) {
		this.delegate = closeable;
	}

	public static ExactlyOnceCloseable wrap(AutoCloseable c) {
		return new ExactlyOnceCloseable(c);
	}

	@Override
	public void close() {
		try {
			UPDATER.getAndUpdate(this, c -> () -> {
			}).close();
		} catch (Exception ignore) {
			Logger.getLogger(ExactlyOnceCloseable.class.getName()).log(
					Level.FINER, ignore.getMessage(), ignore);
		}
	}
}
