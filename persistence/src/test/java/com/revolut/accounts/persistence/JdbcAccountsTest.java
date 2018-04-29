package com.revolut.accounts.persistence;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.AccountNotFoundException;
import com.revolut.accounts.core.Accounts;
import com.revolut.accounts.core.Money;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class JdbcAccountsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void canSaveNewAccount() {
        Accounts accounts = createAccounts();

        Account account = new Account(new Money(654));
        accounts.save(account);

        Account loaded = accounts.find(account.id());
        assertThat(loaded.id()).isEqualTo(account.id());
        assertThat(loaded.balance()).isEqualTo(account.balance());
    }

    @Test
    public void canFindAccountById() {
        Accounts accounts = createAccounts();
        Account first = new Account(new Money(100));
        accounts.save(first);
        Account second = new Account(new Money(500));
        accounts.save(second);

        Account found = accounts.find(first.id());

        assertThat(found.id()).isEqualTo(first.id());
    }

    @Test
    public void findThrowsWhenAccountDoesNotExist() {
        Accounts accounts = createAccounts();
        UUID id = UUID.randomUUID();
        expectedException.expect(AccountNotFoundException.class);
        expectedException.expectMessage("Account [" + id + "] is not found");
        accounts.find(id);
    }

    @Test
    public void findAllReturnsAllSavedAccounts() {
        Accounts accounts = createAccounts();
        Account first = new Account(new Money(100));
        accounts.save(first);
        Account second = new Account(new Money(500));
        accounts.save(second);

        List<Account> all = accounts.findAll();
        List<UUID> ids = all.stream().map(Account::id).collect(toList());

        assertThat(all).hasSize(2);
        assertThat(ids).contains(first.id());
        assertThat(ids).contains(second.id());
    }

    private Accounts createAccounts() {
        return new JdbcAccounts();
    }
}
