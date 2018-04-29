package com.revolut.accounts.core;

import java.util.UUID;

public class AccountOptimisticLockException extends AccountException {
    public AccountOptimisticLockException(UUID id) {
        super("Account [" + id + "] optimistic lock exception");
    }
}
