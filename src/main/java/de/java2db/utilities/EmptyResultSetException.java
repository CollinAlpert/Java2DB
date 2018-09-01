package de.java2db.utilities;

/**
 * @author Collin Alpert
 * <p>
 * Exception that occurres when a {@link java.sql.ResultSet} is empty.
 * </p>
 */
public class EmptyResultSetException extends RuntimeException {

	public EmptyResultSetException() {
		super("The ResultSet is empty");
	}

	public EmptyResultSetException(String message) {
		super(message);
	}
}
