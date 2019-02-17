package com.github.collinalpert.java2db.exceptions;

/**
 * This exception will be thrown when an exception occurs inside of an asynchronous operation and there was no exception handling supplied.
 *
 * @author Collin Alpert
 */
public class AsynchronousOperationException extends RuntimeException {

	public AsynchronousOperationException() {
		super("An asynchronous task threw an exception.");
	}

	public AsynchronousOperationException(Throwable cause) {
		super("An asynchronous task threw an exception.", cause);
	}
}
