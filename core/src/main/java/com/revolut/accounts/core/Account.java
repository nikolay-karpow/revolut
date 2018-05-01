package com.revolut.accounts.core;

import java.util.UUID;

public class Account {

    private final UUID id;
    private final int version;
    private Money balance;

    public Account(Money balance) {
        this(UUID.randomUUID(), 0, balance);
    }

    public Account(UUID id, int version, Money balance) {
        this.id = id;
        this.balance = balance;
        this.version = version;
    }

    public Money balance() {
        return balance;
    }

    public void transferTo(Account to, Money amount) {
        if (balance.isLessThan(amount)) {
            throw new AccountException("There is not enough money for transfer");
        }
        to.deposit(amount);
        withdraw(amount);
    }

    public void withdraw(Money amount) {
        balance = balance.minus(amount);
    }

    public void deposit(Money amount) {
        balance = balance.plus(amount);
    }

    public UUID id() {
        return id;
    }

    public int version() {
        return version;
    }
}
