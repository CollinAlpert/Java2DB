package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.database.ForeignKeyObject;
import com.github.collinalpert.java2db.entities.BaseEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Collin Alpert
 */
public class Utilities {

	public static <T extends BaseEntity> ArrayList<Field> getAllFields(T instance) {
		return getAllFields(instance, null, false);
	}

	public static <T extends BaseEntity> ArrayList<Field> getAllFields(T instance, boolean includeForeignKeys) {
		return getAllFields(instance, null, includeForeignKeys);
	}

	public static <T extends BaseEntity> ArrayList<Field> getAllFields(T instance, Class<?> delimiter) {
		return getAllFields(instance, delimiter, false);
	}

	/**
	 * Gets all fields of an entity including all base classes.
	 *
	 * @param instance           The instance to get the fields of.
	 * @param delimiter          Up to which parent class to go to.
	 * @param includeForeignKeys Decides if to include foreign key objects in this list, since they do not exist on the database.
	 * @param <T>                The type of the entity.
	 * @return A list with all fields of this class and its parents, up to the delimiter class.
	 */
	public static <T extends BaseEntity> ArrayList<Field> getAllFields(T instance, Class<?> delimiter, boolean includeForeignKeys) {
		Class<?> current = instance.getClass();
		ArrayList<Field> fields = new ArrayList<>();
		do {
			fields.addAll(Arrays.asList(current.getDeclaredFields()));
			if (!includeForeignKeys) {
				fields.removeIf(x -> x.getAnnotation(ForeignKeyObject.class) != null);
			}
			current = current.getSuperclass();
		} while (current != delimiter);
		return fields;
	}

	/**
	 * Prints messages to the query, while considering the {@code LOG_QUERIES} constant.
	 *
	 * @param text The message to print.
	 */
	public static void log(Object text) {
		if (SystemParameter.LOG_QUERIES) {
			System.out.println(text);
		}
	}

	/**
	 * Prints formatted messages to the query, while considering the {@code LOG_QUERIES} constant.
	 *
	 * @param text   The formatted text.
	 * @param params The parameters to be inserted into the string.
	 */
	public static void logf(String text, Object... params) {
		log(String.format(text, params));
	}
}
