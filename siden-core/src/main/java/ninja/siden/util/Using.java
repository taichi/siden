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
package ninja.siden.util;

/**
 * @author taichi
 */
public interface Using {

    static <IO extends AutoCloseable, R> R transform(
            ExceptionalSupplier<IO, Exception> supplier,
            ExceptionalFunction<IO, R, Exception> transformer) {
        try (IO t = supplier.get()) {
            return transformer.apply(t);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static <IO extends AutoCloseable> void consume(
            ExceptionalSupplier<IO, Exception> supplier,
            ExceptionalConsumer<IO, Exception> consumer) {
        try (IO t = supplier.get()) {
            consumer.accept(t);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
