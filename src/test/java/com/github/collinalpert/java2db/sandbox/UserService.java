package com.github.collinalpert.java2db.sandbox;

import com.github.collinalpert.java2db.database.TransactionManager;
import com.github.collinalpert.java2db.services.BaseService;

public class UserService extends BaseService<User> {

	protected UserService(TransactionManager transactionManager) {
		super(transactionManager);
	}
}
