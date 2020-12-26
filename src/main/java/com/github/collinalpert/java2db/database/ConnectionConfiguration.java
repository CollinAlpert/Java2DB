package com.github.collinalpert.java2db.database;

/**
 * A container for database access. Contains all necessary information for this library to access a database.
 *
 * @author Collin Alpert
 */
public class ConnectionConfiguration {

	/**
	 * Specifies the hostname/ip address of the database.
	 */
	private final String host;

	/**
	 * Specifies the name of the database to connect to.
	 */
	private final String database;

	/**
	 * Specifies the username to log in on the database with.
	 */
	private final String username;

	/**
	 * Specifies the password to log in on the database with.
	 */
	private final String password;

	/**
	 * Specifies the port to connect to the database on.
	 * This property is optional. If not specified, it will be set to 3306, the default port of MySQL.
	 */
	private final int port;

	/**
	 * Specifies the login timeout to the database in seconds. Default is 5 seconds.
	 */
	private final int timeout;

	public ConnectionConfiguration(String host, String database, String username, String password) {
		this(host, database, username, password, 3306, 5);
	}

	public ConnectionConfiguration(String host, String database, String username, String password, int port, int timeout) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
		this.port = port;
		this.timeout = timeout;
	}

	public String getHost() {
		return host;
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}


}
