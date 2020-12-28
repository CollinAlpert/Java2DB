package com.github.collinalpert.java2db.sandbox;

import com.github.collinalpert.java2db.database.TransactionManager;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TestMain {

	public static void main(String[] args) throws Exception {
		Class.forName("org.h2.Driver");

		DataSource h2DataSource = createH2DataSource();
		createSchema(h2DataSource);

		TransactionManager transactionManager = new TransactionManager(h2DataSource);

		UserService userService = new UserService(transactionManager);
		OrderService orderService = new OrderService(transactionManager);

		try {
			transactionManager.transact(connection -> {
				orderService.create(new Order("Lamp", 1));
				orderService.create(new Order("Desk", 1));
				userService.create(new User("John"));

				throw new RuntimeException("this should rollback the transaction");
			});
		} catch (Exception e) {
			e.printStackTrace(); // expected
		}

		transactionManager.transact(connection -> {
			var allUsers = userService.getAll();
			assert allUsers.isEmpty();
		});
	}

	private static void createSchema(DataSource dataSource) throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			var statement = conn.createStatement();

			statement.execute(
				"CREATE TABLE `order` (" +
				"	id INT AUTO_INCREMENT PRIMARY KEY," +
				"	product VARCHAR(100)," +
				"	amount SMALLINT" +
				")"
			);

			statement.execute(
				"CREATE TABLE user (" +
				"	id INT AUTO_INCREMENT PRIMARY KEY," +
				"	name VARCHAR(50)" +
				")"
			);
		}
	}

	private static DataSource createH2DataSource() {
		return new DataSource() {
			@Override
			public Connection getConnection() throws SQLException {
				return DriverManager.getConnection("jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1");
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				return null;
			}

			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}

			@Override
			public void setLogWriter(PrintWriter out) {

			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {

			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}
		};
	}
}
