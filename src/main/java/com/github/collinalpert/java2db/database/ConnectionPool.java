package com.github.collinalpert.java2db.database;

import com.github.collinalpert.java2db.transactions.Transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Collin Alpert
 */
public class ConnectionPool {

	private static final Map<String, Transaction> transactions;
	private static final DBConnection connection;

	static {
		connection = new DBConnection();
		transactions = new ConcurrentHashMap<>();
	}

	public static DBConnection getConnection() {
		var transactionOptional = StackWalker.getInstance().walk(s -> s.limit(10).map(x -> transactions.get(String.format("%s$%s", x.getClassName(), x.getMethodName()))).filter(Objects::nonNull).findFirst());
		if (transactionOptional.isEmpty()) {
			return connection;
		}

		return transactionOptional.get().getConnection();
	}

	public static void enlistTransaction(String transactionId, Transaction transaction) {
		transactions.put(transactionId, transaction);
	}

	public static void removeTransaction(String transactionId) {
		transactions.remove(transactionId);
	}
}
