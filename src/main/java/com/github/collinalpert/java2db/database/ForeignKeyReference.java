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
	private final String foreignKeyAlias;

	/**
	 * The type of join to use.
	 */
	private final ForeignKeyEntity.JoinTypes joinType;


	public ForeignKeyReference(String tableName, String alias, Field column, String foreignKeyTableName, String foreignKeyAlias, ForeignKeyEntity.JoinTypes joinType) {
		super(tableName, alias, column);
		this.foreignKeyTableName = foreignKeyTableName;
		this.foreignKeyColumnName = column.getAnnotation(ForeignKeyEntity.class).value();
		this.foreignKeyAlias = foreignKeyAlias;
		this.joinType = joinType;
	}

	public ForeignKeyReference(String tableName, String alias, Field column, String foreignKeyTableName, String foreignKeyAlias) {
		this(tableName, alias, column, foreignKeyTableName, foreignKeyAlias, ForeignKeyEntity.JoinTypes.LEFT);
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

	public ForeignKeyEntity.JoinTypes getJoinType() {
		return joinType;
	}
}
