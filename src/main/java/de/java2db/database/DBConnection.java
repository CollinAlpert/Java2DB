package de.java2db.database;

import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import de.java2db.utilities.SystemParameter;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Collin Alpert
 * @see <a href="https://github.com/CollinAlpert/APIs/blob/master/de/collin/DBConnection.java">GitHub</a>
 */
public class DBConnection implements AutoCloseable {
	private Connection connection = null;
	private boolean isConnectionValid;

	public DBConnection() {
		try {
			String driver;
			String connectionString;
			if (SystemParameter.PORT == 0) {
				switch (SystemParameter.DATABASE_TYPE) {
					default:
					case MYSQL:
						SystemParameter.PORT = 3306;
						break;
					case MICROSOFT:
						SystemParameter.PORT = 1433;
						break;
					case ORACLE:
						SystemParameter.PORT = 1521;
						break;
				}
			}
			switch (SystemParameter.DATABASE_TYPE) {
				default:
				case MYSQL:
					driver = "com.mysql.cj.jdbc.Driver";
					connectionString = "jdbc:mysql://" + SystemParameter.HOST + ":" + SystemParameter.PORT + "/" + SystemParameter.DATABASE + "?serverTimezone=UTC";
					break;
				case ORACLE:
					driver = "oracle.jdbc.driver.OracleDriver";
					connectionString = "jdbc:oracle:thin:@" + SystemParameter.HOST + ":" + SystemParameter.PORT + ":orcl";
					OracleDataSource source = new OracleDataSource();
					source.setDatabaseName(SystemParameter.DATABASE);
					source.setURL(connectionString);
					source.setUser(SystemParameter.USERNAME);
					source.setPassword(SystemParameter.PASSWORD);
					connection = source.getConnection();
					break;
				case MICROSOFT:
					driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
					connectionString = "jdbc:sqlserver://" + SystemParameter.HOST + ":" + SystemParameter.PORT + ";databaseName=" + SystemParameter.DATABASE;
					break;
			}
			Class.forName(driver);
			DriverManager.setLoginTimeout(5);
			if (connection == null) {
				connection = DriverManager.getConnection(connectionString, SystemParameter.USERNAME, SystemParameter.PASSWORD);
			}
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
	 * Executes an SELECT SQL statement on the database without Java parameters.
	 *
	 * @param query The query to be executed.
	 * @return The <code>ResultSet</code> containing the result from the SELECT query.
	 */
	public ResultSet execute(String query) {
		try {
			Statement statement = connection.createStatement();
			var set = statement.executeQuery(query);
			statement.closeOnCompletion();
			return set;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Executes an SELECT SQL statement on the database with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return The <code>ResultSet</code> containing the result from the SELECT query.
	 */
	public ResultSet execute(String query, Object... params) {
		try {
			var statement = connection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			var set = statement.executeQuery();
			statement.closeOnCompletion();
			return set;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This command is used for any queries that are supposed to update the database, such as UPDATE, DELETE, TRUNCATE etc.
	 *
	 * @param query The query to be executed.
	 * @return <code>true</code> if the update was successful, <code>false</code> if not.
	 */
	public boolean update(String query) {
		try {
			var statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.closeOnCompletion();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This command is used for any queries that are supposed to update the database, such as UPDATE, DELETE, TRUNCATE etc.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return <code>true</code> if the update was successful, <code>false</code> if not.
	 */
	public boolean update(String query, Object... params) {
		try {
			var statement = connection.prepareStatement(query);
			for (int i = 0; i < params.length; i++) {
				statement.setObject(i + 1, params[i]);
			}
			statement.executeUpdate();
			statement.closeOnCompletion();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Determines if a connection to the database still exists or not.
	 *
	 * @return <code>True</code> if a connection exists, <code>false</code> if not.
	 * This method will return <code>false</code> if an exception occurs.
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
