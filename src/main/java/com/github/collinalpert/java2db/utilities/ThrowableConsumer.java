package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;

/**
 * @author Collin Alpert
 */
@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> extends Consumer<T> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 */
	@Override
	default void accept(T t) {
		try {
			this.consume(t);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	void consume(T t) throws E;
}
