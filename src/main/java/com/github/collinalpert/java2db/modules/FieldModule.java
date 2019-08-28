package com.github.collinalpert.java2db.modules;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.annotations.Ignore;
import com.github.collinalpert.java2db.database.ForeignKeyReference;
import com.github.collinalpert.java2db.database.TableColumnReference;
import com.github.collinalpert.java2db.entities.BaseEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper module for getting fields from classes.
 *
 * @author Collin Alpert
 */
public class FieldModule {

	private static final FieldModule instance;

	private static final TableModule tableModule;
	private static final AnnotationModule annotationModule;

	static {
		instance = new FieldModule();

		tableModule = TableModule.getInstance();
		annotationModule = AnnotationModule.getInstance();
	}

	private FieldModule() {
	}

	public static FieldModule getInstance() {
		return instance;
	}

	public List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass) {
		return getEntityFields(instanceClass, null, false);
	}

	public List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass, boolean includeForeignKeys) {
		return getEntityFields(instanceClass, null, includeForeignKeys);
	}

	public List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass, Class<?> delimiter) {
		return getEntityFields(instanceClass, delimiter, false);
	}

	/**
	 * Gets all fields of an entity including all base classes.
	 *
	 * @param instanceClass      The class to get the fields of.
	 * @param delimiter          Up to which parent class to go to.
	 * @param includeForeignKeys Decides if to include foreign key objects in this list, since they do not exist on the database.
	 * @param <E>                The type of the entity.
	 * @return A list with all fields of this class and its parents, up to the delimiter class.
	 */
	private <E extends BaseEntity> List<Field> getEntityFields(Class<? super E> instanceClass, Class<?> delimiter, boolean includeForeignKeys) {
		var fields = new LinkedList<Field>();
		do {
			fields.addAll(Arrays.stream(instanceClass.getDeclaredFields())
					.filter(field -> field.getAnnotation(Ignore.class) == null && (includeForeignKeys || field.getAnnotation(ForeignKeyEntity.class) == null))
					.collect(Collectors.toList()));
			instanceClass = instanceClass.getSuperclass();
		} while (instanceClass != delimiter);

		return fields;
	}

	/**
	 * Gets all the fields and the fields of foreign key objects in this entity.
	 *
	 * @param instanceClass The class to get the fields from.
	 * @return A list of columns including references to their table.
	 */
	public List<TableColumnReference> getAllFields(Class<? extends BaseEntity> instanceClass) {
		return getAllFields(instanceClass, "", 0);
	}

	/**
	 * Gets all the fields and the fields of foreign key objects in this entity.
	 *
	 * @param instanceClass  The class to get the fields from.
	 * @param alias          The alias that nested properties will use.
	 * @return A list of columns including references to their table.
	 */
	private List<TableColumnReference> getAllFields(Class<? extends BaseEntity> instanceClass, String alias, int aliasCounter) {
		var fields = new LinkedList<TableColumnReference>();
		for (var field : getEntityFields(instanceClass, true)) {
			if (field.getType().isEnum()) {
				continue;
			}

			if (annotationModule.hasAnnotation(field, ForeignKeyEntity.class)) {
				var tempAlias = tableModule.getTableName(field.getType()).substring(0, 1) + ++aliasCounter;
				fields.add(new ForeignKeyReference(tableModule.getTableName(instanceClass), alias, field, tableModule.getTableName(field.getType()), tempAlias));
				fields.addAll(getAllFields((Class<? extends BaseEntity>) field.getType(), tempAlias, aliasCounter++));
			} else {
				fields.add(new TableColumnReference(tableModule.getTableName(instanceClass), alias, field));
			}
		}

		return fields;
	}
}