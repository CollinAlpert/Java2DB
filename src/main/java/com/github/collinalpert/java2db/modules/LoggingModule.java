package com.github.collinalpert.java2db.modules;

import com.github.collinalpert.java2db.database.DBConnection;

/**
 * A helper module which logs messages based on a condition.
 *
 * @author Collin Alpert
 */
public class LoggingModule {

	private static final LoggingModule instance;

	static {
		instance = new LoggingModule();
	}

	private LoggingModule() {
	}

	public static LoggingModule getInstance() {
		return instance;
	}

	/**
	 * Prints messages to the query, while considering the {@link DBConnection#LOG_QUERIES} constant.
	 *
	 * @param text The message to print.
	 */
	public void log(String text) {
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
	public void logf(String text, Object... params) {
		log(String.format(text, params));
	}
}
