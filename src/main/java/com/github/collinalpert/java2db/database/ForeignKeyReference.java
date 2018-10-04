package com.github.collinalpert.java2db.database;

/**
 * Describes a foreign key reference containing the table and column of the
 *
 * @author Collin Alpert
 */
public class ForeignKeyReference {

	// The table the foreign key column is in.
	private final String parentTable;

	// The foreign key column.
	private final String parentForeignKey;

	// The table the foreign key refers to.
	private final String childTable;

	public ForeignKeyReference(String parentTable, String parentForeignKey, String childTable) {
		this.parentTable = parentTable;
		this.parentForeignKey = parentForeignKey;
		this.childTable = childTable;
	}

	public String getParentTable() {
		return parentTable;
	}

	public String getParentForeignKey() {
		return parentForeignKey;
	}

	public String getChildTable() {
		return childTable;
	}
}
