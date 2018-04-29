package com.revolut.accounts.persistence;

import com.revolut.accounts.core.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JdbcAccounts implements Accounts {
    private static final String SAVE_NEW_ACCOUNT = "INSERT INTO ACCOUNT (ID, BALANCE) VALUES (?, ?)";
    private static final String FIND_BY_ID = "SELECT * FROM ACCOUNT WHERE ID=?";
    private static final String SELECT_ALL = "SELECT * FROM ACCOUNT";
    private final DataSource dataSource;

    public JdbcAccounts(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Account find(UUID id) {
        return execute(FIND_BY_ID, (statement -> {
            statement.setObject(1, id);
            statement.execute();
            try (ResultSet resultSet = statement.getResultSet()) {
                if (resultSet.first()) {
                    return createAccountFromResultSet(resultSet);
                } else {
                    throw new AccountNotFoundException(id);
                }
            }
        }));
    }

    @Override
    public List<Account> findAll() {
        return execute(SELECT_ALL, statement -> {
            statement.execute();
            List<Account> results = new ArrayList<>();
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    results.add(createAccountFromResultSet(resultSet));
                }
            }
            return results;
        });
    }

    @Override
    public void save(Account account) {
        execute(SAVE_NEW_ACCOUNT, statement -> {
            statement.setObject(1, account.id());
            statement.setObject(2, account.balance().asLong());
            statement.execute();
            return null;
        });
    }

    private Account createAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return new Account(
                (UUID) resultSet.getObject("ID"),
                new Money(resultSet.getLong("BALANCE"))
        );
    }

    @FunctionalInterface
    private interface StatementCallable<T> {
        T call(PreparedStatement statement) throws SQLException;
    }

    private <T> T execute(String sqlQueryTemplate, StatementCallable<T> callable) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQueryTemplate)) {
            return callable.call(statement);
        } catch (SQLException e) {
            throw new AccountException(e);
        }
    }
}
