package com.revolut.accounts.persistence;

import com.revolut.accounts.core.AccountException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolder {
    private final DataSource dataSource;
    private final ThreadLocal<Connection> holder;

    public ConnectionHolder(DataSource dataSource) {
        this.dataSource = dataSource;
        this.holder = new ThreadLocal<>();
    }

    public Connection getConnection() {
        if (holder.get() == null) {
            try {
                holder.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new AccountException(e);
            }
        }
        return holder.get();
    }

    public void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new AccountException(e);
        } finally {
            holder.set(null);
        }
    }
}
