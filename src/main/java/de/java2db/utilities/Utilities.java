package de.java2db.utilities;

import de.java2db.database.ForeignKeyObject;
import de.java2db.entities.BaseEntity;

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

	public static void log(Object text) {
		if (SystemParameter.LOG_QUERIES) {
			System.out.println(text);
		}
	}

	public static void logf(String x, Object... params) {
		log(String.format(x, params));
	}
}
