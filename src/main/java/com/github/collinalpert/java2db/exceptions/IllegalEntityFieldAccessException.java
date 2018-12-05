package com.github.collinalpert.java2db.exceptions;

/**
 * An exception which occurs when Java2DB tries to access a field in an entity and fails.
 * Is a sort of wrapper around {@link IllegalAccessException}.
 *
 * @author Collin Alpert
 */
public class IllegalEntityFieldAccessException extends RuntimeException {

	public IllegalEntityFieldAccessException(String field, String entity, String underlyingMessage) {
		super(String.format("Unable to get field '%s' from entity '%s'. Please check your entity.\nUnderlying message is: %s", field, entity, underlyingMessage));
	}
}
