package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.BaseMapper;
import com.github.collinalpert.java2db.queries.OrderTypes;
import com.github.collinalpert.java2db.queries.Query;
import com.github.collinalpert.java2db.utilities.IoC;
import com.github.collinalpert.java2db.utilities.Utilities;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class that provides base functionality for all service classes. Every service class must extend this class.
 *
 * @author Collin Alpert
 */
public class BaseService<T extends BaseEntity> {

	private final Class<T> type;
	private final String tableName;
	private final BaseMapper<T> baseMapper;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");


	/**
	 * Constructor for the base class of all services. It is not possible to create instances of it.
	 */
	protected BaseService() {
		this.type = getGenericType();
		this.baseMapper = new BaseMapper<>(type);
		this.tableName = String.format("`%s`", Utilities.getTableName(type));
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
		var insertQuery = new StringBuilder("insert into ").append(tableName).append(" (");
		var databaseFields = Utilities.getEntityFields(instance.getClass()).stream().map(field -> String.format("`%s`", field.getName())).collect(Collectors.joining(", "));
		insertQuery.append(databaseFields).append(") values");
		List<String> values = new ArrayList<>();
		Utilities.getEntityFields(instance.getClass(), BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			try {
				var value = field.get(instance);
				if (value == null) {
					values.add("null");
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
					values.add(String.format("'%s'", dateTimeFormatter.format(dateTime)));
					return;
				}
				if (value instanceof LocalDate) {
					var date = (LocalDate) value;
					values.add(String.format("'%s'", dateFormatter.format(date)));
					return;
				}
				if (value instanceof LocalTime) {
					var time = (LocalTime) value;
					values.add(String.format("'%s'", timeFormatter.format(time)));
					return;
				}
				values.add(value.toString());
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to get value from field %s for type %s\n", field.getName(), type.getSimpleName());
			}
		});
		values.add("default");
		insertQuery.append(" (").append(String.join(", ", values)).append(")");
		try (var connection = new DBConnection()) {
			connection.update(insertQuery.toString());
			Utilities.logf("%s successfully created!", type.getSimpleName());
		}
	}
	//endregion

	//region Count

	/**
	 * An overload of the {@link #count(SqlPredicate)} method. It will count all the rows in a table.
	 *
	 * @return The amount of rows in this table.
	 */
	public long count() {
		return count(x -> true);
	}

	/**
	 * Counts the rows matching a certain condition.
	 *
	 * @param predicate The condition to test for.
	 * @return The number of rows matching the condition.
	 */
	public long count(SqlPredicate<T> predicate) {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select count(*) from %s where %s", tableName, Lambda2Sql.toSql(predicate, tableName)))) {
				if (result.next()) {
					return result.getLong("count(*)");
				}
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Could not get amount of rows for this predicate.");
		}
	}

	//endregion

	//region Any

	/**
	 * Checks if a table has at least one row.
	 *
	 * @return {@code true} if at least one row exists in the table, {@code false} if not.
	 */
	public boolean any() {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select count(*) from (select 1 from %s limit 1) as x", this.tableName))) {
				if (result.next()) {
					return result.getLong("count(*)") == 1;
				}
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Could not check if this table has any rows.");
		}
	}

	/**
	 * Checks if a value matching the condition exists in the table.
	 *
	 * @param predicate The condition to check for.
	 * @return {@code true} if the predicate matches one or more records, {@code false} if not.
	 */
	public boolean any(SqlPredicate<T> predicate) {
		return count(predicate) > 0;
	}

	//endregion

	//region Read

	/**
	 * @return a {@link Query} object with which a DQL statement can be build, using operations like order by, limit etc.
	 */
	protected Query<T> createQuery() {
		if (IoC.isMapperRegistered(type)) {
			return new Query<>(type, IoC.resolveMapper(type));
		}
		return new Query<>(type, baseMapper);
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
		return createQuery().where(predicate).getFirst();
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
		return createQuery().where(predicate);
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

	/**
	 * Gets all values from the table but limits the result.
	 *
	 * @param limit The maximum of records to return.
	 * @return A list with the maximum size of the parameter specified.
	 */
	public List<T> getAll(int limit) {
		return getMultiple(x -> true).limit(limit).get();
	}

	/**
	 * Gets all values from the table and orders them in an ascending order.
	 *
	 * @param orderBy The property to order by.
	 * @return A list of all records ordered by a specific property in an ascending order.
	 */
	public List<T> getAll(SqlFunction<T, ?> orderBy) {
		return getAll(orderBy, OrderTypes.ASCENDING);
	}

	/**
	 * Gets all values from the table and orders them in the specified order.
	 *
	 * @param orderBy     The property to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @return A list of all records ordered by a specific property in the specified order.
	 */
	public List<T> getAll(SqlFunction<T, ?> orderBy, OrderTypes sortingType) {
		return getMultiple(x -> true).orderBy(orderBy, sortingType).get();
	}

	/**
	 * Gets all values from the table, orders them in an ascending order and limits the result.
	 *
	 * @param orderBy The property to order by.
	 * @param limit   The maximum records to return.
	 * @return A list with the maximum size of the parameter specified and in an ascending order.
	 */
	public List<T> getAll(SqlFunction<T, ?> orderBy, int limit) {
		return getAll(orderBy, OrderTypes.ASCENDING, limit);
	}

	/**
	 * Gets all values from the table, orders them in a specific order and limits the result.
	 *
	 * @param orderBy     The property to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @param limit       The maximum records to return.
	 * @return A list with the maximum size of the parameter specified and in an ascending order.
	 */
	public List<T> getAll(SqlFunction<T, ?> orderBy, OrderTypes sortingType, int limit) {
		return getMultiple(x -> true).orderBy(orderBy, sortingType).limit(limit).get();
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
		var updateQuery = new StringBuilder("update ").append(tableName).append(" set ");
		ArrayList<String> fieldSetterList = new ArrayList<>();
		Utilities.getEntityFields(instance.getClass(), BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			try {
				var value = field.get(instance);
				if (value == null) {
					fieldSetterList.add(String.format("`%s` = null", field.getName()));
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
					fieldSetterList.add(String.format("`%s` = '%s'", field.getName(), dateTimeFormatter.format(dateTime)));
					return;
				}
				if (value instanceof LocalDate) {
					var date = (LocalDate) value;
					fieldSetterList.add(String.format("`%s` = '%s'", field.getName(), dateFormatter.format(date)));
					return;
				}
				if (value instanceof LocalTime) {
					var time = (LocalTime) value;
					fieldSetterList.add(String.format("`%s` = '%s'", field.getName(), timeFormatter.format(time)));
					return;
				}
				fieldSetterList.add(String.format("`%s` = %s", field.getName(), value));
			} catch (IllegalAccessException e) {
				System.err.printf("Error getting value for field %s from type %s\n", field.getName(), type.getSimpleName());
			}
		});
		updateQuery.append(String.join(", ", fieldSetterList))
				.append(" where id = ").append(instance.getId());
		try (var connection = new DBConnection()) {
			connection.update(updateQuery.toString());
			Utilities.logf("%s with id %d was successfully updated", type.getSimpleName(), instance.getId());
		}
	}
	//endregion

	//region Delete

	/**
	 * Deletes the corresponding row on the database.
	 *
	 * @param instance The instance to delete on the database.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	public void delete(T instance) throws SQLException {
		delete(instance.getId());
	}

	/**
	 * Deletes a row by an id.
	 *
	 * @param id The row with this id to delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	public void delete(long id) throws SQLException {
		try (var connection = new DBConnection()) {
			connection.update(String.format("delete from %s where %s.id = ?", this.tableName, this.tableName), id);
			Utilities.logf("%s with id %s successfully deleted!", type.getSimpleName(), id);
		}
	}

	/**
	 * Deletes rows based on a condition.
	 *
	 * @param predicate The condition to delete by.
	 * @throws SQLException in case the condition cannot be applied or if a foreign key constraint fails.
	 */
	public void delete(SqlPredicate<T> predicate) throws SQLException {
		try (var connection = new DBConnection()) {
			connection.update(String.format("delete from %s where %s", this.tableName, Lambda2Sql.toSql(predicate, this.tableName)));
			Utilities.logf("%s successfully deleted!", type.getSimpleName());
		}
	}
	//endregion

	@SuppressWarnings("unchecked")
	private Class<T> getGenericType() {
		return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}
}