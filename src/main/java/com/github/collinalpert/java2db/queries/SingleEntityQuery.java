package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.*;
import com.github.collinalpert.java2db.modules.*;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.*;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Collin Alpert
 */
public class SingleEntityQuery<E extends BaseEntity> implements SingleQueryable<E> {

	protected static final TableModule tableModule;

	static {
		tableModule = TableModule.getInstance();
	}

	protected final Class<E> type;
	protected final Mappable<E> mapper;
	private SqlPredicate<E> whereClause;

	public SingleEntityQuery(Class<E> type) {
		this.type = type;
		this.mapper = IoC.resolveMapper(type, new EntityMapper<>(type));
	}

	//region Configuration

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) WHERE clause.
	 */
	public SingleEntityQuery<E> where(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.and(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link EntityQuery} object, now with an (appended) OR WHERE clause.
	 */
	public SingleEntityQuery<E> orWhere(SqlPredicate<E> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.or(predicate);
		return this;
	}

	/**
	 * Selects only a single column from a table. This is meant if you don't want to fetch an entire entity from the database.
	 *
	 * @param projection The column to project to.
	 * @param <R>        The type of the column you want to retrieve.
	 * @return A queryable containing the projection.
	 */
	public <R> SingleQueryable<R> project(SqlFunction<E, R> projection) {
		return new SingleEntityProjectionQuery<>(projection, this);
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
		try (var connection = new DBConnection()) {
			return this.mapper.map(connection.execute(getQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
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
		var tableName = tableModule.getTableName(this.type);
		var columns = FieldModule.getInstance().getColumnReferences(this.type);

		var columnIterator = columns.iterator();
		while (columnIterator.hasNext()) {
			var column = columnIterator.next();
			if (column instanceof ForeignKeyReference) {
				continue;
			}

			fieldList.add(String.format("%s as %s", column.getSQLNotation(), column.getAliasNotation()));
			// Remove TableColumnReferences so we end up only with the foreign keys.
			columnIterator.remove();
		}

		builder.append(String.join(", ", fieldList)).append(" from `").append(tableName).append("`");

		for (var column : columns) {
			var foreignKey = (ForeignKeyReference) column;
			builder.append(" ").append(foreignKey.getJoinType().getSqlKeyword()).append(" join `").append(foreignKey.getForeignKeyTableName()).append("` ").append(foreignKey.getForeignKeyAlias()).append(" on `").append(foreignKey.getAlias()).append("`.`").append(foreignKey.getForeignKeyColumnName()).append("` = `").append(foreignKey.getForeignKeyAlias()).append("`.`id`");
		}

		builder.append(getQueryClauses(tableName));

		return builder.toString();
	}

	String getQueryClauses(String tableName) {
		var builder = new StringBuilder();

		buildWhereClause(builder, tableName);

		// Since we only want to fetch one result anyway.
		builder.append(" limit 1");

		return builder.toString();
	}

	/**
	 * Builds the WHERE clause in a select statement.
	 *
	 * @param builder   The {@code StringBuilder} to append the clause to.
	 * @param tableName The name of the table the where clause will affect.
	 */
	protected void buildWhereClause(StringBuilder builder, String tableName) {
		var constraints = QueryConstraints.getConstraints(this.type);
		var clauseCopy = this.whereClause;
		if (clauseCopy == null) {
			clauseCopy = constraints;
		} else {
			clauseCopy = clauseCopy.and(constraints);
		}

		builder.append(" where ").append(Lambda2Sql.toSql(clauseCopy, tableName));
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
