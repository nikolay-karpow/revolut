package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.AccountException;
import com.revolut.accounts.core.Accounts;
import com.revolut.accounts.core.Money;
import com.revolut.accounts.persistence.ConnectionHolder;
import com.revolut.accounts.persistence.JdbcAccounts;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.revolut.accounts.persistence.DataSourceFactory.createDataSource;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AccountServiceTest {

    @Test
    public void canAddAccount() {
        AccountService accountService = accountService();
        Account account = accountService.add(new Account(new Money(0)));
        assertThat(account.balance()).isEqualTo(new Money(0));
    }

    @Test
    public void canGiveListOfAllAccounts() {
        AccountService accountService = accountService();
        Account first = accountService.add(new Account(new Money(0)));
        Account second = accountService.add(new Account(new Money(0)));

        List<UUID> ids = accountService.getAll().stream()
                .map(Account::id).collect(toList());

        assertThat(ids.contains(first.id()));
        assertThat(ids.contains(second.id()));
    }

    @Test
    public void canGiveAccountById() {
        AccountService accountService = accountService();
        Account account = accountService.add(new Account(new Money(200)));

        Account found = accountService.get(account.id());

        assertThat(found.balance()).isEqualTo(account.balance());
    }

    @Test
    public void canTransferMoneyFromOneAccountToAnother() {
        AccountService accountService = accountService();
        Account from = accountService.add(new Account(new Money(1000)));
        Account to = accountService.add(new Account(new Money(100)));

        accountService.transfer(from, to, new Money(200));

        assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(800));
        assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(300));
    }

    @Test
    public void transferDoesNotMakeAnyEffect_ifToAccountIsChanged() throws InterruptedException {
        ConnectionHolder connectionHolder = connectionHolder();
        Accounts accounts = new JdbcAccounts(connectionHolder);
        AccountService accountService = new AccountService(connectionHolder, accounts);
        Account from = accountService.add(new Account(new Money(1000)));
        Account to = accountService.add(new Account(new Money(100)));

        Thread thread = new Thread(() -> {
            Account toChanged = accountService.get(to.id());
            toChanged.deposit(new Money(400));
            accounts.update(toChanged);
        });
        thread.start();
        thread.join();

        try {
            accountService.transfer(from, to, new Money(200));
            fail("Must not be executed");
        } catch (AccountException e) {
            assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(1000));
            assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(500));
        }
    }

    @Test
    public void transferDoesNotMakeAnyEffect_ifFromAccountIsChanged() throws InterruptedException {
        ConnectionHolder connectionHolder = connectionHolder();
        Accounts accounts = new JdbcAccounts(connectionHolder);
        AccountService accountService = new AccountService(connectionHolder, accounts);
        Account from = accountService.add(new Account(new Money(1000)));
        Account to = accountService.add(new Account(new Money(100)));

        Thread thread = new Thread(() -> {
            Account fromChanged = accountService.get(from.id());
            fromChanged.deposit(new Money(400));
            accounts.update(fromChanged);
        });
        thread.start();
        thread.join();

        try {
            accountService.transfer(from, to, new Money(200));
            fail("Must not be executed");
        } catch (AccountException e) {
            assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(1400));
            assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(100));
        }
    }

    private AccountService accountService() {
        return new AccountService(
                connectionHolder(),
                new JdbcAccounts(connectionHolder())
        );
    }

    private ConnectionHolder connectionHolder() {
        return new ConnectionHolder(createDataSource());
    }
}
