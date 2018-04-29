package com.revolut.accounts.core;

import java.util.UUID;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(UUID accountId) {
        super("Account [" + accountId + "] is not found");
    }
}
