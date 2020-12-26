package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.expressions.expression.LambdaExpression;
import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.*;
import com.github.collinalpert.java2db.modules.TableModule;
import com.github.collinalpert.java2db.queries.builder.*;
import com.github.collinalpert.java2db.queries.ordering.OrderTypes;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.lambda2sql.functions.*;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class representing a DQL statement with different options, including where clauses, order by clauses and limits.
 * It also automatically joins foreign keys so the corresponding entities (marked with the {@link ForeignKeyEntity} attribute) can be filled.
 *
 * @author Collin Alpert
 */
public class EntityQuery<E extends BaseEntity> implements Queryable<E> {

	private static final TableModule tableModule = TableModule.getInstance();
	protected final ConnectionConfiguration connectionConfiguration;
	protected final IQueryBuilder<E> queryBuilder;
	protected final QueryParameters<E> queryParameters;
	private final Class<E> type;
	private final Mappable<E> mapper;

	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link com.github.collinalpert.java2db.services.BaseService}.
	 *
	 * @param type The entity to query.
	 */

	public EntityQuery(Class<E> type, ConnectionConfiguration connectionConfiguration) {
		this.type = type;
		this.connectionConfiguration = connectionConfiguration;
		this.queryParameters = new QueryParameters<>();
		this.mapper = IoC.resolveMapper(type, new EntityMapper<>(type));
		this.queryBuilder = new EntityQueryBuilder<>(type);
	}

	//region Where

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	public EntityQuery<E> where(SqlPredicate<E> predicate) {
		this.queryParameters.appendLogicalAndWhereClause(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	public EntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		this.queryParameters.appendLogicalOrWhereClause(predicate);
		return this;
	}

	//endregion

	//region Order By

	/**
	 * Sets a single column as the ORDER BY clause for the DQL statement in an ascending manner.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(SqlFunction<E, ?> function) {
		return this.orderBy(function, OrderTypes.ASCENDING);
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(SqlFunction<E, ?> function, OrderTypes orderType) {
		this.queryParameters.setOrderByClause(function, orderType);

		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(SqlFunction<E, ?>[] functions) {
		if (functions == null) {
			return this;
		}

		return this.orderBy(Arrays.asList(functions), OrderTypes.ASCENDING);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(SqlFunction<E, ?>[] functions, OrderTypes orderType) {
		if (functions == null) {
			return this;
		}

		return this.orderBy(Arrays.asList(functions), orderType);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(List<SqlFunction<E, ?>> functions) {
		return this.orderBy(functions, OrderTypes.ASCENDING);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(List<SqlFunction<E, ?>> functions, OrderTypes orderType) {
		this.queryParameters.setOrderByClause(functions, orderType);

		return this;
	}

	//endregion

	//region Group By

	public EntityQuery<E> groupBy(SqlFunction<E, ?> groupBy) {
		this.queryParameters.setGroupBy(groupBy);

		return this;
	}

	public EntityQuery<E> groupBy(SqlFunction<E, ?>... groupBy) {
		this.queryParameters.setGroupBy(Arrays.asList(groupBy));

		return this;
	}

	//endregion

	//region Then By

	/**
	 * Sets a single column as the ORDER BY clause for the DQL statement in an ascending manner.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(SqlFunction<E, ?> function) {
		return this.thenBy(function, OrderTypes.ASCENDING);
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(SqlFunction<E, ?> function, OrderTypes orderType) {
		this.queryParameters.addOrderByColumns(function, orderType);

		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(SqlFunction<E, ?>[] functions) {
		return this.thenBy(Arrays.asList(functions), OrderTypes.ASCENDING);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(SqlFunction<E, ?>[] functions, OrderTypes orderType) {
		return this.thenBy(Arrays.asList(functions), orderType);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(List<SqlFunction<E, ?>> functions) {
		return this.thenBy(functions, OrderTypes.ASCENDING);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> thenBy(List<SqlFunction<E, ?>> functions, OrderTypes orderType) {
		this.queryParameters.addOrderByColumns(functions, orderType);

		return this;
	}

	//endregion

	//region Limit

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer with an offset.
	 * For example, the call <code>.limit(10, 5)</code> would return the rows 6-15.
	 *
	 * @param limit  The maximum of rows to be returned.
	 * @param offset The offset of the limit.
	 * @return This {@link EntityQuery} object, now with a LIMIT with an OFFSET.
	 */
	public EntityQuery<E> limit(int limit, int offset) {
		this.queryParameters.setLimit(limit);
		this.queryParameters.setLimitOffset(offset);
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link EntityQuery} object, now with a LIMIT.
	 */
	public EntityQuery<E> limit(int limit) {
		this.queryParameters.setLimit(limit);
		return this;
	}

	//endregion

	/**
	 * Adds a DISTINCT modifier to the query.
	 *
	 * @return This {@link EntityQuery} object, now with a DISTINCT clause.
	 */
	public EntityQuery<E> distinct() {
		this.queryParameters.setDistinct();

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

		return new EntityProjectionQuery<>(returnType, queryBuilder, this.queryParameters, this.connectionConfiguration);
	}

	/**
	 * Gets the first record of a result. This method should be used when only one record is expected, i.e. when filtering by a unique identifier such as an id.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	@Override
	public Optional<E> first() {
		this.limit(1);
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.map(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link List}
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public List<E> toList() {
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.mapToList(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link Stream}
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public Stream<E> toStream() {
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.mapToStream(connection.execute(getQuery()));
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
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.mapToArray(connection.execute(getQuery()));
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
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.mapToMap(connection.execute(getQuery()), keyMapping, valueMapping);
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
		try (var connection = new DBConnection(this.connectionConfiguration)) {
			return this.mapper.mapToSet(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();

			return Collections.emptySet();
		}
	}

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
