package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.exceptions.AsynchronousOperationException;

import java.sql.SQLException;
import java.util.function.*;

/**
 * @author Collin Alpert
 */
public class Utilities {

	/**
	 * Handles an {@code SQLException} that gets thrown inside a {@code Supplier}.
	 *
	 * @param supplier          The {@code Supplier} that throws the exception.
	 * @param exceptionHandling The exception handling supplied for this exception.
	 * @param <V>               The return type of the operation.
	 * @return The original {@code Supplier} but now with the added exception handling.
	 */
	public static <V> Supplier<V> supplierHandling(ThrowableSupplier<V, SQLException> supplier, Consumer<SQLException> exceptionHandling) {
		return () -> {
			try {
				return supplier.fetch();
			} catch (SQLException e) {
				if (exceptionHandling != null) {
					exceptionHandling.accept(e);
					return null;
				} else {
					throw new AsynchronousOperationException(e);
				}
			}
		};
	}

	/**
	 * Handles an {@code SQLException} that gets thrown inside a {@code Runnable}.
	 *
	 * @param runnable          The {@code Runnable} that throws the exception.
	 * @param exceptionHandling The exception handling supplied for this exception.
	 * @return The original {@code Runnable} but now with the added exception handling.
	 */
	public static Runnable runnableHandling(ThrowableRunnable<SQLException> runnable, Consumer<SQLException> exceptionHandling) {
		return () -> {
			try {
				runnable.doAction();
			} catch (SQLException e) {
				if (exceptionHandling != null) {
					exceptionHandling.accept(e);
				} else {
					throw new AsynchronousOperationException(e);
				}
			}
		};
	}

	/**
	 * Tries to perform a certain action while considering a checked exception that could occur.
	 *
	 * @param runnable The {@code Runnable} to try to execute.
	 * @param <E>      The type of checked exception.
	 */
	public static <E extends Throwable> void tryAction(ThrowableRunnable<E> runnable) {
		try {
			runnable.doAction();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException("Underlying method threw an exception.", e);
		}
	}

	/**
	 * Tries to execute a supplier and retrieve its value while considering a checked exception that could occur.
	 *
	 * @param supplier The {@code Supplier} to try to execute.
	 * @param <T>      The type of value to return.
	 * @param <E>      The type of checked exception.
	 * @return The value returned by the supplier, assuming it can be excecuted without throwing an exception.
	 */
	public static <T, E extends Throwable> T tryGetValue(ThrowableSupplier<T, E> supplier) {
		try {
			return supplier.fetch();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException("Underlying method threw an exception.", e);
		}
	}
}
