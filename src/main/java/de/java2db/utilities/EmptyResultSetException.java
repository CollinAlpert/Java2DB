package de.java2db.utilities;

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
