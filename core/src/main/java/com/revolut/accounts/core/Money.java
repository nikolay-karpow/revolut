package com.revolut.accounts.core;

import lombok.*;

@EqualsAndHashCode @ToString
public class Money {
    private final long cents;

    public Money(long cents) {
        this.cents = cents;
    }

    public Money plus(Money amount) {
        return new Money(cents + amount.cents);
    }

    public Money minus(Money amount) {
        return new Money(cents - amount.cents);
    }

    public boolean isLessThan(Money other) {
        return cents < other.cents;
    }

    public long asCents() {
        return cents;
    }

    public boolean isNegative() {
        return cents < 0;
    }
}
