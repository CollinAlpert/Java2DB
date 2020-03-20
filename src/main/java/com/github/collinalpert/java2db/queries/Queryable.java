package com.github.collinalpert.java2db.queries;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Queryable<T> extends SingleQueryable<T> {

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
}
