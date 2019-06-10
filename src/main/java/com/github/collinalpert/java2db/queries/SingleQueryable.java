package com.github.collinalpert.java2db.queries;

import java.util.Optional;

/**
 * @author Collin Alpert
 */
public interface SingleQueryable<T> {

	/**
	 * Gets the first value from the database result. This method should be used when only one result is expected.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned. If the value from the database is {@code null}, an empty {@code Optional} is also returned.
	 */
	Optional<T> first();

	/**
	 * Responsible for building and returning the individual DQL statement.
	 *
	 * @return The DQL statement which fetches data from the database.
	 */
	String getQuery();
}
