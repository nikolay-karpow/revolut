package com.revolut.accounts.persistence;

import com.revolut.accounts.core.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcAccounts implements Accounts {
    private static final String SAVE_NEW_ACCOUNT =
            "INSERT INTO ACCOUNT (ID, VERSION, BALANCE) VALUES (?, ?, ?)";
    private static final String UPDATE_ACCOUNT =
            "UPDATE ACCOUNT SET BALANCE=?, VERSION=? WHERE ID=? AND VERSION=?";
    private static final String FIND_BY_ID =
            "SELECT * FROM ACCOUNT WHERE ID=?";
    private static final String SELECT_ALL =
            "SELECT * FROM ACCOUNT";
    private final ConnectionHolder connectionHolder;

    public JdbcAccounts(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
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
    public Account save(Account account) {
        return execute(SAVE_NEW_ACCOUNT, statement -> {
            statement.setObject(1, account.id());
            statement.setInt(2, account.version());
            statement.setObject(3, account.balance().asCents());
            statement.execute();
            return account;
        });
    }

    @Override
    public Account update(Account account) {
        return execute(UPDATE_ACCOUNT, statement -> {
            int oldVersion = account.version();
            int newVersion = oldVersion + 1;
            statement.setLong(1, account.balance().asCents());
            statement.setInt(2, newVersion);
            statement.setObject(3, account.id());
            statement.setInt(4, oldVersion);
            int updatedCount = statement.executeUpdate();
            if (updatedCount == 1) {
                return new Account(account.id(), newVersion, account.balance());
            } else {
                throw new AccountOptimisticLockException(account.id());
            }
        });
    }

    private Account createAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return new Account(
                (UUID) resultSet.getObject("ID"),
                resultSet.getInt("VERSION"),
                new Money(resultSet.getLong("BALANCE"))
        );
    }

    @FunctionalInterface
    private interface StatementCallable<T> {
        T call(PreparedStatement statement) throws SQLException;
    }

    private <T> T execute(String sqlQueryTemplate, StatementCallable<T> callable) {
        Connection connection = connectionHolder.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sqlQueryTemplate)) {
            return callable.call(statement);
        } catch (SQLException e) {
            throw new AccountException(e);
        }
    }
}
