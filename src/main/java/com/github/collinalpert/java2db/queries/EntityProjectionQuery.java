package com.github.collinalpert.java2db.queries;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.entities.BaseEntity;
import com.github.collinalpert.java2db.modules.ArrayModule;
import com.github.collinalpert.lambda2sql.Lambda2Sql;
import com.github.collinalpert.lambda2sql.functions.SqlFunction;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A query which represents a projection from an {@link EntityQuery} to a single column on the database.
 *
 * @author Collin Alpert
 */
public class EntityProjectionQuery<E extends BaseEntity, R> implements Queryable<R> {

	private final Class<R> returnType;
	private final SqlFunction<E, R> projection;
	private final EntityQuery<E> originalQuery;

	public EntityProjectionQuery(Class<R> returnType, SqlFunction<E, R> projection, EntityQuery<E> originalQuery) {
		this.returnType = returnType;
		this.projection = projection;
		this.originalQuery = originalQuery;
	}

	@Override
	public Optional<R> getFirst() {
		try (var connection = new DBConnection();
			 var result = connection.execute(getQuery())) {

			if (result.next()) {
				return Optional.ofNullable(result.getObject(1, this.returnType));
			}

			return Optional.empty();
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	@Override
	public List<R> toList() {
		var list = new ArrayList<R>();
		return resultHandling(list, List::add, Collections.emptyList(), Function.identity());
	}

	@Override
	public Stream<R> toStream() {
		var streamBuilder = Stream.<R>builder();
		return resultHandling(streamBuilder, Stream.Builder::accept, Stream.empty(), Stream.Builder::build);
	}

	@Override
	public R[] toArray() {
		var arrayModule = new ArrayModule<>(this.returnType, 20);
		@SuppressWarnings("unchecked")
		var defaultValue = (R[]) Array.newInstance(this.returnType, 0);
		return resultHandling(arrayModule, ArrayModule::addElement, defaultValue, ArrayModule::getArray);
	}

	private <T, D> T resultHandling(D dataType, BiConsumer<D, R> valueConsumer, T defaultValue, Function<D, T> valueMapping) {
		try (var connection = new DBConnection();
			 var result = connection.execute(getQuery())) {
			while (result.next()) {
				valueConsumer.accept(dataType, result.getObject(1, this.returnType));
			}

			return valueMapping.apply(dataType);
		} catch (SQLException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	@Override
	public String getQuery() {
		var builder = new StringBuilder("select ");

		var tableName = originalQuery.getTableName();
		var columnName = Lambda2Sql.toSql(projection, tableName);
		builder.append(columnName).append(" from `").append(tableName).append("`");

		builder.append(originalQuery.generateQueryClauses(tableName));

		return builder.toString();
	}
}
