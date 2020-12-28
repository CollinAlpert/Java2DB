package com.github.collinalpert.java2db.sandbox;

import com.github.collinalpert.java2db.database.TransactionManager;
import com.github.collinalpert.java2db.services.BaseService;

public class OrderService extends BaseService<Order> {
	protected OrderService(TransactionManager transactionManager) {
		super(transactionManager);
	}
}
