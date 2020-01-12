package com.github.collinalpert.java2db.services;

import com.github.collinalpert.java2db.annotations.DefaultIfNull;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.exceptions.IllegalEntityFieldAccessException;
import com.github.collinalpert.java2db.modules.AnnotationModule;
import com.github.collinalpert.java2db.modules.FieldModule;
import com.github.collinalpert.java2db.modules.TableModule;
import com.github.collinalpert.java2db.pagination.CacheablePaginationResult;
import com.github.collinalpert.java2db.pagination.PaginationResult;
import com.github.collinalpert.java2db.queries.EntityQuery;
import com.github.collinalpert.java2db.queries.OrderTypes;
import com.github.collinalpert.java2db.queries.SingleEntityQuery;
import com.github.collinalpert.java2db.utilities.FunctionUtils;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Class that provides base functionality for all service classes. Every service class must extend this class.
 *
 * @author Collin Alpert
 */
public class BaseService<E extends BaseEntity> {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final AnnotationModule annotationModule;
	private static final FieldModule fieldModule;
	private static final TableModule tableModule;

	static {
		annotationModule = AnnotationModule.getInstance();
		fieldModule = FieldModule.getInstance();
		tableModule = TableModule.getInstance();
	}

	/**
	 * The generic type of this service.
	 */
	protected final Class<E> type;

	/**
	 * Represents the table name of the entity this services corresponds to.
	 * It contains the table name with escaping backticks.
	 */
	protected final String tableName;

	/**
	 * Represents the id column being accessed.
	 */
	private final String idAccess;

	/**
	 * Constructor for the base class of all services. It is not possible to create instances of it.
	 */
	protected BaseService() {
		this.type = getGenericType();
		this.tableName = tableModule.getTableName(this.type);

		final SqlFunction<E, Long> idFunc = BaseEntity::getId;
		this.idAccess = Lambda2Sql.toSql(idFunc, this.tableName);
	}

	//region Create

	/**
	 * Creates this Java entity on the database and sets the newly created id for the entity.
	 *
	 * @param instance The instance to create on the database.
	 * @return The id of the newly created record.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type or a foreign key constraint.
	 */
	public long create(E instance) throws SQLException {
		var insertQuery = createInsertHeader();
		var joiner = new StringJoiner(", ", "(", ");");
		fieldModule.getEntityFields(instance.getClass(), BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			if (getFieldValue(field, instance) == null && annotationModule.hasAnnotation(field, DefaultIfNull.class, DefaultIfNull::onCreate)) {
				joiner.add("default");
				return;
			}

			joiner.add(getSqlValue(field, instance));
		});

