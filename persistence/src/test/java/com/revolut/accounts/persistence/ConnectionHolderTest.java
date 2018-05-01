package com.revolut.accounts.persistence;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static com.revolut.accounts.persistence.DataSourceFactory.createDataSource;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionHolderTest {
    @Test
    public void givesConnectionFromDataSource_whenThereIsNoExistingOne() {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        Connection connection = connectionHolder.getConnection();
        assertThat(connection).isNotNull();
    }

    @Test
    public void givesTheSameConnectionForTheSameThread() {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        Connection c1 = connectionHolder.getConnection();
        Connection c2 = connectionHolder.getConnection();
        assertThat(c1).isSameAs(c2);
    }

    @Test
    public void givesDifferentConnectionsForDifferentThreads() throws InterruptedException {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        AtomicReference<Connection> c1 = new AtomicReference<>();
        AtomicReference<Connection> c2 = new AtomicReference<>();

        Thread t1 = new Thread(() -> c1.set(connectionHolder.getConnection()));
        Thread t2 = new Thread(() -> c2.set(connectionHolder.getConnection()));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertThat(c1.get()).isNotSameAs(c2.get());
    }

    @Test
    public void canCloseConnection() throws SQLException {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        Connection connection = connectionHolder.getConnection();
        connectionHolder.closeConnection(connection);
        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    public void theSameThreadGetsNewConnectionAfterOldOneIsClosed() throws SQLException {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        Connection c1 = connectionHolder.getConnection();
        connectionHolder.closeConnection(c1);
        Connection c2 = connectionHolder.getConnection();
        assertThat(c2).isNotSameAs(c1);
        assertThat(c2.isClosed()).isFalse();
    }
}
