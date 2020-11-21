package com.github.collinalpert.java2db.modules;

import com.github.collinalpert.java2db.annotations.*;

import java.lang.reflect.Field;

/**
 * A helper module for getting information about database tables and their columns.
 *
 * @author Collin Alpert
 */
public class TableModule {

	private static final TableModule instance;

	private static final AnnotationModule annotationModule;

	static {
		instance = new TableModule();

		annotationModule = AnnotationModule.getInstance();
	}

	private TableModule() {
	}

	public static TableModule getInstance() {
		return instance;
	}

	/**
	 * Gets the database table name from the {@link TableName} attribute on the class.
	 * If there is no attribute, the class name in lower case characters is returned.
	 *
	 * @param type The entity to get the table name of.
	 * @return The table name.
	 */
	public String getTableName(Class<?> type) {
		var tableNameAnnotation = type.getAnnotation(TableName.class);
		if (tableNameAnnotation == null) {
			return type.getSimpleName().toLowerCase();
		}

		return tableNameAnnotation.value();
	}

	/**
	 * Gets the corresponding table column name of a field.
	 *
	 * @param field The field representing the column in a table.
	 * @return The column name in the table.
	 */
	public String getColumnName(Field field) {
		var annotationInfo = annotationModule.getAnnotationInfo(field, ColumnName.class);
		if (!annotationInfo.hasAnnotation()) {
			return field.getName();
		}

		return annotationInfo.getAnnotation().value();
	}
}
