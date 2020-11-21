package com.github.collinalpert.java2db.utilities;

import java.util.function.*;

/**
 * Simple hack to add checked error support to a {@link Supplier}s.
 * It is mainly used in conjunction with {@link Utilities#supplierHandling(ThrowableSupplier, Consumer)} to achieve error handling within a {@code Supplier}.
 *
 * @param <T> The type this supplier is supposed to return.
 * @param <E> The type of exception this {@code Supplier} is expected to throw.
 * @author Collin Alpert
 * @see Supplier
 */
@FunctionalInterface
public interface ThrowableSupplier<T, E extends Throwable> extends Supplier<T> {

	T fetch() throws E;

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	@Override
	default T get() {
		try {
			return fetch();
		} catch (Throwable exception) {
			exception.printStackTrace();

			return null;
		}
	}
}
