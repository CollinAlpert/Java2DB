package com.github.collinalpert.java2db.transactions;

import com.github.collinalpert.java2db.database.DBConnection;

import java.sql.*;

/**
 * @author Collin Alpert
 */
public class Transaction {

	private final DBConnection connection;
	private final IsolationLevelTypes isolationLevel;
	private boolean isCommitted;
	private Savepoint lastSavepoint;

	public Transaction() {
		this(new DBConnection(), IsolationLevelTypes.TRANSACTION_READ_COMMITTED);
	}

	public Transaction(DBConnection connection) {
		this(connection, IsolationLevelTypes.TRANSACTION_READ_COMMITTED);
	}

	public Transaction(IsolationLevelTypes isolationLevel) {
		this(new DBConnection(), isolationLevel);
	}

	public Transaction(DBConnection connection, IsolationLevelTypes isolationLevel) {
		this.connection = connection;
		this.isolationLevel = isolationLevel;

		prepareConnection();
	}

	private void prepareConnection() {
		try {
			this.connection.underlyingConnection().setAutoCommit(false);
			this.connection.underlyingConnection().setTransactionIsolation(this.isolationLevel.getValue());
		} catch (SQLException e) {
			throw new IllegalStateException("Could not create transaction.", e);
		}
	}

	protected void commit() {
		this.isCommitted = true;

		try {
			this.connection.underlyingConnection().commit();
		} catch (SQLException e) {
			this.rollback();

			throw new RuntimeException("Transaction could not be committed. Rolling back.", e);
		}
	}

	public void rollback() {
		try {
			this.connection.underlyingConnection().rollback();
		} catch (SQLException e) {
			throw new RuntimeException("Transaction could not be rolled back. I don't even know what to do at this point.", e);
		}
	}

	public void rollback(Savepoint savepoint) {
		if (savepoint == null) {
			rollback();

			return;
		}

		try {
			this.connection.underlyingConnection().rollback(savepoint);
		} catch (SQLException e) {
			throw new RuntimeException("Transaction could not be rolled back. I don't even know what to do at this point.", e);
		}
	}

	public void rollbackToLastSavePoint() {
		if (this.lastSavepoint == null) {
			throw new IllegalStateException("No savepoint has been set so far.");
		}

		try {
			this.connection.underlyingConnection().rollback(this.lastSavepoint);
		} catch (SQLException e) {
			throw new RuntimeException("Transaction could not be rolled back. I don't even know what to do at this point.", e);
		}
	}

	public void dispose() {
		this.connection.close();
	}

	public Savepoint setSavepoint() {
		try {
			return this.lastSavepoint = this.connection.underlyingConnection().setSavepoint();
		} catch (SQLException e) {
			throw new RuntimeException("Could not set savepoint.", e);
		}
	}

	public DBConnection getConnection() {
		return connection;
	}

	public boolean isCommitted() {
		return isCommitted;
	}
}
