package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.ForeignKeyReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.Mapper;
import com.github.collinalpert.java2db.services.BaseService;
import com.github.collinalpert.java2db.utilities.Utilities;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A class representing a DQL statement with different options, including where clauses, order by clauses and limits.
 * It also automatically joins foreign keys so the corresponding entities (marked with the {@link ForeignKeyEntity} attribute) can be filled.
 *
 * @author Collin Alpert
 */
public class Query<T extends BaseEntity> {

	private final Class<T> type;
	private final Mapper<T> mapper;

	private SqlPredicate<T> whereClause;
	private SqlFunction<T, ?> orderBy;
	private OrderTypes orderType;
	private Integer limit;


	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the
	 * {@link BaseService#createQuery()} method which every service can use due to inheritance.
	 *
	 * @param type   The entity to query.
	 * @param mapper The mapper for mapping entities.
	 */
	public Query(Class<T> type, Mapper<T> mapper) {
		this.type = type;
		this.mapper = mapper;
	}

	/**
	 * Gets the first row of a query.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	public Optional<T> getFirst() {
		try (var connection = new DBConnection()) {
			return mapper.map(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Gets the values returned from the query as a {@code List}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	public List<T> toList() {
		try (var connection = new DBConnection()) {
			return mapper.mapToList(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Gets the values returned from the query as a {@code Stream}.
	 *
	 * @return A list of entities representing the result rows.
	 */
	public Stream<T> toStream() {
		try (var connection = new DBConnection()) {
			return mapper.mapToStream(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Stream.empty();
		}
	}

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link Query} object, now with an (appended) WHERE clause.
	 */
	public Query<T> where(SqlPredicate<T> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.and(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link Query} object, now with an (appended) OR WHERE clause.
	 */
	public Query<T> orWhere(SqlPredicate<T> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.or(predicate);
		return this;
	}

	/**
	 * Sets an ORDER BY clause for the DQL statement. The order will be ascending.
	 *
	 * @param function The property to order by.
	 * @return This {@link Query} object, now with an ORDER BY clause.
	 */
	public Query<T> orderBy(SqlFunction<T, ?> function) {
		return orderBy(function, OrderTypes.ASCENDING);
	}

	/**
	 * Sets an ORDER BY clause for the DQL statement.
	 *
	 * @param function The property to order by.
	 * @param type     The type of ordering that should be applied.
	 * @return This {@link Query} object, now with an ORDER BY clause.
	 */
	public Query<T> orderBy(SqlFunction<T, ?> function, OrderTypes type) {
		this.orderBy = function;
		this.orderType = type;
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link Query} object, now with a LIMIT.
	 */
	public Query<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * Builds the query from the set query options.
	 *
	 * @return The DQL statement for getting data from the database.
	 */
	private String buildQuery() {
		var builder = new StringBuilder("select ");
		var fieldList = new LinkedList<String>();
		var foreignKeyList = new LinkedList<ForeignKeyReference>();
		var tableName = String.format("`%s`", Utilities.getTableName(this.type));
		var columns = Utilities.getAllFields(this.type);
		for (var column : columns) {
			if (column.isForeignKey()) {
				foreignKeyList.add(new ForeignKeyReference(
						column.getReference(),
						column.getColumn().getAnnotation(ForeignKeyEntity.class).value(),
						Utilities.getTableName(column.getColumn().getType()),
						column.getAlias()));
				continue;
			}
			fieldList.add(String.format("%s as %s", column.getSQLNotation(), column.getAliasNotation()));
		}
		builder.append(String.join(", ", fieldList)).append(" from ").append(tableName);
		for (var foreignKey : foreignKeyList) {
			builder.append(" left join `").append(foreignKey.getChildTable()).append("` ").append(foreignKey.getAlias()).append(" on `").append(foreignKey.getParentClass()).append("`.").append(foreignKey.getParentForeignKey()).append(" = ").append(foreignKey.getAlias()).append(".id");
		}
		var constraints = QueryConstraints.getConstraints(this.type);
		var clauseCopy = this.whereClause;
		if (clauseCopy == null) {
			clauseCopy = constraints;
		} else {
			clauseCopy = clauseCopy.and(constraints);
		}

		builder.append(" where ").append(Lambda2Sql.toSql(clauseCopy, tableName));
		if (orderBy != null) {
			builder.append(" order by ")
					.append(Lambda2Sql.toSql(this.orderBy, tableName))
					.append(" ")
					.append(this.orderType.getSql());
		}
		if (this.limit != null) {
			builder.append(" limit ").append(this.limit);
		}
		return builder.toString();
	}

	/**
	 * @return the query as a {@code String}
	 */
	public String getQuery() {
		return buildQuery();
	}

	@Override
	public String toString() {
		return buildQuery();
	}
}
