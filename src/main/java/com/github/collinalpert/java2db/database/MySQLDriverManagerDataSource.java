package com.github.collinalpert.java2db.database;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * {@link DataSource} implementation hard-coded to support only MySQL databases.
 * Obtains connections directly from {@link java.sql.DriverManager}
 *
 * @author Tyler Sharpe
 */
public class MySQLDriverManagerDataSource implements DataSource {

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final ConnectionConfiguration configuration;

	public MySQLDriverManagerDataSource(ConnectionConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
			String.format("jdbc:mysql://%s:%d/%s?rewriteBatchedStatements=true", configuration.getHost(), configuration.getPort(), configuration.getDatabase()),
			configuration.getUsername(),
			configuration.getPassword()
		);
	}

	@Override
	public Connection getConnection(String username, String password) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PrintWriter getLogWriter() {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoginTimeout(int seconds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLoginTimeout() {
		return configuration.getTimeout();
	}

	@Override
	public Logger getParentLogger() {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> interfaceType) {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> interfaceType) {
		return false;
	}
}
