package com.revolut.accounts.persistence;

import com.revolut.accounts.core.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.revolut.accounts.persistence.DataSourceFactory.createDataSource;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JdbcAccountsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void canSaveNewAccount() {
        Accounts accounts = accounts();

        Account account = accounts.save(new Account(new Money(654)));

        Account loaded = accounts.find(account.id());
        assertThat(loaded.id()).isEqualTo(account.id());
        assertThat(loaded.balance()).isEqualTo(account.balance());
    }

    @Test
    public void canUpdateExistingAccount() {
        Accounts accounts = accounts();

        Account account = accounts.save(new Account(new Money(654)));
        account.deposit(new Money(100));
        Account saved = accounts.update(account);

        assertThat(saved.balance()).isEqualTo(new Money(754));
    }

    @Test
    public void updateIncreasesAccountVersion() {
        Accounts accounts = accounts();

        Account account = accounts.save(new Account(new Money(654)));
        account.deposit(new Money(100));
        int oldVersion = account.version();
        int newVersion = accounts.update(account).version();

        assertThat(newVersion).isEqualTo(oldVersion + 1);
    }

    @Test
    public void throwsIfAccountWasChangedDuringUpdate() throws InterruptedException {
        Accounts accounts = accounts();
        Account original = accounts.save(new Account(new Money(654)));

        Thread thread = new Thread(() -> {
            Account copy = accounts.find(original.id());
            copy.deposit(new Money(700));
            accounts.update(copy);
        });
        thread.start();
        thread.join();

        try {
            original.deposit(new Money(100));
            accounts.update(original);
            fail("Must not be executed");
        } catch (AccountOptimisticLockException e) {
            assertThat(e.getMessage()).isEqualTo("Account [" + original.id() + "] optimistic lock exception");
            assertThat(accounts.find(original.id()).balance()).isEqualTo(new Money(1354));
        }
    }

    @Test
    public void canFindAccountById() {
        Accounts accounts = accounts();
        Account first = accounts.save(new Account(new Money(100)));
        accounts.save(new Account(new Money(500)));

        Account found = accounts.find(first.id());

        assertThat(found.id()).isEqualTo(first.id());
        assertThat(found.balance()).isEqualTo(first.balance());
    }

    @Test
    public void findThrowsWhenAccountDoesNotExist() {
        Accounts accounts = accounts();
        UUID id = UUID.randomUUID();
        expectedException.expect(AccountNotFoundException.class);
        expectedException.expectMessage("Account [" + id + "] is not found");
        accounts.find(id);
    }

    @Test
    public void findAllReturnsAllSavedAccounts() {
        Accounts accounts = accounts();
        Account first = accounts.save(new Account(new Money(100)));
        Account second = accounts.save(new Account(new Money(500)));

        List<Account> all = accounts.findAll();
        Map<UUID, Account> idToAccount = all.stream()
                .collect(toMap(Account::id, Function.identity()));

        assertThat(idToAccount.get(first.id()).balance()).isEqualTo(first.balance());
        assertThat(idToAccount.get(second.id()).balance()).isEqualTo(second.balance());
    }


    private Accounts accounts() {
        return new JdbcAccounts(
                new ConnectionHolder(
                        createDataSource()
                )
        );
    }
}
