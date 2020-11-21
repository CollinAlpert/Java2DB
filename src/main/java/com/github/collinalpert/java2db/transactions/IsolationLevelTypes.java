package com.github.collinalpert.java2db.transactions;

/**
 * @author Collin Alpert
 */
public enum IsolationLevelTypes {
	/**
	 * @see java.sql.Connection#TRANSACTION_NONE
	 */
	TRANSACTION_NONE(0),

	/**
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	TRANSACTION_READ_UNCOMMITTED(1),

	/**
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	TRANSACTION_READ_COMMITTED(2),

	/**
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	TRANSACTION_REPEATABLE_READ(4),

	/**
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	TRANSACTION_SERIALIZABLE(8);

	private final int value;

	IsolationLevelTypes(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
