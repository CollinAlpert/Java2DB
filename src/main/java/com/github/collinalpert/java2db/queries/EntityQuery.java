package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.ForeignKeyReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.Mappable;
import com.github.collinalpert.java2db.modules.FieldModule;
import com.github.collinalpert.java2db.modules.TableModule;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;
import com.trigersoft.jaque.expression.LambdaExpression;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * A class representing a DQL statement with different options, including where clauses, order by clauses and limits.
 * It also automatically joins foreign keys so the corresponding entities (marked with the {@link ForeignKeyEntity} attribute) can be filled.
 *
 * @author Collin Alpert
 */
public class EntityQuery<E extends BaseEntity> implements Queryable<E> {

	private static final TableModule tableModule;

	static {
		tableModule = new TableModule();
	}

	private final Class<E> type;
	private final Mappable<E> mapper;
	private SqlPredicate<E> whereClause;
	private SqlFunction<E, ?>[] orderByClause;
	private OrderTypes orderType;
	private Integer limit;
	private int limitOffset;

	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link com.github.collinalpert.java2db.services.BaseService}.
	 *
	 * @param type   The entity to query.
	 * @param mapper The mapper for mapping entities.
	 */
	public EntityQuery(Class<E> type, Mappable<E> mapper) {
		this.type = type;
		this.mapper = mapper;
	}

	//region Configuration

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	public EntityQuery<E> where(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.and(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	public EntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.or(predicate);
		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns, if more than one is supplied.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@SafeVarargs
	public final EntityQuery<E> orderBy(SqlFunction<E, ?>... functions) {
		return orderBy(OrderTypes.ASCENDING, functions);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns, if more than one is supplied.
	 *
	 * @param type      The type of ordering that should be applied.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link EntityQuery} object, now with a coalesced ORDER BY clause.
	 */
	@SafeVarargs
	public final EntityQuery<E> orderBy(OrderTypes type, SqlFunction<E, ?>... functions) {
		this.orderByClause = functions;
		this.orderType = type;
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
	public <R> Queryable<R> project(SqlFunction<E, R> projection) {
		var lambda = LambdaExpression.parse(projection);
		@SuppressWarnings("unchecked")
		var returnType = (Class<R>) lambda.getBody().getResultType();
		return new EntityProjectionQuery<>(returnType, projection, this);
	}

	//endregion

	/**
	 * Gets the first record of a result. This method should be used when only one record is expected, i.e. when filtering by a unique identifier such as an id.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	@Override
	public Optional<E> getFirst() {
		try (var connection = new DBConnection()) {
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
		try (var connection = new DBConnection()) {
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
		try (var connection = new DBConnection()) {
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
		try (var connection = new DBConnection()) {
			return this.mapper.mapToArray(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return (E[]) Array.newInstance(this.type, 0);
		}
	}

	/**
	 * Builds the query from the set query options.
	 *
	 * @return The DQL statement for getting data from the database.
	 */
	@Override
	public String getQuery() {
		var builder = new StringBuilder("select ");
		var fieldList = new LinkedList<String>();
		var foreignKeyList = new LinkedList<ForeignKeyReference>();
		var tableName = tableModule.getTableName(this.type);
		var fieldModule = new FieldModule();
		var columns = fieldModule.getAllFields(this.type);
		for (var column : columns) {
			if (column.isForeignKey()) {
				foreignKeyList.add(new ForeignKeyReference(
						column.getReference(),
						column.getColumn().getAnnotation(ForeignKeyEntity.class).value(),
						tableModule.getTableName(column.getColumn().getType()),
						column.getAlias()));
				continue;
			}

			fieldList.add(String.format("%s as %s", column.getSQLNotation(), column.getAliasNotation()));
		}

		builder.append(String.join(", ", fieldList)).append(" from `").append(tableName).append("`");
		for (var foreignKey : foreignKeyList) {
			builder.append(" left join `").append(foreignKey.getChildTable()).append("` ").append(foreignKey.getAlias()).append(" on `").append(foreignKey.getParentClass()).append("`.`").append(foreignKey.getParentForeignKey()).append("` = `").append(foreignKey.getAlias()).append("`.`id`");
		}

		builder.append(generateQueryClauses(tableName));

		return builder.toString();
	}

	/**
	 * Creates the query clauses for a DQL statement. This contains constraints like a WHERE, an ORDER BY and a LIMIT statement.
	 *
	 * @param tableName The table name which is targeted.
	 * @return A string containing the clauses which can then be appended to the end of a DQL statement.
	 */
	public String generateQueryClauses(String tableName) {
		var builder = new StringBuilder();

		var constraints = QueryConstraints.getConstraints(this.type);
		var clauseCopy = this.whereClause;
		if (clauseCopy == null) {
			clauseCopy = constraints;
		} else {
			clauseCopy = clauseCopy.and(constraints);
		}

		builder.append(" where ").append(Lambda2Sql.toSql(clauseCopy, tableName));
		if (this.orderByClause != null && this.orderByClause.length > 0) {
			builder.append(" order by ");

			if (this.orderByClause.length == 1) {
				builder.append(Lambda2Sql.toSql(this.orderByClause[0], tableName));
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

	/**
	 * Gets the table name which this query targets.
	 *
	 * @return The table name which this query targets.
	 */
	public String getTableName() {
		return tableModule.getTableName(this.type);
	}
}
