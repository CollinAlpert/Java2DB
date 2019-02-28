package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.annotations.ForeignKeyEntity;
import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.database.ForeignKeyReference;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.mappers.IMapper;
import com.github.collinalpert.java2db.utilities.Utilities;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;
import com.github.collinalpert.lambda2sql.functions.SqlPredicate;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A class representing a DQL statement with different options, including where clauses, order by clauses and limits.
 * It also automatically joins foreign keys so the corresponding entities (marked with the {@link ForeignKeyEntity} attribute) can be filled.
 *
 * @author Collin Alpert
 */
public class Query<T extends BaseEntity> {

	private final Class<T> type;
	private final IMapper<T> mapper;

	private SqlPredicate<T> whereClause;
	private SqlFunction<T, ?>[] orderBy;
	private OrderTypes orderType;
	private Integer limit;
	private int limitOffset;


	/**
	 * Constructor for creating a DQL statement for a given entity.
	 * This constructor should not be used directly, but through the DQL methods defined in the {@link com.github.collinalpert.java2db.services.BaseService}.
	 *
	 * @param type   The entity to query.
	 * @param mapper The mapper for mapping entities.
	 */
	public Query(Class<T> type, IMapper<T> mapper) {
		this.type = type;
		this.mapper = mapper;
	}

	// region Asynchronous

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #getFirst()
	 */
	public CompletableFuture<Optional<T>> getFirstAsync() {
		return CompletableFuture.supplyAsync(this::getFirst);

	}

