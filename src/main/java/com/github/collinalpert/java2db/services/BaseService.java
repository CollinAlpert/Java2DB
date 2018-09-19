package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.TableName;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.BaseMapper;
import com.github.collinalpert.java2db.utilities.Lambda2Sql;
import com.github.collinalpert.java2db.utilities.SqlPredicate;
import com.github.collinalpert.java2db.utilities.Utilities;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
		this.mapper = new BaseMapper<>(clazz);
		this.typeName = clazz.getSimpleName();
		var tableNameAnnotation = clazz.getAnnotation(TableName.class);
		if (tableNameAnnotation == null) {
			this.tableName = this.typeName.toLowerCase();
			return;
		}
		this.tableName = tableNameAnnotation.value();
	}


	//region Create

	/**
	 * Creates a Java entity on the database.
	 *
	 * @param instance The instance to create on the database.
	 * @return {@code True} if the INSERT was successful, {@code false} if not.
	 */
	public boolean create(T instance) {
		var insertQuery = new StringBuilder("insert into `").append(tableName).append("` (");
		var databaseFields = Utilities.getAllFields(instance).stream().map(Field::getName).collect(Collectors.joining(", "));
		insertQuery.append(databaseFields).append(") values");
		List<String> values = new ArrayList<>();
		Utilities.getAllFields(instance, BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			try {
				var value = field.get(instance);
				if (value == null) {
					values.add("default");
					return;
				}
				if (value instanceof String) {
					values.add(String.format("'%s'", value));
					return;
				}
				if (value instanceof Boolean) {
					var bool = (boolean) value;
					values.add(Integer.toString(bool ? 1 : 0));
					return;
				}
				if (value instanceof LocalDateTime) {
					var dateTime = (LocalDateTime) value;
					values.add(String.format("'%d-%d-%d %d:%d:%d'", dateTime.getYear(), dateTime.getMonthValue(),
							dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
					return;
				}
				if (value instanceof LocalDate) {
					var date = (LocalDate) value;
					values.add(String.format("'%d-%d-%d'", date.getYear(), date.getMonthValue(),
							date.getDayOfMonth()));
					return;
				}
				if (value instanceof LocalTime) {
					var time = (LocalTime) value;
					values.add(String.format("'%d:%d:%d'", time.getHour(), time.getMinute(), time.getSecond()));
					return;
				}
				values.add(value.toString());
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to get value from field %s for type %s\n", field.getName(), typeName);
			}
		});
		values.add("default");
		insertQuery.append(" (").append(String.join(", ", values)).append(")");
		Utilities.log(insertQuery.toString());
		try (var connection = new DBConnection()) {
			if (connection.update(insertQuery.toString())) {
				Utilities.logf("%s successfully created!", typeName);
				return true;
			} else {
				System.err.printf("Unable to create type %s.\n", typeName);
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
	public Optional<T> getSingle(SqlPredicate<T> predicate) {
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
	public Optional<T> getById(long id) {
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
	 * @return {@code True} if the update is successful, {@code false} if not.
	 */
	public boolean update(T instance) {
		var updateQuery = new StringBuilder("update `").append(tableName).append("` set ");
		ArrayList<String> fieldSetterList = new ArrayList<>();
		Utilities.getAllFields(instance, BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			try {
				var value = field.get(instance);
				if (value == null) {
					fieldSetterList.add(String.format("%s = default", field.getName()));
				}
				if (value instanceof String) {
					fieldSetterList.add(String.format("%s = '%s'", field.getName(), value));
					return;
				}
				if (value instanceof Boolean) {
					var bool = (boolean) value;
					fieldSetterList.add(String.format("%s = %d", field.getName(), bool ? 1 : 0));
					return;
				}
				if (value instanceof LocalDateTime) {
					var dateTime = (LocalDateTime) value;
					fieldSetterList.add(String.format("%s = '%d-%d-%d %d:%d:%d'", field.getName(), dateTime.getYear(), dateTime.getMonthValue(),
							dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
					return;
				}
				if (value instanceof LocalDate) {
					var date = (LocalDate) value;
					fieldSetterList.add(String.format("%s = '%d-%d-%d'", field.getName(), date.getYear(), date.getMonthValue(),
							date.getDayOfMonth()));
					return;
				}
				if (value instanceof LocalTime) {
					var time = (LocalTime) value;
					fieldSetterList.add(String.format("%s = '%d:%d:%d'", field.getName(), time.getHour(), time.getMinute(), time.getSecond()));
					return;
				}
				fieldSetterList.add(field.getName() + " = " + value);
			} catch (IllegalAccessException e) {
				System.err.printf("Error getting value for field %s from type %s\n", field.getName(), typeName);
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
	public void delete(long id) {
		try (var connection = new DBConnection()) {
			boolean success = connection.update(String.format("delete from `%s` where id=?", tableName), id);
			if (success) {
				Utilities.logf("%s with id %d successfully deleted!", typeName, id);
			}
		}
	}
	//endregion
}