package com.github.collinalpert.java2db.utilities;

import java.util.function.Function;

/**
 * @author Collin Alpert
 */
@FunctionalInterface
public interface ThrowableFunction<T, R, E extends Throwable> extends Function<T, R> {

	R run(T t) throws E;

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	@Override
	default R apply(T t) {
		try {
			return this.run(t);
		} catch (Throwable e) {
			e.printStackTrace();

			return null;
		}
	}
}
