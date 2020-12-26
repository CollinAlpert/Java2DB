package com.github.collinalpert.java2db.queries.builder;

import com.github.collinalpert.java2db.database.ForeignKeyReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.*;
import com.github.collinalpert.java2db.queries.*;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.util.LinkedList;

/**
 * @author Collin Alpert
 */
public abstract class QueryBuilder<E extends BaseEntity> implements IQueryBuilder<E> {

	static final TableModule tableModule = TableModule.getInstance();
	private static final FieldModule fieldModule = FieldModule.getInstance();
	protected final String tableName;
	private final Class<E> type;

	public QueryBuilder(Class<E> type) {
		this.type = type;
		this.tableName = tableModule.getTableName(this.type);
	}

	/**
	 * Builds the WHERE clause in a select statement.
	 *
	 * @param buffer      The {@code StringBuffer} to append the clause to.
	 * @param whereClause The predicate to transform into the WHERE clause.
	 */
	protected void appendWhereClause(StringBuffer buffer, SqlPredicate<E> whereClause) {
		var constraints = QueryConstraints.getConstraints(this.type);
		if (whereClause == null) {
			whereClause = constraints;
		} else {
			whereClause = whereClause.and(constraints);
		}

		buffer.append("where ").append(Lambda2Sql.toSql(whereClause, this.tableName));
	}

	@Override
	public String build(QueryParameters<E> queryParameters) {
		var builder = new StringBuilder("select ");
		var fieldList = new LinkedList<String>();
		var columns = fieldModule.getColumnReferences(this.type);

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

		if (queryParameters.getDistinct()) {
			builder.append("distinct ");
		}

		builder.append(String.join(", ", fieldList)).append(" from `").append(this.tableName).append("`");

		for (var column : columns) {
			var foreignKey = (ForeignKeyReference) column;
			builder.append(' ').append(foreignKey.getJoinType().getSqlKeyword()).append(" join `").append(foreignKey.getForeignKeyTableName()).append("` ").append(foreignKey.getForeignKeyAlias()).append(" on `").append(foreignKey.getAlias()).append("`.`").append(foreignKey.getForeignKeyColumnName()).append("` = `").append(foreignKey.getForeignKeyAlias()).append("`.`id`");
		}

		builder.append(buildQueryClauses(queryParameters));

		return builder.toString();
	}


	abstract String buildQueryClauses(QueryParameters<E> queryParameters);
}
