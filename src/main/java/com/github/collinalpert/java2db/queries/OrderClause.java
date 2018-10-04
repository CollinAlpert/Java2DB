package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

/**
 * Describes an SQL ORDER BY clause.
 *
 * @author Collin Alpert
 */
public class OrderClause<T extends BaseEntity> {

	private SqlFunction<T, ?> function;
	private OrderTypes orderType;

	public OrderClause(SqlFunction<T, ?> function, OrderTypes orderType) {
		this.function = function;
		this.orderType = orderType;
	}

	public OrderClause(SqlFunction<T, ?> function) {
		this.function = function;
		this.orderType = OrderTypes.ASCENDING;
	}

	public SqlFunction<T, ?> getFunction() {
		return function;
	}

	public void setFunction(SqlFunction<T, ?> function) {
		this.function = function;
	}

	public OrderTypes getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderTypes orderType) {
		this.orderType = orderType;
	}
}
