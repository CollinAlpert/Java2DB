package com.github.collinalpert.java2db.database;

import com.github.collinalpert.java2db.annotations.ForeignKeyObject;

import java.lang.reflect.Field;

/**
 * Describes a column and its table name so they can be referenced together.
 *
 * @author Collin Alpert
 */
public class TableNameColumn {

	private final String tableName;
	private final Field column;

	public TableNameColumn(String tableName, Field column) {
		this.tableName = tableName;
		this.column = column;
	}

	public String getTableName() {
		return tableName;
	}

	public Field getColumn() {
		return column;
	}

	public String getSQLNotation() {
		return tableName + "." + column.getName();
	}

	public String getAliasNotation() {
		return tableName + "_" + column.getName();
	}

	public boolean isForeignKey() {
		return column.getAnnotation(ForeignKeyObject.class) != null;
	}

	@Override
	public String toString() {
		return getSQLNotation();
	}
}
