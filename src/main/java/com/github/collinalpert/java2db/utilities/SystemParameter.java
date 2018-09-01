package com.github.collinalpert.java2db.utilities;

import com.github.collinalpert.java2db.database.DatabaseTypes;

/**
 * @author Collin Alpert
 * <p>
 * Class for configuring settings in this library, i.e. the database connection.
 * </p>
 */
public class SystemParameter {
	public static String HOST;
	public static String DATABASE;
	public static String USERNAME;
	public static String PASSWORD;
	public static DatabaseTypes DATABASE_TYPE;
	public static int PORT;
	public static boolean LOG_QUERIES;
}
