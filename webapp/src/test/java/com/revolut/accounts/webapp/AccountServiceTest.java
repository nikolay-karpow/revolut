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

        accountService.transfer(from.id(), to.id(), new Money(200));

        assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(800));
        assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(300));
    }

    @Test
    public void canDepositMoneyToAccount() {
        AccountService accountService = accountService();
        Account account = accountService.add(new Account(new Money(1000)));

        Account afterDeposit = accountService.deposit(account.id(), new Money(500));

        assertThat(afterDeposit.id()).isEqualTo(account.id());
        assertThat(accountService.get(account.id()).balance()).isEqualTo(new Money(1500));
    }

    @Test
    public void canWithdrawMoneyFromAccount() {
        AccountService accountService = accountService();
        Account account = accountService.add(new Account(new Money(1000)));

        Account afterWithdrawal = accountService.withdraw(account.id(), new Money(600));

        assertThat(afterWithdrawal.id()).isEqualTo(account.id());
        assertThat(accountService.get(account.id()).balance()).isEqualTo(new Money(400));
    }

    private AccountService accountService() {
        return new RetryingAccountService(new AccountServiceImpl(
                connectionHolder(),
                new JdbcAccounts(connectionHolder())
        ));
    }

    private ConnectionHolder connectionHolder() {
        return new ConnectionHolder(createDataSource());
    }
}
