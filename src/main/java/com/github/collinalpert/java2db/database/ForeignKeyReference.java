package com.github.collinalpert.java2db.database;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;

import java.lang.reflect.Field;

/**
 * Describes a foreign key reference to a specific table. This represents a field marked with the {@link ForeignKeyEntity} attribute.
 *
 * @author Collin Alpert
 */
public class ForeignKeyReference extends TableColumnReference {

	/**
	 * The table the foreign key refers to.
	 */
	private final String foreignKeyTableName;
	/**
	 * The name of the column which references the foreign table. Not to be confused with the column in the foreign table.
	 */
	private final String foreignKeyColumnName;
	/**
	 * An alias for the foreign key table name.
	 */
	private String foreignKeyAlias;


	public ForeignKeyReference(String tableName, String alias, Field column, String foreignKeyTableName, String foreignKeyAlias) {
		super(tableName, alias, column);
		this.foreignKeyTableName = foreignKeyTableName;
		this.foreignKeyColumnName = column.getAnnotation(ForeignKeyEntity.class).value();
		this.foreignKeyAlias = foreignKeyAlias;
	}

	public String getForeignKeyTableName() {
		return foreignKeyTableName;
	}

	public String getForeignKeyColumnName() {
		return foreignKeyColumnName;
	}

	public String getForeignKeyAlias() {
		return foreignKeyAlias;
	}
}
