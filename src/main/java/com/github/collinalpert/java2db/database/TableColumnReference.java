package com.github.collinalpert.java2db.database;

import com.github.collinalpert.java2db.modules.TableModule;

import java.lang.reflect.Field;

/**
 * Describes a column and its table name so they can be referenced together.
 *
 * @author Collin Alpert
 */
public class TableColumnReference {

	private static final TableModule tableModule;

	static {
		tableModule = TableModule.getInstance();
	}

	/**
	 * The table name of this reference.
	 */
	private final String tableName;

	/**
	 * The column name of this reference.
	 */
	private final Field column;

	/**
	 * The alias for this reference, if one exists.
	 * If it doesn't, the table name will be used.
	 */
	private final String alias;

	public TableColumnReference(String tableName, String alias, Field column) {
		this.tableName = tableName;
		this.alias = alias.isBlank() ? tableName : alias;
		this.column = column;
	}

	public String getTableName() {
		return tableName;
	}

	public String getAlias() {
		return alias;
	}

	public Field getColumn() {
		return column;
	}

	public String getSQLNotation() {
		return String.format("`%s`.`%s`", getAlias(), tableModule.getColumnName(column));
	}

	public String getAliasNotation() {
		return getAlias() + "_" + tableModule.getColumnName(column);
	}

	@Override
	public String toString() {
		return getSQLNotation();
	}
}
