package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;

/**
 * Simple hack to add checked error support to a {@link Runnable}s.
 * It is used in conjunction with {@link Utilities#runnableHandling(ThrowableRunnable, Consumer)} to achieve error handling within a {@code Runnable}.
 *
 * @param <E> The type of exception this {@code Runnable} is expected to throw.
 * @author Collin Alpert
 * @see Runnable
 */
@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> extends Runnable {

	void doAction() throws E;

	@Override
	// Dirty hack! Don't use this method in this specific implementation!!
	default void run() {
	}
}
