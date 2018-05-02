package com.revolut.accounts.persistence;

import com.revolut.accounts.core.AccountException;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TransactionTest {

    @Test
    public void returnsResultWhenCompletesNormally() {
        Integer actual = new Transaction<>(connectionHolder(), () -> 456).execute();
        assertThat(actual.intValue()).isEqualTo(456);
    }

    @Test
    public void closesConnectionWhenCompletesNormally() throws SQLException {
        ConnectionHolder connectionHolder = connectionHolder();
        Connection connection = connectionHolder.getConnection();
        new Transaction<>(connectionHolder, () -> 456).execute();
        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    public void closesConnectionWhenRollsBack() throws SQLException {
        ConnectionHolder connectionHolder = connectionHolder();
        Connection connection = connectionHolder.getConnection();
        AccountException exception = new AccountException("");
        Transaction transaction = new Transaction<>(
                connectionHolder,
                () -> {
                    throw exception;
                }
        );
        try {
            transaction.execute();
            fail("Must not be executed");
        } catch (AccountException e) {
            assertThat(e).isSameAs(exception);
            assertThat(connection.isClosed()).isTrue();
        }
    }

    private ConnectionHolder connectionHolder() {
        return new ConnectionHolder(DataSourceFactory.createDataSource());
    }
}
