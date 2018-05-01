package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Money;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account add(Account account);

    List<Account> getAll();

    Account get(UUID id);

    void transfer(UUID from, UUID to, Money money);

    Account deposit(UUID id, Money money);

    Account withdraw(UUID id, Money money);
}
