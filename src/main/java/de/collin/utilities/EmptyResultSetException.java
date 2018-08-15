package de.collin.utilities;

/**
 * @author Collin Alpert
 */
public class EmptyResultSetException extends RuntimeException {

	public EmptyResultSetException() {
		super("The ResultSet is empty");
	}

	public EmptyResultSetException(String message) {
		super(message);
	}
}
