package com.revolut.accounts.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AccountTest {

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

        try {
            from.transferTo(to, new Money(270));
            fail("Must not be executed");
        } catch (AccountException e) {
            assertThat(from.balance()).isEqualTo(new Money(100));
            assertThat(to.balance()).isEqualTo(new Money(70));
        }
    }

    @Test
    public void throwsWhenThereIsNotEnoughMoneyForWithdrawal() {
        Account account = new Account(new Money(100));
        try {
            account.withdraw(new Money(101));
            fail("Must not be executed");
        } catch (AccountException e) {
            assertThat(account.balance()).isEqualTo(new Money(100));
        }
    }

    @Test
    public void throwsWhenTryingToWithdrawNegativeAmount() {
        Account account = new Account(new Money(100));
        try {
            account.withdraw(new Money(-10));
            fail("Must not be executed");
        } catch (AccountOperationIllegalArgumentException e) {
            assertThat(account.balance()).isEqualTo(new Money(100));
            assertThat(e.getMessage()).isEqualTo("Can't withdraw negative amount");
        }
    }
    @Test
    public void throwsWhenTryingToDepositNegativeAmount() {
        Account account = new Account(new Money(100));
        try {
            account.deposit(new Money(-10));
            fail("Must not be executed");
        } catch (AccountOperationIllegalArgumentException e) {
            assertThat(account.balance()).isEqualTo(new Money(100));
            assertThat(e.getMessage()).isEqualTo("Can't deposit negative amount");
        }
    }
}
