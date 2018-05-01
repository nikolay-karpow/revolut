package com.revolut.accounts.persistence;

import com.revolut.accounts.core.AccountException;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction<T> {
    private final ConnectionHolder connectionHolder;
    private final Transactional<T> transactional;

    public Transaction(ConnectionHolder connectionHolder, Transactional<T> transactional) {
        this.connectionHolder = connectionHolder;
        this.transactional = transactional;
    }

    public T execute() {
        Connection connection = connectionHolder.getConnection();
        try {
            try {
                connection.setAutoCommit(false);
                T result = transactional.execute();
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connectionHolder.closeConnection(connection);
            }
        } catch (SQLException e) {
            throw new AccountException(e);
        }
    }
}
