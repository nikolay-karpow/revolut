package com.revolut.accounts.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void accountCanTellItsBalance() {
        Account account = new Account(new Money(1234));
        assertThat(account.balance()).isEqualTo(new Money(1234));
    }

    @Test
    public void accountCanTransferMoneyToAnotherAccount() {
        Account from = new Account(new Money(1270));
        Account to = new Account(new Money(70));
        from.transferTo(to, new Money(270));

        assertThat(from.balance()).isEqualTo(new Money(1000));
        assertThat(to.balance()).isEqualTo(new Money(340));
    }

    @Test
    public void throwsWhenThereIsNotEnoughMoneyForTransfer() {
        Account from = new Account(new Money(100));
        Account to = new Account(new Money(70));

        expectedException.expect(AccountException.class);
        expectedException.expectMessage("There is not enough money for transfer");
        from.transferTo(to, new Money(270));
        assertThat(from.balance()).isEqualTo(new Money(100));
        assertThat(to.balance()).isEqualTo(new Money(70));
    }
}
