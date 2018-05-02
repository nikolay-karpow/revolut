package com.revolut.accounts.core;

import java.util.UUID;

public class Account {

    private final UUID id;
    private final int version;
    private Money balance;

    public Account() {
        this(new Money(0));
    }

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
        withdraw(amount);
        to.deposit(amount);
    }

    public void withdraw(Money amount) {
        if (balance.isLessThan(amount)) {
            throw new AccountException("There is not enough money");
        }
        if (amount.isNegative()) {
            throw new AccountOperationIllegalArgumentException(
                    "Can't withdraw negative amount"
            );
        }
        balance = balance.minus(amount);
    }

    public void deposit(Money amount) {
        if (amount.isNegative()) {
            throw new AccountOperationIllegalArgumentException(
                    "Can't deposit negative amount"
            );
        }
        balance = balance.plus(amount);
    }

    public UUID id() {
        return id;
    }

    public int version() {
        return version;
    }
}