		//For using the default database setting for the id.
		joiner.add("default");
		insertQuery.append(joiner.toString());
		try (var connection = new DBConnection()) {
			var id = connection.update(insertQuery.toString());
			instance.setId(id);
			return id;
		}
	}

	/**
	 * Creates a variable amount of entities on the database.
	 *
	 * @param instances The instances to create.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type or a foreign key constraint.
	 * @see #create(List)
	 */
	@SuppressWarnings("unchecked")
	public void create(E... instances) throws SQLException {
		create(Arrays.asList(instances));
	}

	/**
	 * Creates multiple entities on the database.
	 * It is recommended to use this method instead of iterating over the list and
	 * calling a normal {@link #create(BaseEntity)} on each entity separately.
	 *
	 * @param instances The list of entities to create on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type or a foreign key constraint.
	 */
	public void create(List<E> instances) throws SQLException {
		if (instances.isEmpty()) {
			return;
		}

		var insertQuery = createInsertHeader();
		var rows = new String[instances.size()];
		StringJoiner joiner;
		for (int i = 0, instancesSize = instances.size(); i < instancesSize; i++) {
			var entityFields = fieldModule.getEntityFields(instances.get(i).getClass(), BaseEntity.class);
			joiner = new StringJoiner(", ", "(", ")");
			for (var entityField : entityFields) {
				entityField.setAccessible(true);
				var instance = instances.get(i);
				if (getFieldValue(entityField, instance) == null && annotationModule.hasAnnotation(entityField, DefaultIfNull.class, DefaultIfNull::onCreate)) {
					joiner.add("default");
					return;
				}

				joiner.add(getSqlValue(entityField, instance));
			}

			//For using the default database setting for the id.
			joiner.add("default");
			rows[i] = joiner.toString();
		}

		insertQuery.append(String.join(", ", rows));
		try (var connection = new DBConnection()) {
			connection.update(insertQuery.toString());
		}
	}

	//endregion

	//region Read

	//region Count

	/**
	 * An overload of the {@link #count(SqlPredicate)} method. It will count all the rows that have an id, since this should be a reasonable criteria.
	 * Use with caution, as this call can take quite a while.
	 *
	 * @return The amount of rows in this table.
	 */
	public long count() {
		return count(x -> true);
	}

	/**
	 * Counts the rows that have an id (which should usually be every row) and match a certain condition. Note that COUNT operations usually are very slow.
	 *
	 * @param predicate The condition to test for.
	 * @return The number of rows matching the condition.
	 */
	public long count(SqlPredicate<E> predicate) {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select count(%s) from `%s` where %s;", this.idAccess, this.tableName, Lambda2Sql.toSql(predicate, this.tableName)))) {
				if (result.next()) {
					return result.getLong(String.format("count(%s)", this.idAccess));
				}

				return 0;
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(String.format("Could not get amount of rows in table %s for this predicate.", this.tableName), e);
		}
	}

	//endregion

	//region Any

	/**
	 * Checks if a table has at least one row.
	 *
	 * @return {@code True} if at least one row exists in the table, {@code false} if not.
	 */
	public boolean any() {
		return any(x -> true);
	}

	/**
	 * Checks if a value matching the condition exists in the table.
	 *
	 * @param predicate The condition to check for.
	 * @return {@code True} if the predicate matches one or more records, {@code false} if not.
	 */
	public boolean any(SqlPredicate<E> predicate) {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select exists(select %s from `%s` where %s limit 1) as result;", this.idAccess, this.tableName, Lambda2Sql.toSql(predicate, this.tableName)))) {
				if (result.next()) {
					return result.getInt("result") == 1;
				}

				return false;
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(String.format("Could not check if a row matches this condition on table %s.", this.tableName), e);
		}
	}

	//endregion

	//region Max

	/**
	 * Gets the maximum value of a column in a table.
	 *
	 * @param column The column to get the maximum value of.
	 * @param <T>    The generic type of the column. It is also the return type.
	 * @return The maximum value of the column.
	 */
	public <T> T max(SqlFunction<E, T> column) {
		return max(column, FunctionUtils.alwaysTrue());
	}

	/**
	 * Gets the maximum value of a column in a set of values filtered by a condition.
	 *
	 * @param column    The column to get the maximum value of.
	 * @param predicate The predicate to filter by.
	 * @param <T>       The generic type of the column. It is also the return type.
	 * @return The maximum value of the column.
	 */
	public <T> T max(SqlFunction<E, T> column, SqlPredicate<E> predicate) {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select max(%s) from `%s` where %s;", Lambda2Sql.toSql(column, this.tableName), this.tableName, Lambda2Sql.toSql(predicate, this.tableName)))) {
				if (result.next()) {
					return (T) result.getObject(1);
				}

				return null;
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(String.format("Could not get maximum value of column %s in table %s.", Lambda2Sql.toSql(column), this.tableName), e);
		}
	}

	//endregion

	//region Min

	/**
	 * Gets the minimum value of a column in a table.
	 * Please note that this does not include {@code null} values.
	 *
	 * @param column The column to get the minimum value of.
	 * @param <T>    The generic type of the column. It is also the return type.
	 * @return The minimum value of the column.
	 */
	public <T> T min(SqlFunction<E, T> column) {
		return min(column, FunctionUtils.alwaysTrue());
	}

	/**
	 * Gets the minimum value of a column in a set of values filtered by a condition.
	 * Please note that this does not include {@code null} values.
	 *
	 * @param column    The column to get the minimum value of.
	 * @param predicate The predicate to filter by.
	 * @param <T>       The generic type of the column. It is also the return type.
	 * @return The minimum value of the column.
	 */
	public <T> T min(SqlFunction<E, T> column, SqlPredicate<E> predicate) {
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select min(%s) from `%s` where %s;", Lambda2Sql.toSql(column, this.tableName), this.tableName, Lambda2Sql.toSql(predicate, this.tableName)))) {
				if (result.next()) {
					return (T) result.getObject(1);
				}

				return null;
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(String.format("Could not get minimum value of column %s in table %s.", Lambda2Sql.toSql(column), this.tableName), e);
		}
	}

	//endregion

	//region HasDuplicates

	/**
	 * Checks if duplicate values exist for a specific column in a table.
	 *
	 * @param column The column to check for duplicate values for.
	 * @return {@code True} if there is at least one duplicate value in the specified column, {@code false} otherwise.
	 */
	public boolean hasDuplicates(SqlFunction<E, ?> column) {
		var sqlColumn = Lambda2Sql.toSql(column, this.tableName);
		try (var connection = new DBConnection()) {
			try (var result = connection.execute(String.format("select %s from `%s` group by %s having count(%s) > 1", sqlColumn, this.tableName, sqlColumn, sqlColumn))) {
				return result.next();
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(String.format("Could not check if duplicate values exist in column %s on table %s.", sqlColumn, this.tableName), e);
		}
	}

	//endregion

	//region Query

	/**
	 * @return a {@link EntityQuery} object with which a DQL statement can be built, using operations like order by, limit etc.
	 * If you do not require a plain {@link EntityQuery}, please consider using the
	 * {@link #getSingle(SqlPredicate)}, {@link #getMultiple(SqlPredicate)} or {@link #getAll()} methods.
	 */
	protected EntityQuery<E> createQuery() {
		return new EntityQuery<>(this.type);
	}

	/**
	 * @return a {@link SingleEntityQuery} object with which a DQL statement can be built, which returns a single record.
	 * If you do not require a plain {@link SingleEntityQuery}, please consider using the
	 * {@link #getSingle(SqlPredicate)}, {@link #getMultiple(SqlPredicate)} or {@link #getAll()} methods.
	 */
	protected SingleEntityQuery<E> createSingleQuery() {
		return new SingleEntityQuery<>(this.type);
	}

	/**
	 * Retrieves a single entity which matches the predicate.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL query.
	 * @return An entity matching the result of the query.
	 */
	public Optional<E> getFirst(SqlPredicate<E> predicate) {
		return createSingleQuery().where(predicate).first();
	}

	/**
	 * Retrieves a single entity which matches the predicate.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL query.
	 * @return An entity matching the result of the query.
	 */
	public SingleEntityQuery<E> getSingle(SqlPredicate<E> predicate) {
		return createSingleQuery().where(predicate);
	}

	/**
	 * Retrieves list of entities which match the predicate.
	 *
	 * @param predicate The {@link SqlPredicate} to add constraints to a DQL statement.
	 * @return A list of entities matching the result of the query.
	 */
	public EntityQuery<E> getMultiple(SqlPredicate<E> predicate) {
		return createQuery().where(predicate);
	}

	/**
	 * @param id The id of the desired entity.
	 * @return Gets an entity by its id.
	 */
	public Optional<E> getById(long id) {
		return getFirst(x -> x.getId() == id);
	}

	/**
	 * @return All entities in this table.
	 */
	public List<E> getAll() {
		return createQuery().toList();
	}

	/**
	 * Gets all values from the table and orders them in an ascending order.
	 *
	 * @param orderBy The property to order by.
	 * @return A list of all records ordered by a specific property in an ascending order.
	 */
	public List<E> getAll(SqlFunction<E, ?> orderBy) {
		return createQuery().orderBy(orderBy).toList();
	}

	/**
	 * Gets all values from the table and orders them in an ascending order by multiple columns in a coalescing manner.
	 *
	 * @param orderBy The properties to order by.
	 * @return A list of all records ordered by a specific property in an ascending order.
	 */
	public List<E> getAll(SqlFunction<E, ?>[] orderBy) {
		return createQuery().orderBy(orderBy).toList();
	}

	/**
	 * Gets all values from the table and orders them in the specified order by multiple columns in a coalescing manner.
	 *
	 * @param orderBy     The property to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @return A list of all records ordered by a specific property in the specified order.
	 */
	public List<E> getAll(OrderTypes sortingType, SqlFunction<E, ?> orderBy) {
		return createQuery().orderBy(sortingType, orderBy).toList();
	}

	/**
	 * Gets all values from the table and orders them in the specified order by multiple columns in a coalescing manner.
	 *
	 * @param orderBy     The properties to order by.
	 * @param sortingType The order direction. Can be either ascending or descending.
	 * @return A list of all records ordered by a specific property in the specified order.
	 */

	public List<E> getAll(OrderTypes sortingType, SqlFunction<E, ?>[] orderBy) {
		return createQuery().orderBy(sortingType, orderBy).toList();
	}

	//endregion EntityQuery

	//region Pagination

	/**
	 * Creates a pagination structure that splits the entire table into multiple pages.
	 * Note that the query to the database for each page is only executed when that specific page is requested and
	 * <em>not</em> when this method is called.
	 *
	 * @param entriesPerPage The number of entries to be displayed on each page.
	 * @return A pagination which splits up an entire table into multiple pages.
	 */
	public PaginationResult<E> createPagination(int entriesPerPage) {
		return new PaginationResult<>(createPaginationQueries(entriesPerPage));
	}

	/**
	 * Creates a pagination structure that splits the result of a query into multiple pages.
	 * Note that the query to the database for each page is only executed when that specific page is requested and
	 * <em>not</em> when this method is called.
	 * The predicate to filter by.
	 *
	 * @param predicate      The predicate to filter by.
	 * @param entriesPerPage The number of entries to be displayed on each page.
	 * @return A pagination which splits the result of a condition into multiple pages.
	 */
	public PaginationResult<E> createPagination(SqlPredicate<E> predicate, int entriesPerPage) {
		return new PaginationResult<>(createPaginationQueries(predicate, entriesPerPage));
	}

	/**
	 * Creates a pagination structure that splits the entire table into multiple pages.
	 * Note that the query to the database for each page is only executed when that specific page is requested and
	 * <em>not</em> when this method is called.
	 * When pages are retrieved, they are stored in a cache, so they can be
	 * retrieved from there the next time they are requested. Note that this will work, until the specified {@code cacheExpiration}
	 * is up. After that, the next time a page is requested, it will be reloaded from the database.
	 *
	 * @param entriesPerPage  The number of entries to be displayed on each page.
	 * @param cacheExpiration The time a page is valid in the cache. If a page is requested and in the cache, but expired,
	 *                        it will be retrieved from the database again and re-stored in the cache.
	 * @return A pagination which displays a entire table
	 */
	public CacheablePaginationResult<E> createPagination(int entriesPerPage, Duration cacheExpiration) {
		return new CacheablePaginationResult<>(createPaginationQueries(entriesPerPage), cacheExpiration);
	}

	/**
	 * Creates a cached pagination structure that splits the result of a query into multiple pages.
	 * Note that the query to the database for each page is only executed when that specific page is requested and
	 * <em>not</em> when this method is called.
	 * When pages are retrieved, they are stored in a cache, so they can be
	 * retrieved from there the next time they are requested. Note that this will work, until the specified {@code cacheExpiration}
	 * is up. After that, the next time a page is requested, it will be reloaded from the database.
	 *
	 * @param predicate       The predicate to filter by.
	 * @param entriesPerPage  The number of entries to be displayed on each page.
	 * @param cacheExpiration The time a page is valid in the cache. If a page is requested and in the cache, but expired,
	 *                        it will be retrieved from the database again and re-stored in the cache.
	 * @return A pagination which allows the developer to retrieve specific pages from the result.
	 */
	public CacheablePaginationResult<E> createPagination(SqlPredicate<E> predicate, int entriesPerPage, Duration cacheExpiration) {
		return new CacheablePaginationResult<>(createPaginationQueries(predicate, entriesPerPage), cacheExpiration);
	}

	//endregion

	//endregion

	//region Update

	/**
	 * Updates this entity's row on the database.
	 *
	 * @param instance The instance to update on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public void update(E instance) throws SQLException {
		try (var connection = new DBConnection()) {
			connection.update(updateQuery(instance));
		}
	}

	/**
	 * Variable argument version which behaves the same as the {@link #update(List)} method.
	 *
	 * @param instances The instances to update on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 * @see #update(List)
	 */
	@SuppressWarnings("unchecked")
	public void update(E... instances) throws SQLException {
		update(Arrays.asList(instances));
	}

	/**
	 * Updates multiple entity's rows on the database. This method only opens one connection to the database as oppose
	 * to when calling the {@link #update(BaseEntity)} method in a for loop, which opens a new connection for every entity.
	 *
	 * @param instances The instances to update on the database.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public void update(List<E> instances) throws SQLException {
		try (var connection = new DBConnection()) {
			for (E instance : instances) {
				connection.update(updateQuery(instance));
			}
		}
	}

	private String updateQuery(E instance) {
		var updateQuery = new StringBuilder("update `").append(this.tableName).append("` set ");
		var fieldJoiner = new StringJoiner(", ");
		fieldModule.getEntityFields(instance.getClass(), BaseEntity.class).forEach(field -> {
			field.setAccessible(true);
			var sqlValue = getFieldValue(field, instance) == null && annotationModule.hasAnnotation(field, DefaultIfNull.class, DefaultIfNull::onUpdate) ? "default" : getSqlValue(field, instance);
			fieldJoiner.add(String.format("`%s` = %s", tableModule.getColumnName(field), sqlValue));
		});

		return updateQuery.append(fieldJoiner.toString()).append(String.format(" where %s = ", this.idAccess)).append(instance.getId()).toString();
	}

	/**
	 * Updates a specific column based on the current column value in the database.
	 *
	 * @param entityId         The id of the row to update.
	 * @param column           The column to update.
	 * @param newValueFunction The function to calculate the new value.
	 * @param <R>              The type of the column to update.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public <R> void update(long entityId, SqlFunction<E, R> column, SqlFunction<E, R> newValueFunction) throws SQLException {
		SqlPredicate<E> predicate = x -> x.getId() == entityId;
		var query = String.format("update `%s` set %s = %s where %s", this.tableName, Lambda2Sql.toSql(column, this.tableName), Lambda2Sql.toSql(newValueFunction, this.tableName), Lambda2Sql.toSql(predicate, this.tableName));

		try (var connection = new DBConnection()) {
			connection.update(query);
		}
	}

	/**
	 * Updates a specific column for a single record in a table.
	 * This method is not affected by the {@link DefaultIfNull} annotation.
	 *
	 * @param entityId The id of the record.
	 * @param column   The column to update.
	 * @param newValue The new value of the column.
	 * @param <R>      The data type of the column to update. It must be the same as the data type of the new value.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public <R> void update(long entityId, SqlFunction<E, R> column, R newValue) throws SQLException {
		update(x -> x.getId() == entityId, column, newValue);
	}

	/**
	 * Updates a specific column for records matching a condition in a table.
	 * This method is not affected by the {@link DefaultIfNull} annotation.
	 *
	 * @param condition The condition to update the column by.
	 * @param column    The column to update.
	 * @param newValue  The new value of the column.
	 * @param <R>       The data type of the column to update. It must be the same as the data type of the new value.
	 * @throws SQLException if the query cannot be executed due to database constraints
	 *                      i.e. non-existing default value for field or an incorrect data type.
	 */
	public <R> void update(SqlPredicate<E> condition, SqlFunction<E, R> column, R newValue) throws SQLException {
		var query = String.format("update `%s` set %s = %s where %s;", this.tableName, Lambda2Sql.toSql(column, this.tableName), convertToSql(newValue), Lambda2Sql.toSql(condition, this.tableName));

		try (var connection = new DBConnection()) {
			connection.update(query);
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
	public void delete(E instance) throws SQLException {
		var id = instance.getId();
		delete(x -> x.getId() == id);
	}

	/**
	 * Deletes a row by an id.
	 *
	 * @param id The row with this id to delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	public void delete(long id) throws SQLException {
		delete(x -> x.getId() == id);
	}

	/**
	 * Deletes a list of entities at once.
	 *
	 * @param entities The list of entities to delete.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	public void delete(List<E> entities) throws SQLException {
		var joiner = new StringJoiner(", ", "(", ")");
		for (var entity : entities) {
			joiner.add(Long.toString(entity.getId()));
		}

		var joinedIds = joiner.toString();
		try (var connection = new DBConnection()) {
			connection.update(String.format("delete from `%s` where %s in %s", this.tableName, this.idAccess, joinedIds));
		}
	}

	/**
	 * Deletes multiple entities at once.
	 *
	 * @param entities A variable amount of entities.
	 * @throws SQLException for example because of a foreign key constraint.
	 * @see #delete(List)
	 */
	@SuppressWarnings("unchecked")
	public void delete(E... entities) throws SQLException {
		delete(Arrays.asList(entities));
	}

	/**
	 * Deletes multiple rows with the corresponding ids.
	 *
	 * @param ids The ids to delete the rows by.
	 * @throws SQLException for example because of a foreign key constraint.
	 */
	public void delete(long... ids) throws SQLException {
		var list = new ArrayList<Long>(ids.length);
		for (long id : ids) {
			list.add(id);
		}

		delete(x -> list.contains(x.getId()));
	}

	/**
	 * Deletes rows based on a condition.
	 *
	 * @param predicate The condition to delete by.
	 * @throws SQLException in case the condition cannot be applied or if a foreign key constraint fails.
	 */
	public void delete(SqlPredicate<E> predicate) throws SQLException {
		try (var connection = new DBConnection()) {
			connection.update(String.format("delete from `%s` where %s;", this.tableName, Lambda2Sql.toSql(predicate, this.tableName)));
		}
	}

	/**
	 * Truncates the corresponding table on the database.
	 *
	 * @throws SQLException for example because a foreign key references this table. Note that you will not be able to
	 *                      truncate a table, even if no foreign key references a row in this table. TRUNCATE is a DDL
	 *                      command and cannot check if rows are being referenced or not.
	 */
	public void truncateTable() throws SQLException {
		try (var connection = new DBConnection()) {
			connection.update(String.format("truncate table `%s`;", this.tableName));
		}
	}

	//endregion

	/**
	 * Gets the generic type of this class.
	 *
	 * @return The entity class used as a generic type for this BaseService.
	 */
	@SuppressWarnings("unchecked")
	private Class<E> getGenericType() {
		return ((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	/**
	 * Will create a {@link StringBuilder} containing the beginning of a DML INSERT statement.
	 *
	 * @return An INSERT statement up to the VALUES keyword.
	 */
	private StringBuilder createInsertHeader() {
		return new StringBuilder("insert into `").append(this.tableName).append("` ").append(fieldModule.getEntityFields(this.type)
				.stream().map(field -> String.format("`%s`", tableModule.getColumnName(field)))
				.collect(Collectors.joining(", ", "(", ")"))).append(" values ");
	}

	/**
	 * Gets a field value from an entity and handles the checked exception.
	 *
	 * @param entityField    The field to get the value for.
	 * @param entityInstance The entity to get the value from.
	 * @return The value of the field in the entity.
	 */
	private Object getFieldValue(Field entityField, E entityInstance) {
		try {
			return entityField.get(entityInstance);
		} catch (IllegalAccessException e) {
			throw new IllegalEntityFieldAccessException(entityField.getName(), this.type.getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Returns a field value from an entity in its SQL equivalent.
	 *
	 * @param entityField    The value's field.
	 * @param entityInstance The entity containing the value
	 * @return A {@code String} representing the SQL value of the entity field.
	 */
	private String getSqlValue(Field entityField, E entityInstance) {
		return convertToSql(getFieldValue(entityField, entityInstance));
	}

	/**
	 * Converts a Java object to its SQL representation.
	 *
	 * @param value The value to convert.
	 * @return The SQL version of a Java value.
	 */
	private String convertToSql(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof String) {
			return "'" + value + "'";
		}

		if (value instanceof Boolean) {
			var bool = (boolean) value;
			return bool ? "1" : "0";
		}

		if (value instanceof LocalDateTime) {
			var dateTime = (LocalDateTime) value;
			return "'" + DATE_TIME_FORMATTER.format(dateTime) + "'";
		}

		if (value instanceof LocalDate) {
			var date = (LocalDate) value;
			return "'" + DATE_FORMATTER.format(date) + "'";
		}

		if (value instanceof LocalTime) {
			var time = (LocalTime) value;
			return "'" + TIME_FORMATTER.format(time) + "'";
		}

		return value.toString();
	}

	/**
	 * An overload of the {@link #createPaginationQueries(SqlPredicate, int)} method.
	 * It creates queries for getting all the values from a table.
	 *
	 * @param entriesPerPage The number of entries to be displayed on each page.
	 * @return A list of queries containing the definitions for getting specific pages.
	 */
	private List<EntityQuery<E>> createPaginationQueries(int entriesPerPage) {
		return createPaginationQueries(x -> true, entriesPerPage);
	}

	/**
	 * Creates queries that define the queries for specific pages. Note that this does not execute the queries yet.
	 *
	 * @param predicate      A filter to narrow down the queries.
	 * @param entriesPerPage The number of entries to be displayed on each page.
	 * @return A list of queries containing the definitions for getting specific pages.
	 */
	private List<EntityQuery<E>> createPaginationQueries(SqlPredicate<E> predicate, int entriesPerPage) {
		var count = this.count(predicate);
		var numberOfPages = Math.ceil(count / (double) entriesPerPage);
		var queries = new ArrayList<EntityQuery<E>>((int) numberOfPages);
		for (int i = 0; i < numberOfPages; i++) {
			var offset = i * entriesPerPage;
			//TODO Refactor the limit-offset-combo for performance in bigger data sets.
			queries.add(getMultiple(predicate).limit(entriesPerPage, offset));
		}

		return queries;
	}
}