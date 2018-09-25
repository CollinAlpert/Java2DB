package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.TableName;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.BaseMapper;
import com.github.collinalpert.java2db.queries.Query;
import com.github.collinalpert.java2db.utilities.Utilities;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.sql.SQLException;
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
	 * @param type The entity class corresponding to this service class.
	 */
	public BaseService(Class<T> type) {
		this.mapper = new BaseMapper<>(type);
		this.typeName = type.getSimpleName();
		var tableNameAnnotation = type.getAnnotation(TableName.class);
		if (tableNameAnnotation == null) {
			this.tableName = this.typeName.toLowerCase();
			return;
		}
		this.tableName = tableNameAnnotation.value();
	}


	//region Create

	/**
	 * Creates this Java entity on the database.
	 *
	 * @param instance The instance to create on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public void create(T instance) throws SQLException {
		var insertQuery = new StringBuilder("insert into `").append(tableName).append("` (");
		var databaseFields = Utilities.getAllFields(instance).stream().map(field -> String.format("`%s`", field.getName())).collect(Collectors.joining(", "));
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
			connection.update(insertQuery.toString());
			Utilities.logf("%s successfully created!", typeName);
		}
	}
	//endregion

	//region Read

	/**
	 * @return a {@link Query} object with which a DQL statement can be build, using operations like order by, limit etc.
	 */
	protected Query<T> selectQuery() {
		return new Query<>(tableName, mapper);
	}

	/**
	 * Creates a DQL statement which contains a sub select.
	 *
	 * @param subSelect The sub select to select from.
	 * @return A {@link Query} object containing the sub select.
	 */
	protected Query<T> subSelectQuery(Query<T> subSelect) {
		return new Query<>(subSelect, mapper);
	}

	/**
	 * Retrieves a single entity which matches the predicate.
	 * It is {@code protected} as it is only meant for use in methods of the respective service.
	 * This is to keep good programming practice and create descriptive methods for what kind of data you are getting.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL query.
	 * @return An entity matching the result of the query.
	 */
	protected Optional<T> getSingle(SqlPredicate<T> predicate) {
		return new Query<>(tableName, mapper).where(predicate).getFirst();
	}

	/**
	 * Retrieves list of entities which match the predicate.
	 * It is {@code protected} as it is only meant for use in methods of the respective service.
	 * This is to keep good programming practice and create descriptive methods for what kind of data you are getting.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL statement.
	 * @return A list of entities matching the result of the query.
	 */
	protected Query<T> getMultiple(SqlPredicate<T> predicate) {
		return new Query<>(tableName, mapper).where(predicate);
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
		return getMultiple(x -> true).get();
	}
	//endregion

	//region Update

	/**
	 * Updates this entity's row on the database.
	 *
	 * @param instance The instance to update on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public void update(T instance) throws SQLException {
		var updateQuery = new StringBuilder("update `").append(tableName).append("` set ");
		ArrayList<String> fieldSetterList = new ArrayList<>();
		Utilities.getAllFields(instance, BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			try {
				var value = field.get(instance);
				if (value == null) {
					fieldSetterList.add(String.format("`%s` = default", field.getName()));
				}
				if (value instanceof String) {
					fieldSetterList.add(String.format("`%s` = '%s'", field.getName(), value));
					return;
				}
				if (value instanceof Boolean) {
					var bool = (boolean) value;
					fieldSetterList.add(String.format("`%s` = %d", field.getName(), bool ? 1 : 0));
					return;
				}
				if (value instanceof LocalDateTime) {
					var dateTime = (LocalDateTime) value;
					fieldSetterList.add(String.format("`%s` = '%d-%d-%d %d:%d:%d'", field.getName(), dateTime.getYear(), dateTime.getMonthValue(),
							dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
					return;
				}
				if (value instanceof LocalDate) {
					var date = (LocalDate) value;
					fieldSetterList.add(String.format("`%s` = '%d-%d-%d'", field.getName(), date.getYear(), date.getMonthValue(),
							date.getDayOfMonth()));
					return;
				}
				if (value instanceof LocalTime) {
					var time = (LocalTime) value;
					fieldSetterList.add(String.format("`%s` = '%d:%d:%d'", field.getName(), time.getHour(), time.getMinute(), time.getSecond()));
					return;
				}
				fieldSetterList.add(String.format("`%s` = %s", field.getName(), value));
			} catch (IllegalAccessException e) {
				System.err.printf("Error getting value for field %s from type %s\n", field.getName(), typeName);
			}
		});
		updateQuery.append(String.join(", ", fieldSetterList))
				.append(" where id = ").append(instance.getId());
		Utilities.log(updateQuery.toString());
		try (var connection = new DBConnection()) {
			connection.update(updateQuery.toString());
			Utilities.logf("%s with id %d was successfully updated", typeName, instance.getId());
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
			connection.update(String.format("delete from `%s` where id=?", tableName), id);
			Utilities.logf("%s with id %d successfully deleted!", typeName, id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//endregion
}