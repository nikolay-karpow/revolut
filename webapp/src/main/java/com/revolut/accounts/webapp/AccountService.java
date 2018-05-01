package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Accounts;
import com.revolut.accounts.core.Money;
import com.revolut.accounts.persistence.ConnectionHolder;
import com.revolut.accounts.persistence.Transaction;

import java.util.List;
import java.util.UUID;

public class AccountService {
    private final ConnectionHolder connectionHolder;
    private final Accounts accounts;

    public AccountService(ConnectionHolder connectionHolder, Accounts accounts) {
        this.connectionHolder = connectionHolder;
        this.accounts = accounts;
    }

    public Account add(Account account) {
        return new Transaction<>(
                connectionHolder,
                () -> accounts.save(account)
        ).execute();
    }

    public List<Account> getAll() {
        return new Transaction<>(
                connectionHolder,
                accounts::findAll
        ).execute();
    }

    public Account get(UUID id) {
        return new Transaction<>(
                connectionHolder,
                () -> accounts.find(id)
        ).execute();
    }

    public void transfer(Account from, Account to, Money money) {
        new Transaction<Void>(connectionHolder, () -> {
            from.transferTo(to, money);
            accounts.update(from);
            accounts.update(to);
            return null;
        }).execute();
    }

}
