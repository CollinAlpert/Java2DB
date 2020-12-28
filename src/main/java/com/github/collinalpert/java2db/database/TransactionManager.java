package com.github.collinalpert.java2db.database;

import com.github.collinalpert.java2db.utilities.ThrowableConsumer;
import com.github.collinalpert.java2db.utilities.ThrowableFunction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Allows to execute code within a database transaction.
 *
 * This class maintains the notion of a 'current' database connection, which is bound to the currently
 * executing thread via a {@link ThreadLocal}. The first call in the stack to execute code within a
 * transaction opens a new connection and binds it to this thread local variable. Subsequent calls within
 * the same thread which wish to participate within the transaction will then re-use this connection.
 *
 * @author Tyler Sharpe
 */
public class TransactionManager {

    private static final ThreadLocal<DBConnection> CURRENT_THREAD_CONNECTION = new ThreadLocal<>();

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Run some code inside of a database transaction, creating one if it does not already exist.
     */
    public void transact(ThrowableConsumer<DBConnection, SQLException> action) throws SQLException {
        transactAndReturn(connection -> {
            action.consume(connection);
            return null;
        });
    }

    /**
     * Run some code inside of a database transaction, creating one if it does not already exist, and then return a value.
     * @param action Action to run
     * @param <T> Type returned from the action lambda
     * @return
     * @throws SQLException
     */
    public <T> T transactAndReturn(ThrowableFunction<DBConnection, T, SQLException> action) throws SQLException {
        if (CURRENT_THREAD_CONNECTION.get() != null) {
            return action.run(CURRENT_THREAD_CONNECTION.get());
        }

        try (Connection rawConnection = dataSource.getConnection()) {
            rawConnection.setAutoCommit(false);
            DBConnection dbConnection = new DBConnection(rawConnection);
            CURRENT_THREAD_CONNECTION.set(dbConnection);

            try {
                T result = action.run(dbConnection);
                rawConnection.commit();
                return result;
            } catch (Exception exception) {
                // rollback transaction on error
                try {
                    rawConnection.rollback();
                } catch (Exception rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw new SQLException(exception);
            }
        } finally {
            CURRENT_THREAD_CONNECTION.remove();
        }
    }

}
