package com.github.collinalpert.java2db.queries.ordering;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

import java.util.*;

/**
 * @author Collin Alpert
 */
public class OrderByClause<E extends BaseEntity> {

	private final List<OrderByStatement<E>> orderByStatements;

	public OrderByClause(SqlFunction<E, ?> column, OrderTypes orderType) {
		this.orderByStatements = new LinkedList<>(Collections.singletonList(new OrderByStatement<>(Collections.singletonList(column), orderType)));
	}

	public OrderByClause(List<SqlFunction<E, ?>> columns, OrderTypes orderType) {
		this.orderByStatements = new LinkedList<>(Collections.singletonList(new OrderByStatement<>(columns, orderType)));
	}

	public void addStatement(SqlFunction<E, ?> column, OrderTypes orderType) {
		this.orderByStatements.add(new OrderByStatement<>(Collections.singletonList(column), orderType));
	}

	public void addStatement(List<SqlFunction<E, ?>> columns, OrderTypes orderType) {
		this.orderByStatements.add(new OrderByStatement<>(columns, orderType));
	}

	public String buildSql(String tableName) {
		var orderByJoiner = new StringJoiner(", ", "order by ", "");
		var builder = new StringBuilder();
		for (var statement : this.orderByStatements) {
			if (statement.getColumns().size() == 1) {
				builder.append(Lambda2Sql.toSql(statement.getColumns().get(0), tableName));
			} else {
				var coalesceJoiner = new StringJoiner(", ", "coalesce(", ")");
				for (SqlFunction<E, ?> orderByFunction : statement.getColumns()) {
					coalesceJoiner.add(Lambda2Sql.toSql(orderByFunction, tableName));
				}

				builder.append(coalesceJoiner.toString());
			}

			builder.append(' ').append(statement.getOrderType().getSqlKeyword());
			orderByJoiner.add(builder);
			builder.setLength(0);
		}

		return orderByJoiner.toString();
	}
}
