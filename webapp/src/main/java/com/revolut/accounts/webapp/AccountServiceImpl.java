package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Accounts;
import com.revolut.accounts.core.Money;
import com.revolut.accounts.persistence.ConnectionHolder;
import com.revolut.accounts.persistence.Transaction;

import java.util.List;
import java.util.UUID;

public class AccountServiceImpl implements AccountService {
    private final ConnectionHolder connectionHolder;
    private final Accounts accounts;

    public AccountServiceImpl(ConnectionHolder connectionHolder, Accounts accounts) {
        this.connectionHolder = connectionHolder;
        this.accounts = accounts;
    }

    @Override
    public Account add(Account account) {
        return new Transaction<>(
                connectionHolder, () -> accounts.save(account)
        ).execute();
    }

    @Override
    public List<Account> getAll() {
        return new Transaction<>(connectionHolder, accounts::findAll)
                .execute();
    }

    @Override
    public Account get(UUID id) {
        return new Transaction<>(
                connectionHolder, () -> accounts.find(id)
        ).execute();
    }

    @Override
    public void transfer(UUID from, UUID to, Money money) {
        new Transaction<Void>(connectionHolder, () -> {
            Account accountFrom = accounts.find(from);
            Account accountTo = accounts.find(to);
            accountFrom.transferTo(accountTo, money);
            accounts.update(accountFrom);
            accounts.update(accountTo);
            return null;
        }).execute();
    }

    @Override
    public Account deposit(UUID id, Money money) {
        return new Transaction<>(connectionHolder, () -> {
            Account account = accounts.find(id);
            account.deposit(money);
            accounts.update(account);
            return account;
        }).execute();
    }

    @Override
    public Account withdraw(UUID id, Money money) {
        return new Transaction<>(connectionHolder, () -> {
            Account account = accounts.find(id);
            account.withdraw(money);
            accounts.update(account);
            return account;
        }).execute();
    }

}
