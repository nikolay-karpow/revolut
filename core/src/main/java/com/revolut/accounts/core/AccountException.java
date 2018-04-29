package com.revolut.accounts.core;

public class AccountException extends RuntimeException {
    public AccountException(Throwable cause) {
        super(cause);
    }

    public AccountException(String message) {
        super(message);
    }
}
