package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.queries.builder.*;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A query which represents a projection from an {@link SingleEntityQuery} to a single column on the database.
 *
 * @param <E> The entity which the query is supposed to be executed for.
 * @param <R> The return type of the projection this query represents.
 * @author Collin Alpert
 */
public class SingleEntityProjectionQuery<E extends BaseEntity, R> implements Queryable<R> {

	private final Class<R> returnType;
	private final IQueryBuilder<E> queryBuilder;
	private final QueryParameters<E> queryParameters;
	private final TransactionManager transactionManager;

	public SingleEntityProjectionQuery(Class<R> returnType, ProjectionQueryBuilder<E, R> queryBuilder, QueryParameters<E> queryParameters, TransactionManager transactionManager) {
		this.returnType = returnType;
		this.queryBuilder = queryBuilder;
		this.queryParameters = queryParameters;
		this.transactionManager = transactionManager;
	}

	@Override
	public Optional<R> first() {
		return resultHandling(Optional::ofNullable, Optional::empty);
	}

	/**
	 * Executes the query and returns the result as a {@link List}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public List<R> toList() {
		return resultHandling(Collections::singletonList, Collections::emptyList);
	}

	/**
	 * Executes the query and returns the result as a {@link Stream}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public Stream<R> toStream() {
		return resultHandling(Stream::of, Stream::empty);
	}

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public R[] toArray() {
		Function<R, R[]> arrayMapping = databaseObject -> {
			var array = (R[]) Array.newInstance(this.returnType, 1);
			array[0] = databaseObject;

			return array;
		};
		return resultHandling(arrayMapping, () -> (R[]) Array.newInstance(this.returnType, 0));
	}

	/**
	 * Executes a new query and returns the result as a {@link Map}.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @return A map containing the result of the query.
	 */
	@Override
	public <K, V> Map<K, V> toMap(Function<R, K> keyMapping, Function<R, V> valueMapping) {
		return resultHandling(databaseObject -> Collections.singletonMap(keyMapping.apply(databaseObject), valueMapping.apply(databaseObject)), Collections::emptyMap);
	}

	/**
	 * Executes the query and returns the result as a {@link Set}.
	 *
	 * @return A set of entities representing the result rows.
	 */
	@Override
	public Set<R> toSet() {
		return resultHandling(Collections::singleton, Collections::emptySet);
	}

	/**
	 * Responsible for building and returning the individual DQL statement.
	 *
	 * @return The DQL statement which fetches data from the database.
	 */
	@Override
	public String getQuery() {
		return this.queryBuilder.build(this.queryParameters);
	}

	private <D> D resultHandling(Function<R, D> valueConsumer, Supplier<D> defaultValueFactory) {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var result = connection.execute(getQuery());
				if (result.next()) {
					return valueConsumer.apply(result.getObject(1, this.returnType));
				}

				return defaultValueFactory.get();
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return defaultValueFactory.get();
		}
	}
}
