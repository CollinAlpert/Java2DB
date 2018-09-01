package de.java2db.services;

import de.java2db.database.DBConnection;
import de.java2db.database.TableName;
import de.java2db.entities.BaseEntity;
import de.java2db.mappers.BaseMapper;
import de.java2db.utilities.Lambda2Sql;
import de.java2db.utilities.SqlPredicate;
import de.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Collin Alpert
 * <p>
 * Class that provides base functionality for all service classes. Every service class must extend this class.
 * </p>
 */
public class BaseService<T extends BaseEntity> {

	private final String typeName;
	private final String tableName;

	private BaseMapper<T> mapper;

	/**
	 * Constructor for the base class of all services. It is possible to create instances of it.
	 *
	 * @param clazz The entity class corresponding to this service class.
	 */
	public BaseService(Class<T> clazz) {
		mapper = new BaseMapper<>(clazz);
		String name;
		typeName = name = clazz.getSimpleName();
		if (clazz.getAnnotation(TableName.class) == null) {
			tableName = name.toLowerCase();
			return;
		}
		tableName = clazz.getAnnotation(TableName.class).value();
	}


	//region Create

	/**
	 * Creates a Java entity on the database.
	 *
	 * @param instance The instance to create on the database.
	 * @return <code>True</code> if the INSERT was successful, <code>false</code> if not.
	 */
	public boolean create(T instance) {
		var insertQuery = new StringBuilder("insert into ").append(tableName).append(" (");
		var databaseFields = Utilities.getAllFields(instance).stream().map(Field::getName).collect(Collectors.joining(", "));
		insertQuery.append(databaseFields).append(") values (");
		Utilities.getAllFields(instance, BaseEntity.class).forEach(x -> {
			x.setAccessible(true);
			try {
				var value = x.get(instance);
				if (value == null) {
					insertQuery.append("default, ");
					return;
				}
				if (value instanceof String) {
					insertQuery.append("'").append(value).append("'").append(", ");
					return;
				}
				insertQuery.append(value).append(", ");
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to get value from field %s for type %s\n", x.getName(), typeName);
			}
		});
		insertQuery.append("default)");
		Utilities.log(insertQuery.toString());
		try (var connection = new DBConnection()) {
			if (connection.update(insertQuery.toString())) {
				Utilities.logf("%s successfully created!", typeName);
				return true;
			} else {
				System.err.printf("Unable to create type %s\n", typeName);
				return false;
			}
		}
	}
	//endregion

	//region Read

	/**
	 * @param predicate  The {@link SqlPredicate} for constraints on the SELECT query.
	 * @param connection A connection to execute the query on.
	 * @return A {@link ResultSet} by a predicate.
	 */
	protected ResultSet getByPredicate(SqlPredicate<T> predicate, DBConnection connection) {
		var query = "select * from " + tableName + " where " + Lambda2Sql.toSql(predicate);
		Utilities.log(query);
		return connection.execute(query);
	}

	/**
	 * @param predicate The {@link SqlPredicate} to add constraints to a SELECT query.
	 * @return An entity matching the result of the query.
	 */
	public T getSingle(SqlPredicate<T> predicate) {
		try (var connection = new DBConnection()) {
			return mapper.map(getByPredicate(predicate, connection));
		}
	}

	/**
	 * @param predicate The {@link SqlPredicate} to add constraints to a SELECT query.
	 * @return A list of entities matching the result of the query.
	 */
	public List<T> getMultiple(SqlPredicate<T> predicate) {
		try (var connection = new DBConnection()) {
			return mapper.mapToList(getByPredicate(predicate, connection));
		}
	}

	/**
	 * @param id The id of the desired entity.
	 * @return Gets an entity by its id.
	 */
	public T getById(int id) {
		return getSingle(x -> x.getId() == id);
	}

	/**
	 * @return All entities in this table.
	 */
	public List<T> getAll() {
		return getMultiple(x -> true);
	}
	//endregion

	//region Update

	/**
	 * Applies updates to an entity on the database.
	 *
	 * @param instance The instance to update on the interface.
	 * @return <code>True</code> if the update is successful, <code>false</code> if not.
	 */
	public boolean update(T instance) {
		var updateQuery = new StringBuilder("update ").append(tableName).append(" set ");
		ArrayList<String> fieldSetterList = new ArrayList<>();
		Utilities.getAllFields(instance, BaseEntity.class).forEach(x -> {
			x.setAccessible(true);
			try {
				var value = x.get(instance);
				if (value instanceof String) {
					fieldSetterList.add(String.format("%s = '%s'", x.getName(), value));
					return;
				}
				fieldSetterList.add(x.getName() + " = " + value);
			} catch (IllegalAccessException e) {
				System.err.printf("Error getting value for field %s from type %s\n", x.getName(), typeName);
			}
		});
		updateQuery.append(String.join(", ", fieldSetterList))
				.append(" where id = ").append(instance.getId());
		Utilities.log(updateQuery.toString());
		try (var connection = new DBConnection()) {
			if (connection.update(updateQuery.toString())) {
				Utilities.logf("%s with id %d was successfully updated", typeName, instance.getId());
				return true;
			} else {
				System.err.printf("%s with id %d could not be updated\n", typeName, instance.getId());
				return false;
			}
		}
	}
	//endregion

	//region Delete

	/**
	 * Deletes the corresponding row on the database.
	 *
	 * @param instance The instance to delete on the database.
	 */
	public void delete(T instance) {
		delete(instance.getId());
	}

	/**
	 * Deletes a row by an id.
	 *
	 * @param id The row with this id to delete.
	 */
	public void delete(int id) {
		try (var connection = new DBConnection()) {
			boolean success = connection.update("delete from " + tableName + " where id=?", id);
			if (success) {
				Utilities.logf("%s with id %d successfully deleted!", typeName, id);
			}
		}
	}
	//endregion
}