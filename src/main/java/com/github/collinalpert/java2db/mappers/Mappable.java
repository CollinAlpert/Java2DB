package com.github.collinalpert.java2db.mappers;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Mappable<T> {

	/**
	 * Maps a {@code ResultSet} to an {@code Optional}. Should be used when only one result is expected from the database.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	Optional<T> map(ResultSet set) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@code List}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code List} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	List<T> mapToList(ResultSet set) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@code Stream}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code Stream} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	Stream<T> mapToStream(ResultSet set) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to an array.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return An array containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	T[] mapToArray(ResultSet set) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@link Map}.
	 *
	 * @param set        The {@code ResultSet} to get the data from.
	 * @param keyMapping The key function of the map.
	 * @param <K>        The type of the keys in the map.
	 * @return A {@code Map} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	default <K> Map<K, T> mapToMap(ResultSet set, Function<T, K> keyMapping) throws SQLException {
		return this.mapToMap(set, keyMapping, Function.identity());
	}

	/**
	 * Maps a {@code ResultSet} to a {@link Map}.
	 *
	 * @param set          The {@code ResultSet} to get the data from.
	 * @param keyMapping   The key function of the map.
	 * @param valueMapping The value function of the map.
	 * @param <K>          The type of the keys in the map.
	 * @param <V>          The type of the values in the map.
	 * @return A {@code Map} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	<K, V> Map<K, V> mapToMap(ResultSet set, Function<T, K> keyMapping, Function<T, V> valueMapping) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@code Set}.
	 *
	 * @param set The {@code ResultSet} to get the data from.
	 * @return A {@code Set} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	Set<T> mapToSet(ResultSet set) throws SQLException;
}
