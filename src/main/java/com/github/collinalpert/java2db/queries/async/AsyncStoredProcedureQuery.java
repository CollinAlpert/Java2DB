package com.github.collinalpert.java2db.queries.async;

import com.github.collinalpert.java2db.database.DBConnection;
import com.github.collinalpert.java2db.queries.StoredProcedureQuery;

/**
 * @author Collin Alpert
 */
public class AsyncStoredProcedureQuery<T> extends StoredProcedureQuery<T> implements AsyncQueryable<T> {

	public AsyncStoredProcedureQuery(Class<T> underlyingClass, DBConnection connection, String procedureName, Object[] arguments) {
		super(underlyingClass, connection, procedureName, arguments);
	}
}
