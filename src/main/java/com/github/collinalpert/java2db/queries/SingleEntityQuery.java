package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.expressions.expression.LambdaExpression;
import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.*;
import com.github.collinalpert.java2db.modules.TableModule;
import com.github.collinalpert.java2db.queries.builder.*;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.lambda2sql.functions.*;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Collin Alpert
 */
public class SingleEntityQuery<E extends BaseEntity> implements Queryable<E> {

	private static final TableModule tableModule = TableModule.getInstance();
	protected final QueryParameters<E> queryParameters;
	protected final IQueryBuilder<E> queryBuilder;
	protected final TransactionManager transactionManager;
	private final Class<E> type;
	private final Mappable<E> mapper;

	public SingleEntityQuery(Class<E> type, TransactionManager transactionManager) {
		this.type = type;
		this.queryParameters = new QueryParameters<>();
		this.mapper = IoC.resolveMapper(type, new EntityMapper<>(type));
		this.queryBuilder = new SingleEntityQueryBuilder<>(type);
		this.transactionManager = transactionManager;
	}

	//region Configuration

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	public SingleEntityQuery<E> where(SqlPredicate<E> predicate) {
		this.queryParameters.appendLogicalAndWhereClause(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	public SingleEntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		this.queryParameters.appendLogicalOrWhereClause(predicate);
		return this;
	}

	/**
	 * Selects only a single column from a table. This is meant if you don't want to fetch an entire entity from the database.
	 *
	 * @param projection The column to project to.
	 * @param <R>        The type of the column you want to retrieve.
	 * @return A queryable containing the projection.
	 */
	public <R> Queryable<R> project(SqlFunction<E, R> projection) {
		@SuppressWarnings("unchecked") var returnType = (Class<R>) LambdaExpression.parse(projection).getBody().getResultType();
		var queryBuilder = new ProjectionQueryBuilder<>(projection, this.getTableName(), (QueryBuilder<E>) this.queryBuilder);

		return new SingleEntityProjectionQuery<>(returnType, queryBuilder, this.queryParameters, this.transactionManager);
	}

	//endregion

	/**
	 * Gets the first record of a result. This method should be used when only one record is expected, i.e. when filtering by a unique identifier such as an id.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	@Override
	public Optional<E> first() {
		try {
			return transactionManager.transactAndReturn(connection -> {
				return this.mapper.map(connection.execute(getQuery()));
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link List}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public List<E> toList() {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var mappedValue = this.mapper.map(connection.execute(getQuery()));
				return mappedValue.map(Collections::singletonList).orElse(Collections.emptyList());
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link Stream}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public Stream<E> toStream() {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var mappedValue = this.mapper.map(connection.execute(getQuery()));
				return mappedValue.stream();
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return Stream.empty();
		}
	}

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E[] toArray() {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var mappedValue = this.mapper.map(connection.execute(getQuery()));

				return mappedValue.map(v -> {
					var array = (E[]) Array.newInstance(this.type, 1);
					array[0] = v;

					return array;
				}).orElse((E[]) Array.newInstance(this.type, 0));
			});
		} catch (SQLException e) {
			e.printStackTrace();

			return (E[]) Array.newInstance(this.type, 0);
		}
	}

	/**
	 * Executes a new query and returns the result as a {@link Map}.
	 *
	 * @param keyMapping   The field representing the keys of the map.
	 * @param valueMapping The field representing the values of the map.
	 * @return A map containing the result of the query.
	 */
	@Override
	public <K, V> Map<K, V> toMap(Function<E, K> keyMapping, Function<E, V> valueMapping) {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var mappedValue = this.mapper.map(connection.execute(getQuery()));
				return mappedValue.map(v -> Collections.singletonMap(keyMapping.apply(v), valueMapping.apply(v))).orElse(Collections.emptyMap());
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link Set}.
	 *
	 * @return A set of entities representing the result rows.
	 */
	@Override
	public Set<E> toSet() {
		try {
			return transactionManager.transactAndReturn(connection -> {
				var mappedValue = this.mapper.map(connection.execute(getQuery()));
				return mappedValue.map(Collections::singleton).orElse(Collections.emptySet());
			});
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
	}

	/**
	 * Builds the query from the set query options.
	 *
	 * @return The DQL statement for getting data from the database.
	 */
	@Override
	public String getQuery() {
		return this.queryBuilder.build(this.queryParameters);
	}

	/**
	 * Gets the table name which this query targets.
	 *
	 * @return The table name which this query targets.
	 */
	protected String getTableName() {
		return tableModule.getTableName(this.type);
	}
}
