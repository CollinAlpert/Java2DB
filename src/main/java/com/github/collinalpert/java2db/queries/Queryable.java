package com.github.collinalpert.java2db.queries;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Queryable<T> extends SingleQueryable<T> {

	/**
	 * Executes the query and returns the result as a {@link List}
	 *
	 * @return A list of entities representing the result rows.
	 */
	List<T> toList();

	/**
	 * Executes the query and returns the result as a {@link Stream}
	 *
	 * @return A list of entities representing the result rows.
	 */
	Stream<T> toStream();

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	T[] toArray();
}
