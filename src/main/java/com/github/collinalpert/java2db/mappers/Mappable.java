package com.github.collinalpert.java2db.mappers;

import com.github.collinalpert.java2db.entities.BaseEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public interface Mappable<T extends BaseEntity> {

	/**
	 * Maps a {@code ResultSet} to an {@code Optional}. Should be used when only one result is expected from the database.
	 *
	 * @param set     The {@code ResultSet} to get the data from.
	 * @param aliases A map of column aliases needed to retrieve column data from the {@code ResultSet}.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	Optional<T> map(ResultSet set, Map<String, String> aliases) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@code List}.
	 *
	 * @param set     The {@code ResultSet} to get the data from.
	 * @param aliases A map of column aliases needed to retrieve column data from the {@code ResultSet}.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	List<T> mapToList(ResultSet set, Map<String, String> aliases) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to a {@code Stream}.
	 *
	 * @param set     The {@code ResultSet} to get the data from.
	 * @param aliases A map of column aliases needed to retrieve column data from the {@code ResultSet}.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	Stream<T> mapToStream(ResultSet set, Map<String, String> aliases) throws SQLException;

	/**
	 * Maps a {@code ResultSet} to an array.
	 *
	 * @param set     The {@code ResultSet} to get the data from.
	 * @param aliases A map of column aliases needed to retrieve column data from the {@code ResultSet}.
	 * @return An {@code Optional} containing the {@code ResultSet}s data.
	 * @throws SQLException In case the {@code ResultSet} can't be read.
	 */
	T[] mapToArray(ResultSet set, Map<String, String> aliases) throws SQLException;
}
