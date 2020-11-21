package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.mappers.*;
import com.github.collinalpert.java2db.utilities.ThrowableSupplier;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public class StoredProcedureQuery<T> implements Queryable<T> {

	private final Class<T> underlyingClass;
	private final Mappable<T> mapper;
	private final DBConnection connection;
	private final String procedureName;
	private final Object[] arguments;

	public StoredProcedureQuery(Class<T> underlyingClass, DBConnection connection, String procedureName, Object[] arguments) {
		this.connection = connection;
		this.procedureName = procedureName;
		this.arguments = arguments;
		this.underlyingClass = underlyingClass;
		this.mapper = new FieldMapper<>(underlyingClass);
	}

	/**
	 * Executes the query and returns the result as a {@link List}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public List<T> toList() {
		return materializeInternal(() -> this.mapper.mapToList(this.connection.execute(getQuery(), this.arguments)), Collections::emptyList);
	}

	/**
	 * Executes the query and returns the result as a {@link Stream}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public Stream<T> toStream() {
		return materializeInternal(() -> this.mapper.mapToStream(this.connection.execute(getQuery(), this.arguments)), Stream::empty);
	}

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return materializeInternal(() -> this.mapper.mapToArray(this.connection.execute(getQuery(), this.arguments)), () -> (T[]) Array.newInstance(this.underlyingClass, 0));
	}

	/**
	 * Executes a new query and returns the result as a {@link Map}.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @return A map containing the result of the query.
	 */
	@Override
	public <K, V> Map<K, V> toMap(Function<T, K> keyMapping, Function<T, V> valueMapping) {
		return materializeInternal(() -> this.mapper.mapToMap(this.connection.execute(getQuery(), this.arguments), keyMapping, valueMapping), Collections::emptyMap);
	}

	/**
	 * Executes the query and returns the result as a {@link Set}.
	 *
	 * @return A set of entities representing the result rows.
	 */
	@Override
	public Set<T> toSet() {
		return materializeInternal(() -> this.mapper.mapToSet(this.connection.execute(getQuery(), this.arguments)), Collections::emptySet);
	}

	/**
	 * Gets the first value from the database result. This method should be used when only one result is expected.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned. If the value from the database is {@code null}, an empty {@code Optional} is also returned.
	 */
	@Override
	public Optional<T> first() {
		return materializeInternal(() -> this.mapper.map(this.connection.execute(getQuery(), this.arguments)), Optional::empty);
	}

	/**
	 * Responsible for building and returning the individual DQL statement.
	 *
	 * @return The DQL statement which fetches data from the database.
	 */
	@Override
	public String getQuery() {
		var joiner = new StringJoiner(",");
		for (int i = 0; i < this.arguments.length; i++) {
			joiner.add("?");
		}

		return String.format("call %s(%s);", this.procedureName, joiner.toString());
	}

	private <R> R materializeInternal(ThrowableSupplier<R, SQLException> valueFunction, Supplier<R> exceptionFunction) {
		try {
			return valueFunction.fetch();
		} catch (SQLException e) {
			e.printStackTrace();
			return exceptionFunction.get();
		}
	}
}
