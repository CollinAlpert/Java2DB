package com.github.collinalpert.java2db.database;

import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Collin Alpert
 * @see <a href="https://github.com/CollinAlpert/APIs/blob/master/de/collin/DBConnection.java">GitHub</a>
 */
public class DBConnection implements Closeable {
	public static String HOST;
	public static String DATABASE;
	public static String USERNAME;
	public static String PASSWORD;
	public static DatabaseTypes DATABASE_TYPE;
	public static int PORT;
	public static boolean LOG_QUERIES = true;

	private Connection connection;
	private boolean isConnectionValid;

	public DBConnection() {
		try {
			String driver;
			String connectionString;
			if (PORT == 0) {
				switch (DATABASE_TYPE) {
					case MICROSOFT:
						PORT = 1433;
						break;
					case MYSQL:
					default:
						PORT = 3306;
						break;
				}
			}
			switch (DATABASE_TYPE) {
				case MICROSOFT:
					driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
					connectionString = "jdbc:sqlserver://" + HOST + ":" + PORT + ";databaseName=" + DATABASE;
					break;
				case MYSQL:
				default:
					driver = "com.mysql.cj.jdbc.Driver";
					connectionString = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC";
					break;
			}
			Class.forName(driver);
			DriverManager.setLoginTimeout(5);
			connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
			isConnectionValid = true;
		} catch (CJCommunicationsException | CommunicationsException e) {
			System.err.println("The connection to the database failed. Please check if the MySQL server is reachable and if you have an internet connection.");
			isConnectionValid = false;
			System.exit(1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			isConnectionValid = false;
		}
	}

	/**
	 * Checks if the connection is valid/successful.
	 *
	 * @return True if connection was successful, false if not.
	 */
	public boolean isValid() {
		return this.isConnectionValid;
	}


	/**
	 * Executes a DQL statement on the database without Java parameters.
	 *
	 * @param query The query to be executed.
	 * @return The {@link ResultSet} containing the result from the DQL statement.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public ResultSet execute(String query) throws SQLException {
		Statement statement = connection.createStatement();
		var set = statement.executeQuery(query);
		statement.closeOnCompletion();
		return set;
	}

	/**
	 * Executes a DQL statement on the database with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return The {@link ResultSet} containing the result from the DQL statement.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public ResultSet execute(String query, Object... params) throws SQLException {
		var statement = connection.prepareStatement(query);
		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}
		var set = statement.executeQuery();
		statement.closeOnCompletion();
		return set;
	}

	/**
	 * This command is used for any DDL/DML queries.
	 *
	 * @param query The query to be executed.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public void update(String query) throws SQLException {
		var statement = connection.createStatement();
		statement.executeUpdate(query);
		statement.closeOnCompletion();
	}

	/**
	 * This command is used for any DDL/DML queries with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public void update(String query, Object... params) throws SQLException {
		var statement = connection.prepareStatement(query);
		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}
		statement.executeUpdate();
		statement.closeOnCompletion();
	}

	/**
	 * Determines if a connection to the database still exists or not.
	 *
	 * @return {@code True} if a connection exists, {@code false} if not.
	 * This method will return {@code false} if an exception occurs.
	 */
	public boolean isOpen() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			System.err.println("Could not determine connection status");
			isConnectionValid = false;
			return false;
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Could not close database connection");
		} finally {
			isConnectionValid = false;
		}
	}
}
