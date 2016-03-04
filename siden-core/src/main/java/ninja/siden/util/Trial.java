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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author taichi
 */
public interface Trial<T> {

    boolean success();

    boolean failure();

    <U> U either(Function<? super T, ? extends U> onSuccess,
                 Function<Exception, ? extends U> onFailure);

    Optional<Trial<T>> filter(Predicate<? super T> predicate);

    T get();

    T orElse(T other);

    T orElseGet(Supplier<? extends T> other);

    <U> Trial<U> map(Function<? super T, ? extends U> mapper);

    <U> Trial<U> flatMap(Function<? super T, Trial<U>> mapper);

    static class Success<T> implements Trial<T> {
        final T value;

        Success(T value) {
            this.value = value;
        }

        @Override
        public boolean success() {
            return true;
        }

        @Override
        public boolean failure() {
            return false;
        }

        @Override
        public <U> U either(Function<? super T, ? extends U> onSuccess,
                            Function<Exception, ? extends U> onFailure) {
            return onSuccess.apply(this.value);
        }

        @Override
        public Optional<Trial<T>> filter(Predicate<? super T> predicate) {
            return predicate.test(this.value) ? Optional.of(this) : Optional
                    .empty();
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public T orElse(T other) {
            return this.value;
        }

        @Override
        public T orElseGet(Supplier<? extends T> other) {
            return this.value;
        }

        @Override
        public <U> Trial<U> map(Function<? super T, ? extends U> mapper) {
            return new Success<U>(mapper.apply(this.value));
        }

        @Override
        public <U> Trial<U> flatMap(Function<? super T, Trial<U>> mapper) {
            return mapper.apply(this.value);
        }
    }

    static class Failure<T> implements Trial<T> {
        static class FailureException extends RuntimeException {
            private static final long serialVersionUID = 6537304884970413146L;

            public FailureException(Exception exception) {
                super(exception);
            }
        }

        final Exception exception;

        Failure(Exception exception) {
            this.exception = exception;
        }

        @Override
        public boolean success() {
            return false;
        }

        @Override
        public boolean failure() {
            return true;
        }

        @Override
        public <U> U either(Function<? super T, ? extends U> onSuccess,
                            Function<Exception, ? extends U> onFailure) {
            return onFailure.apply(this.exception);
        }

        @Override
        public Optional<Trial<T>> filter(Predicate<? super T> predicate) {
            return Optional.empty();
        }

        @Override
        public T get() {
            throw new FailureException(this.exception);
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public T orElseGet(Supplier<? extends T> other) {
            return other.get();
        }

        @Override
        public <U> Trial<U> map(Function<? super T, ? extends U> mapper) {
            return new Failure<U>(this.exception);
        }

        @Override
        public <U> Trial<U> flatMap(Function<? super T, Trial<U>> mapper) {
            return new Failure<U>(exception);
        }
    }

    @FunctionalInterface
    public interface ExceptionalFunction<T, R, EX extends Exception> {

        R apply(T t) throws EX;
    }

    public static <T, R, E extends Exception> Function<T, Trial<R>> of(ExceptionalFunction<T, R, E> fn) {
        return t -> {
            try {
                return new Success<R>(fn.apply(t));
            } catch (Exception e) {
                return new Failure<R>(e);
            }
        };
    }
}
