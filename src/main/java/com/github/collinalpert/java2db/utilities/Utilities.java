package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.annotations.ColumnName;
import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.annotations.Ignore;
import com.github.collinalpert.java2db.annotations.TableName;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.TableNameColumnReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.exceptions.AsynchronousOperationException;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Collin Alpert
 */
public class Utilities {

	public static List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass) {
		return getEntityFields(instanceClass, null, false);
	}

	public static List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass, boolean includeForeignKeys) {
		return getEntityFields(instanceClass, null, includeForeignKeys);
	}

	public static List<Field> getEntityFields(Class<? extends BaseEntity> instanceClass, Class<?> delimiter) {
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
	private static <T extends BaseEntity> List<Field> getEntityFields(Class<? super T> instanceClass, Class<?> delimiter, boolean includeForeignKeys) {
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
	public static List<TableNameColumnReference> getAllFields(Class<? extends BaseEntity> instanceClass) {
		return getAllFields(instanceClass, "");
	}

	/**
	 * Gets all the fields and the fields of foreign key objects in this entity.
	 *
	 * @param instanceClass The class to get the fields from.
	 * @param alias         The alias that nested properties will use.
	 * @return A list of columns including references to their table.
	 */
	public static List<TableNameColumnReference> getAllFields(Class<? extends BaseEntity> instanceClass, String alias) {
		var fields = new LinkedList<TableNameColumnReference>();
		for (var field : getEntityFields(instanceClass, true)) {
			if (field.getAnnotation(ForeignKeyEntity.class) != null) {
				var tempAlias = UniqueIdentifier.generate(getTableName(field.getType()).substring(0, 1), field.getName());
				fields.add(new TableNameColumnReference(getTableName(instanceClass), field, tempAlias, alias));
				fields.addAll(getAllFields((Class<? extends BaseEntity>) field.getType(), tempAlias));
			} else {
				fields.add(new TableNameColumnReference(getTableName(instanceClass), field, alias, ""));
			}
		}

		return fields;
	}

	/**
	 * Prints messages to the query, while considering the {@link DBConnection#LOG_QUERIES} constant.
	 *
	 * @param text The message to print.
	 */
	public static void log(Object text) {
		if (DBConnection.LOG_QUERIES) {
			System.out.println(text);
		}
	}

	/**
	 * Prints formatted messages to the query, while considering the {@link DBConnection#LOG_QUERIES} constant.
	 *
	 * @param text   The formatted text.
	 * @param params The parameters to be inserted into the string.
	 */
	public static void logf(String text, Object... params) {
		log(String.format(text, params));
	}

	/**
	 * Gets the database table name from the {@link TableName} attribute on the class.
	 * If there is no attribute, the class name in lower case characters is returned.
	 *
	 * @param type The entity to get the table name of.
	 * @return The table name.
	 */
	public static String getTableName(Class<?> type) {
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
	public static String getColumnName(Field field) {
		ColumnName columnName;
		if ((columnName = field.getAnnotation(ColumnName.class)) != null) {
			return columnName.value();
		}

		return field.getName();
	}

	/**
	 * Handles an {@code SQLException} that gets thrown inside a {@code Supplier}.
	 *
	 * @param supplier          The {@code Supplier} that throws the exception.
	 * @param exceptionHandling The exception handling supplied for this exception.
	 * @param <V>               The return type of the operation.
	 * @return The original {@code Supplier} but now with the added exception handling.
	 */
	public static <V> Supplier<V> supplierHandling(ThrowableSupplier<V, SQLException> supplier, Consumer<SQLException> exceptionHandling) {
		return () -> {
			try {
				return supplier.fetch();
			} catch (SQLException e) {
				if (exceptionHandling != null) {
					exceptionHandling.accept(e);
					return null;
				} else {
					throw new AsynchronousOperationException(e);
				}
			}
		};
	}

	/**
	 * Handles an {@code SQLException} that gets thrown inside a {@code Runnable}.
	 *
	 * @param runnable          The {@code Runnable} that throws the exception.
	 * @param exceptionHandling The exception handling supplied for this exception.
	 * @return The original {@code Runnable} but now with the added exception handling.
	 */
	public static Runnable runnableHandling(ThrowableRunnable<SQLException> runnable, Consumer<SQLException> exceptionHandling) {
		return () -> {
			try {
				runnable.doAction();
			} catch (SQLException e) {
				if (exceptionHandling != null) {
					exceptionHandling.accept(e);
				} else {
					throw new AsynchronousOperationException(e);
				}
			}
		};
	}

	/**
	 * Tries to perform a certain action while considering a checked exception that could occur.
	 *
	 * @param runnable The {@code Runnable} to try to execute.
	 * @param <E>      The type of checked exception.
	 */
	public static <E extends Throwable> void tryAction(ThrowableRunnable<E> runnable) {
		try {
			runnable.doAction();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