	/**
	 * The asynchronous version of the {@link #getFirst()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #getFirst()
	 */
	public CompletableFuture<Void> getFirstAsync(Consumer<? super Optional<T>> callback) {
		return this.getFirstAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toList()
	 */
	public CompletableFuture<List<T>> toListAsync() {
		return CompletableFuture.supplyAsync(this::toList);
	}

	/**
	 * The asynchronous version of the {@link #toList()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toList()
	 */
	public CompletableFuture<Void> toListAsync(Consumer<? super List<T>> callback) {
		return this.toListAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toStream()
	 */
	public CompletableFuture<Stream<T>> toStreamAsync() {
		return CompletableFuture.supplyAsync(this::toStream);
	}

	/**
	 * The asynchronous version of the {@link #toStream()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toStream()
	 */
	public CompletableFuture<Void> toStreamAsync(Consumer<? super Stream<T>> callback) {
		return toStreamAsync().thenAcceptAsync(callback);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @return The asynchronous operation which will retrieve the data from the database.
	 * Custom handling for the {@code CompletableFuture} can be done here.
	 * @see #toArray()
	 */
	public CompletableFuture<T[]> toArrayAsync() {
		return CompletableFuture.supplyAsync(this::toArray);
	}

	/**
	 * The asynchronous version of the {@link #toArray()} method.
	 *
	 * @param callback The action to be applied to the result once it is fetched from the database.
	 * @return The asynchronous operation which will retrieve the data from the database and apply the given action to the result.
	 * @see #toArray()
	 */
	public CompletableFuture<Void> toArrayAsync(Consumer<? super T[]> callback) {
		return toArrayAsync().thenAcceptAsync(callback);
	}

	// endregion

	// region Synchronous

	/**
	 * Gets the first record of a result. This method should be used when only one record is expected, i.e. when filtering by a unique identifier such as an id.
	 *
	 * @return The first row as an entity wrapped in an {@link Optional} if there is at least one row.
	 * Otherwise {@link Optional#empty()} is returned.
	 */
	public Optional<T> getFirst() {
		try (var connection = new DBConnection()) {
			return mapper.map(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link List}
	 *
	 * @return A list of entities representing the result rows.
	 */
	public List<T> toList() {
		try (var connection = new DBConnection()) {
			return mapper.mapToList(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * Executes the query and returns the result as a {@link Stream}
	 *
	 * @return A list of entities representing the result rows.
	 */
	public Stream<T> toStream() {
		try (var connection = new DBConnection()) {
			return mapper.mapToStream(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return Stream.empty();
		}
	}

	/**
	 * Executes a new query and returns the result as an array.
	 *
	 * @return An array of entities representing the result rows.
	 */
	@SuppressWarnings("unchecked")
	public T[] toArray() {
		try (var connection = new DBConnection()) {
			return mapper.mapToArray(connection.execute(buildQuery()));
		} catch (SQLException e) {
			e.printStackTrace();
			return (T[]) Array.newInstance(type, 0);
		}
	}

	// endregion

	// region Config

	/**
	 * Sets or appends a WHERE clause for the DQL statement.
	 *
	 * @param predicate The predicate describing the WHERE clause.
	 * @return This {@link Query} object, now with an (appended) WHERE clause.
	 */
	public Query<T> where(SqlPredicate<T> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.and(predicate);
		return this;
	}

	/**
	 * Sets or appends an OR WHERE clause to the DQL statement.
	 *
	 * @param predicate The predicate describing the OR WHERE clause.
	 * @return This {@link Query} object, now with an (appended) OR WHERE clause.
	 */
	public Query<T> orWhere(SqlPredicate<T> predicate) {
		this.whereClause = this.whereClause == null ? predicate : this.whereClause.or(predicate);
		return this;
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns, if more than one is supplied.
	 *
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link Query} object, now with a coalesced ORDER BY clause.
	 */
	@SafeVarargs
	public final Query<T> orderBy(SqlFunction<T, ?>... functions) {
		return orderBy(OrderTypes.ASCENDING, functions);
	}

	/**
	 * Sets multiple ORDER BY clauses for the DQL statement. The resulting ORDER BY statement will coalesce the passed columns, if more than one is supplied.
	 *
	 * @param type      The type of ordering that should be applied.
	 * @param functions The columns to order by in a coalescing manner.
	 * @return This {@link Query} object, now with a coalesced ORDER BY clause.
	 */
	@SafeVarargs
	public final Query<T> orderBy(OrderTypes type, SqlFunction<T, ?>... functions) {
		this.orderBy = functions;
		this.orderType = type;
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer with an offset.
	 * For example, the call <code>.limit(10, 5)</code> would return the rows 6-15.
	 *
	 * @param limit  The maximum of rows to be returned.
	 * @param offset The offset of the limit.
	 * @return This {@link Query} object, now with a LIMIT with an OFFSET.
	 */
	public Query<T> limit(int limit, int offset) {
		this.limit = limit;
		this.limitOffset = offset;
		return this;
	}

	/**
	 * Limits the result of the rows returned to a maximum of the passed integer.
	 *
	 * @param limit The maximum of rows to be returned.
	 * @return This {@link Query} object, now with a LIMIT.
	 */
	public Query<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	// endregion

	/**
	 * Builds the query from the set query options.
	 *
	 * @return The DQL statement for getting data from the database.
	 */
	private String buildQuery() {
		var builder = new StringBuilder("select ");
		var fieldList = new LinkedList<String>();
		var foreignKeyList = new LinkedList<ForeignKeyReference>();
		var tableName = Utilities.getTableName(this.type);
		var columns = Utilities.getAllFields(this.type);
		for (var column : columns) {
			if (column.isForeignKey()) {
				foreignKeyList.add(new ForeignKeyReference(
						column.getReference(),
						column.getColumn().getAnnotation(ForeignKeyEntity.class).value(),
						Utilities.getTableName(column.getColumn().getType()),
						column.getAlias()));
				continue;
			}

			fieldList.add(String.format("%s as %s", column.getSQLNotation(), column.getAliasNotation()));
		}

		builder.append(String.join(", ", fieldList)).append(" from `").append(tableName).append("`");
		for (var foreignKey : foreignKeyList) {
			builder.append(" left join `").append(foreignKey.getChildTable()).append("` ").append(foreignKey.getAlias()).append(" on `").append(foreignKey.getParentClass()).append("`.`").append(foreignKey.getParentForeignKey()).append("` = `").append(foreignKey.getAlias()).append("`.`id`");
		}

		var constraints = QueryConstraints.getConstraints(this.type);
		var clauseCopy = this.whereClause;
		if (clauseCopy == null) {
			clauseCopy = constraints;
		} else {
			clauseCopy = clauseCopy.and(constraints);
		}

		builder.append(" where ").append(Lambda2Sql.toSql(clauseCopy, tableName));
		if (this.orderBy != null && this.orderBy.length > 0) {
			builder.append(" order by ");

			if (this.orderBy.length == 1) {
				builder.append(Lambda2Sql.toSql(this.orderBy[0], tableName));
			} else {
				var joiner = new StringJoiner(", ", "coalesce(", ")");
				for (SqlFunction<T, ?> orderByFunction : this.orderBy) {
					joiner.add(Lambda2Sql.toSql(orderByFunction, tableName));
				}

				builder.append(joiner.toString());
			}

			builder.append(" ").append(this.orderType.getSql());
		}

		if (this.limit != null) {
			builder.append(" limit ").append(this.limitOffset).append(", ").append(this.limit);
		}

		return builder.toString();
	}

	/**
	 * @return the query as a {@code String}
	 */
	public String getQuery() {
		return buildQuery();
	}

	@Override
	public String toString() {
		return buildQuery();
	}
}
