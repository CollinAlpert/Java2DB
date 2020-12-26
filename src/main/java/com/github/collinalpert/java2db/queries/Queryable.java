package com.github.collinalpert.java2db.queries;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Queryable<T> {

	/**
	 * Gets the first value from the database result. This method should be used when only one result is expected.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned. If the value from the database is {@code null}, an empty {@code Optional} is also returned.
	 */
	Optional<T> first();

	/**
	 * Executes the query and returns the result as a {@link List}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	List<T> toList();

	/**
	 * Executes the query and returns the result as a {@link Stream}.
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

	/**
	 * Executes a new query and returns the result as a {@link Map}. This method is equivalent to the call {@code Queryable#toMap(keyMapping, x -> x)}.
	 *
	 * @param keyMapping The field representing the keys of the map.
	 * @param <K>        The type of the field representing the keys.
	 * @return A map containing the result of the query.
	 */
	default <K> Map<K, T> toMap(Function<T, K> keyMapping) {
		return this.toMap(keyMapping, Function.identity());
	}

	/**
	 * Executes a new query and returns the result as a {@link Map}.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @param <K>          The type of the field representing the keys.
	 * @param <V>The       type of the field representing the values.
	 * @return A map containing the result of the query.
	 */
	<K, V> Map<K, V> toMap(Function<T, K> keyMapping, Function<T, V> valueMapping);

	/**
	 * Executes the query and returns the result as a {@link Set}.
	 *
	 * @return A set of entities representing the result rows.
	 */
	Set<T> toSet();

	/**
	 * Responsible for building and returning the individual DQL statement.
	 *
	 * @return The DQL statement which fetches data from the database.
	 */
	String getQuery();
}
