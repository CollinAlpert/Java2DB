package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
	 * This method only exists so this interface can be used as a functional interface.
	 *
	 * @return This method will always return {@code null}.
	 * @deprecated Do not use this method in a specific implementation. Please use the {@link #fetch()} method instead.
	 */
	@Deprecated(since = "4.0")
	@Override
	default T get() {
		return null;
	}
}
