package com.github.collinalpert.java2db.utilities;

import java.util.function.Consumer;

/**
 * Simple hack to add checked error support to a {@link Runnable}s.
 * It is mainly used in conjunction with {@link Utilities#runnableHandling(ThrowableRunnable, Consumer)} to achieve error handling within a {@code Runnable}.
 *
 * @param <E> The type of exception this {@code Runnable} is expected to throw.
 * @author Collin Alpert
 * @see Runnable
 */
@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> extends Runnable {

	void doAction() throws E;

	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	default void run() {
		try {
			doAction();
		} catch (Throwable exception) {
			exception.printStackTrace();
		}
	}
}
