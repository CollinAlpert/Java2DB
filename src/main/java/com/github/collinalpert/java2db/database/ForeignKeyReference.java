package com.github.collinalpert.java2db.database;

/**
 * Describes a foreign key reference to a specific table.
 *
 * @author Collin Alpert
 */
public class ForeignKeyReference {

	/**
	 * The table the foreign key column is in.
	 */
	private final String parentClass;

	/**
	 * The foreign key column.
	 */
	private final String parentForeignKey;

	/**
	 * The table the foreign key refers to.
	 */
	private final String childTable;

	/**
	 * An alias for the joining table.
	 */
	private final String alias;

	public ForeignKeyReference(String parentClass, String parentForeignKey, String childTable, String alias) {
		this.parentClass = parentClass;
		this.parentForeignKey = parentForeignKey;
		this.childTable = childTable;
		this.alias = alias;
	}

	public String getParentClass() {
		return parentClass;
	}

	public String getParentForeignKey() {
		return parentForeignKey;
	}

	public String getChildTable() {
		return childTable;
	}

	public String getAlias() {
		return alias;
	}
}
