package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.functions.Lambda2Sql;
import com.github.collinalpert.java2db.functions.SqlFunction;
import com.github.collinalpert.java2db.functions.SqlPredicate;
import com.github.collinalpert.java2db.mappers.BaseMapper;
import com.github.collinalpert.java2db.services.BaseService;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A class representing a DQL statement with different options, including where clauses, order by clauses and limits.
 *
 * @author Collin Alpert
 */
public class Query<T extends BaseEntity> {

	private final BaseMapper<T> mapper;
	private final StringBuilder query;

	/**
	 * Constructor for creating a basic DQL statement for a given table name.
	 * This constructor should not be used directly, but through the
	 * {@link BaseService#selectQuery()} method which every service can use due to inheritance.
	 *
	 * @param tableName The table to query.
	 * @param mapper    The mapper for mapping entities.
	 */
	public Query(String tableName, BaseMapper<T> mapper) {
		this.mapper = mapper;
		this.query = new StringBuilder(String.format("select * from `%s`", tableName));
	}

	/**
	 * Constructor for creating a DQL statement which includes a sub select.
	 * This constructor should not be used directly, but through the {@link BaseService#subSelectQuery(Query)} method
	 * which every service class can use due to inheritance.
	 *
	 * @param subSelect The sub select object.
	 * @param mapper    The mapper for mapping entities.
	 */
	public Query(Query<T> subSelect, BaseMapper<T> mapper) {
		this.mapper = mapper;
		this.query = new StringBuilder(String.format("select * from (%s)", subSelect.getQuery()));
	}

	/**
	 * Gets the first row of a query.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	public Optional<T> getFirst() {
		try (var connection = new DBConnection()) {
			Utilities.log(query.toString());
			return mapper.map(connection.execute(query.toString()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Gets the values returned from the query.
	 *
	 * @return A list of entities representing the result rows.
	 */
	public List<T> get() {
		try (var connection = new DBConnection()) {
			Utilities.log(query.toString());
			return mapper.mapToList(connection.execute(query.toString()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Applies an alias to the result.
	 *
	 * @param alias The name of the alias.
	 * @return This {@link Query} object, now with the added alias.
	 */
	public Query<T> as(String alias) {
		query.append(" as `").append(alias).append("`");
		return this;
	}

	/**
	 * Adds a WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link Query} object, now with the added WHERE clause.
	 */
	public Query<T> where(SqlPredicate<T> predicate) {
		query.append(" where ").append(Lambda2Sql.toSql(predicate));
		return this;
	}

	/**
	 * Adds an ORDER BY clause to the DQL statement. The order will be ascending.
	 *
	 * @param function The value to order by.
	 * @return This {@link Query} object, now with the added ORDER BY clause.
	 */
	public Query<T> orderBy(SqlFunction<T, ?> function) {
		return orderBy(function, OrderTypes.ASCENDING);
	}

	/**
	 * Adds an ORDER BY clause to the DQL statement. The order will be ascending.
	 *
	 * @param function The value to order by.
	 * @param type     The type of ordering that should be applied.
	 * @return This {@link Query} object, now with the added ORDER BY clause.
	 */
	public Query<T> orderBy(SqlFunction<T, ?> function, OrderTypes type) {
		query.append(" order by `").append(Lambda2Sql.toSql(function)).append("` ").append(type.getSql());
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link Query} object, now with the added LIMIT.
	 */
	public Query<T> limit(int limit) {
		query.append(" limit ").append(limit);
		return this;
	}

	/**
	 * @return the query as a {@link String}
	 */
	public String getQuery() {
		return query.toString();
	}

	@Override
	public String toString() {
		return query.toString();
	}
}
