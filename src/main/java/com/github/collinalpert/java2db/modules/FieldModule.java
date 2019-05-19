package com.github.collinalpert.java2db.modules;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.annotations.Ignore;
import com.github.collinalpert.java2db.database.TableColumnReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.utilities.UniqueIdentifier;

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

	private static final TableModule tableModule;

	static {
		tableModule = new TableModule();
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
	 * @param <T>                The type of the entity.
	 * @return A list with all fields of this class and its parents, up to the delimiter class.
	 */
	private <T extends BaseEntity> List<Field> getEntityFields(Class<? super T> instanceClass, Class<?> delimiter, boolean includeForeignKeys) {
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
		return getAllFields(instanceClass, "");
	}

	/**
	 * Gets all the fields and the fields of foreign key objects in this entity.
	 *
	 * @param instanceClass The class to get the fields from.
	 * @param alias         The alias that nested properties will use.
	 * @return A list of columns including references to their table.
	 */
	public List<TableColumnReference> getAllFields(Class<? extends BaseEntity> instanceClass, String alias) {
		var fields = new LinkedList<TableColumnReference>();
		for (var field : getEntityFields(instanceClass, true)) {
			if (field.getType().isEnum()) {
				continue;
			}

			if (field.getAnnotation(ForeignKeyEntity.class) != null) {
				var tempAlias = UniqueIdentifier.generate(tableModule.getTableName(field.getType()).substring(0, 1), field.getName());
				fields.add(new TableColumnReference(tableModule.getTableName(instanceClass), field, tempAlias, alias));
				fields.addAll(getAllFields((Class<? extends BaseEntity>) field.getType(), tempAlias));
			} else {
				fields.add(new TableColumnReference(tableModule.getTableName(instanceClass), field, alias, ""));
			}
		}

		return fields;
	}
}
