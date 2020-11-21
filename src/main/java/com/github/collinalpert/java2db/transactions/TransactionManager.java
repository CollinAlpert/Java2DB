package com.github.collinalpert.java2db.transactions;

import com.github.collinalpert.java2db.database.*;
import com.github.collinalpert.java2db.exceptions.TransactionRolledBackException;
import com.github.collinalpert.java2db.utilities.ThrowableConsumer;

import java.sql.Connection;

/**
 * @author Collin Alpert
 */
public class TransactionManager {

	public static void runTransaction(ThrowableConsumer<Transaction, Throwable> consumer) {
		runTransaction(new DBConnection(), consumer);
	}

	public static void runTransaction(Connection connection, ThrowableConsumer<Transaction, Throwable> consumer) {
		runTransaction(new DBConnection(connection), consumer);
	}

	public static void runTransaction(DBConnection connection, ThrowableConsumer<Transaction, Throwable> consumer) {
		var callingMethodOptional = StackWalker.getInstance().walk(s -> s.map(x -> String.format("%s$%s", x.getClassName(), x.getMethodName())).filter(x -> !x.equals("com.github.collinalpert.java2db.transactions.TransactionManager$runTransaction")).findFirst());
		if (callingMethodOptional.isEmpty()) {
			throw new RuntimeException("Transaction could not be created.");
		}

		var transactionId = callingMethodOptional.get();
		var transaction = new Transaction(connection);
		ConnectionPool.enlistTransaction(transactionId, transaction);
		try {
			consumer.consume(transaction);

			if (!transaction.isCommitted()) {
				transaction.commit();
			}
		} catch (Throwable e) {
			transaction.rollback();
			throw new TransactionRolledBackException("Exception was thrown. Transaction rolling back.", e);
		} finally {
			transaction.dispose();
			ConnectionPool.removeTransaction(transactionId);
		}
	}
}
