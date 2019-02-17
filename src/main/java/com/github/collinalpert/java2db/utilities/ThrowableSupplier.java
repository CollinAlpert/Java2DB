package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple hack to add checked error support to a {@link Supplier}s.
 * It is used in conjunction with {@link Utilities#supplierHandling(ThrowableSupplier, Consumer)} to achieve error handling within a {@code Supplier}.
 *
 * @param <T> The type this supplier is supposed to return.
 * @param <E> The type of exception this {@code Supplier} is expected to throw.
 * @author Collin Alpert
 * @see Supplier
 */
@FunctionalInterface
public interface ThrowableSupplier<T, E extends Throwable> extends Supplier<T> {

	T fetch() throws E;

	@Override
	// Dirty hack! Don't use this method in this specific implementation!!
	default T get() {
		return null;
	}
}
