package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
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
public class EntityQuery<E extends BaseEntity> extends SingleEntityQuery<E> implements Queryable<E> {

	protected OrderTypes orderType = OrderTypes.ASCENDING;
	private List<SqlFunction<E, ?>> orderByClause;
	private Integer limit;
	private int limitOffset;

	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link com.github.collinalpert.java2db.services.BaseService}.
	 *
	 * @param type The entity to query.
	 */
	public EntityQuery(Class<E> type) {
		super(type);
	}

	//region Configuration

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	@Override
	public EntityQuery<E> where(SqlPredicate<E> predicate) {
		super.where(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	@Override
	public EntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		super.orWhere(predicate);
		return this;
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement.
	 *
	 * @param function The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(SqlFunction<E, ?> function) {
		if (function == null) {
			return this;
		}

		return this.orderBy(Collections.singletonList(function));
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

		return this.orderBy(Arrays.asList(functions));
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(List<SqlFunction<E, ?>> functions) {
		this.orderByClause = functions;
		return this;
	}

	/**
	 * Sets an ORDER BY clauses for the DQL statement with a sorting order option.
	 *
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @param function  The column to order by.
	 * @return This {@link EntityQuery} object, now with a ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(OrderTypes orderType, SqlFunction<E, ?> function) {
		if (function == null) {
			return this;
		}

		return this.orderBy(orderType, Collections.singletonList(function));
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement with a sorting order option. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(OrderTypes orderType, SqlFunction<E, ?>[] functions) {
		if (functions == null) {
			return this;
		}

		return orderBy(orderType, Arrays.asList(functions));
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement with a sorting order option. The resulting ORDER BY statement will coalesce the passed columns.
	 *
	 * @param orderType The direction to order by. Can be either ascending or descending.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	public EntityQuery<E> orderBy(OrderTypes orderType, List<SqlFunction<E, ?>> functions) {
		this.orderType = orderType;
		this.orderByClause = functions;
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer with an offset.
	 * For example, the call <code>.limit(10, 5)</code> would return the rows 6-15.
	 *
	 * @param limit  The maximum of rows to be returned.
	 * @param offset The offset of the limit.
	 * @return This {@link EntityQuery} object, now with a LIMIT with an OFFSET.
	 */
	public EntityQuery<E> limit(int limit, int offset) {
		this.limit = limit;
		this.limitOffset = offset;
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link EntityQuery} object, now with a LIMIT.
	 */
	public EntityQuery<E> limit(int limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * Selects only a single column from a table. This is meant if you don't want to fetch an entire entity from the database.
	 *
	 * @param projection The column to project to.
	 * @param <R>        The type of the column you want to retrieve.
	 * @return A queryable containing the projection.
	 */
	@Override
	public <R> Queryable<R> project(SqlFunction<E, R> projection) {
		return new EntityProjectionQuery<>(projection, this);
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
		this.limit(1);
		return super.first();
	}

	/**
	 * Executes the query and returns the result as a {@link List}
	 *
	 * @return A list of entities representing the result rows.
	 */
	@Override
	public List<E> toList() {
		try (var connection = new DBConnection()) {
			return super.mapper.mapToList(connection.execute(getQuery()));
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
		try (var connection = new DBConnection()) {
			return super.mapper.mapToStream(connection.execute(getQuery()));
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
		try (var connection = new DBConnection()) {
			return super.mapper.mapToArray(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return (E[]) Array.newInstance(super.type, 0);
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
		try (var connection = new DBConnection()) {
			return super.mapper.mapToMap(connection.execute(getQuery()), keyMapping, valueMapping);
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
		try (var connection = new DBConnection()) {
			return super.mapper.mapToSet(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();

			return Collections.emptySet();
		}
	}

	/**
	 * Creates the query clauses for a DQL statement. This contains constraints like a WHERE, an ORDER BY and a LIMIT statement.
	 *
	 * @param tableName The table name which is targeted.
	 * @return A string containing the clauses which can then be appended to the end of a DQL statement.
	 */
	@Override
	protected String getQueryClauses(String tableName) {
		var builder = new StringBuilder();

		buildWhereClause(builder, tableName);

		if (this.orderByClause != null && this.orderByClause.size() > 0) {
			builder.append(" order by ");

			if (this.orderByClause.size() == 1) {
				builder.append(Lambda2Sql.toSql(this.orderByClause.get(0), tableName));
			} else {
				var joiner = new StringJoiner(", ", "coalesce(", ")");
				for (SqlFunction<E, ?> orderByFunction : this.orderByClause) {
					joiner.add(Lambda2Sql.toSql(orderByFunction, tableName));
				}

				builder.append(joiner.toString());
			}

			builder.append(" ").append(this.orderType.getSql());
		}

		if (this.limit != null) {
			builder.append(" limit ").append(this.limitOffset).append(", ").append(this.limit);
		}

		return builder.toString();
	}
}
