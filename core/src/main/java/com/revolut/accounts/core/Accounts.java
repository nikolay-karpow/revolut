package com.revolut.accounts.core;

import java.util.List;
import java.util.UUID;

public interface Accounts {
    Account find(UUID id);
    List<Account> findAll();
    Account save(Account account);
    Account update(Account account);
}
