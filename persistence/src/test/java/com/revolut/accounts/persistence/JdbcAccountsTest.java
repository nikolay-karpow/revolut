package com.revolut.accounts.persistence;

import com.revolut.accounts.core.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JdbcAccountsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void canSaveNewAccount() {
        Accounts accounts = createAccounts();

        Account account = accounts.save(new Account(new Money(654)));

        Account loaded = accounts.find(account.id());
        assertThat(loaded.id()).isEqualTo(account.id());
        assertThat(loaded.balance()).isEqualTo(account.balance());
    }

    @Test
    public void canUpdateExistingAccount() {
        Accounts accounts = createAccounts();

        Account account = accounts.save(new Account(new Money(654)));
        account.deposit(new Money(100));
        Account saved = accounts.update(account);

        assertThat(saved.balance()).isEqualTo(new Money(754));
    }

    @Test
    public void updateIncreasesAccountVersion() {
        Accounts accounts = createAccounts();

        Account account = accounts.save(new Account(new Money(654)));
        account.deposit(new Money(100));
        int oldVersion = account.version();
        int newVersion = accounts.update(account).version();

        assertThat(newVersion).isEqualTo(oldVersion + 1);
    }

    @Test
    public void throwsIfAccountWasChangedDuringUpdate() {
        Accounts accounts = createAccounts();

        Account original = accounts.save(new Account(new Money(654)));
        Account copy = accounts.find(original.id());
        copy.deposit(new Money(700));
        accounts.update(copy);
        original.deposit(new Money(100));

        expectedException.expect(AccountOptimisticLockException.class);
        expectedException.expectMessage("Account [" + original.id() + "] optimistic lock exception");
        accounts.update(original);
        assertThat(accounts.find(original.id()).balance()).isEqualTo(new Money(1354));
    }

    @Test
    public void canFindAccountById() {
        Accounts accounts = createAccounts();
        Account first = accounts.save(new Account(new Money(100)));
        accounts.save(new Account(new Money(500)));

        Account found = accounts.find(first.id());

        assertThat(found.id()).isEqualTo(first.id());
        assertThat(found.balance()).isEqualTo(first.balance());
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
        Account first = accounts.save(new Account(new Money(100)));
        Account second = accounts.save(new Account(new Money(500)));

        List<Account> all = accounts.findAll();
        Map<UUID, Account> idToAccount = all.stream()
                .collect(toMap(Account::id, Function.identity()));

        assertThat(idToAccount.get(first.id()).balance()).isEqualTo(first.balance());
        assertThat(idToAccount.get(second.id()).balance()).isEqualTo(second.balance());
    }

    private Accounts createAccounts() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testDb;" +
                "DB_CLOSE_DELAY=-1;" +
                "INIT=RUNSCRIPT FROM 'classpath:schema.sql'\\;");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return new JdbcAccounts(dataSource);
    }
}
