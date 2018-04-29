package com.revolute.accounts.core;

public class Account {
    private Money balance;

    public Account(Money balance) {
        this.balance = balance;
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

    private void withdraw(Money amount) {
        balance = balance.minus(amount);
    }

    private void deposit(Money amount) {
        balance = balance.plus(amount);
    }
}
