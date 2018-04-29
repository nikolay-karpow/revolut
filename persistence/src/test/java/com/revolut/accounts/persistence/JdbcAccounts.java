package com.revolut.accounts.persistence;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.AccountNotFoundException;
import com.revolut.accounts.core.Accounts;

import java.util.*;

public class JdbcAccounts implements Accounts {
    private Map<UUID, Account> accounts = new HashMap<>();

    @Override
    public Account find(UUID id) {
        return Optional.ofNullable(accounts.get(id))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public void save(Account account) {
        accounts.put(account.id(), account);
    }
}
