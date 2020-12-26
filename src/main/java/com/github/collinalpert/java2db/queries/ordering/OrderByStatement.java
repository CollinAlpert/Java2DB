package com.github.collinalpert.java2db.queries.ordering;

import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

import java.util.List;

/**
 * @author Collin Alpert
 */
public class OrderByStatement<E extends BaseEntity> {

	private final List<SqlFunction<E, ?>> columns;
	private final OrderTypes orderType;

	public OrderByStatement(List<SqlFunction<E, ?>> columns, OrderTypes orderType) {
		this.columns = columns;
		this.orderType = orderType;
	}

	public List<SqlFunction<E, ?>> getColumns() {
		return columns;
	}

	public OrderTypes getOrderType() {
		return orderType;
	}
}
