package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.AccountOptimisticLockException;
import com.revolut.accounts.core.Accounts;
import com.revolut.accounts.core.Money;
import com.revolut.accounts.persistence.ConnectionHolder;
import com.revolut.accounts.persistence.JdbcAccounts;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.revolut.accounts.persistence.DataSourceFactory.createDataSource;
import static org.assertj.core.api.Assertions.assertThat;

public class RetryingAccountServiceTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void retries_whenTransferFailsBecauseOfOptimisticLock() {
        AccountService accountService = new RetryingAccountService(
                throwingAccountService(4)
        );

        Account from = accountService.add(new Account(new Money(4500)));
        Account to = accountService.add(new Account(new Money(100)));
        accountService.transfer(from.id(), to.id(), new Money(1000));

        assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(3500));
        assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(1100));
    }

    @Test
    public void rethrowsLastException_whenTransferFailsMoreThan5Times() {
        AccountService accountService = new RetryingAccountService(
                throwingAccountService(5)
        );

        Account from = accountService.add(new Account(new Money(4500)));
        Account to = accountService.add(new Account(new Money(100)));
        expectedException.expect(AccountOptimisticLockException.class);
        expectedException.expectMessage("Account [" + from.id() + "] optimistic lock exception");
        accountService.transfer(from.id(), to.id(), new Money(1000));
    }

    @Test
    public void noChangesAreLost_whenOptimisticLockExceptionHappensDuringTransfer() throws InterruptedException {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Accounts accounts = new JdbcAccounts(connectionHolder) {
            @Override @SneakyThrows
            public Account update(Account account) {
                countDownLatch.countDown();
                countDownLatch.await();
                return super.update(account);
            }
        };
        AccountService accountService = new RetryingAccountService(
                new AccountServiceImpl(connectionHolder, accounts)
        );

        Account from = accountService.add(new Account(new Money(4500)));
        Account to = accountService.add(new Account(new Money(100)));
        Thread t1 = new Thread(() -> accountService.transfer(
                from.id(), to.id(), new Money(300)
        ));
        Thread t2 = new Thread(() -> accountService.transfer(
                from.id(), to.id(), new Money(200)
        ));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertThat(accountService.get(from.id()).balance()).isEqualTo(new Money(4000));
        assertThat(accountService.get(to.id()).balance()).isEqualTo(new Money(600));
    }

    @Test
    public void retriesToDoDeposit_ifOptimisticLockExceptionHappens() {
        AccountService accountService = new RetryingAccountService(
                throwingAccountService(3)
        );

        Account account = accountService.add(new Account(new Money(4500)));
        Account afterDeposit = accountService.deposit(account.id(), new Money(4000));

        assertThat(afterDeposit.balance()).isEqualTo(new Money(8500));
    }

    @Test
    public void retriesToDoWithdrawal_ifOptimisticLockExceptionHappens() {
        AccountService accountService = new RetryingAccountService(
                throwingAccountService(3)
        );

        Account account = accountService.add(new Account(new Money(4500)));
        Account afterWithdrawal = accountService.withdraw(account.id(), new Money(4000));

        assertThat(afterWithdrawal.balance()).isEqualTo(new Money(500));
    }

    private AccountService throwingAccountService(int failCount) {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        return new AccountServiceImpl(connectionHolder, new JdbcAccounts(connectionHolder)) {
            private final AtomicInteger transferCallsCounter = new AtomicInteger(0);
            private final AtomicInteger depositCallsCounter = new AtomicInteger(0);
            private final AtomicInteger withdrawalCallsCounter = new AtomicInteger(0);

            @Override
            public void transfer(UUID from, UUID to, Money money) {
                if (transferCallsCounter.getAndIncrement() < failCount) {
                    throw new AccountOptimisticLockException(from);
                } else {
                    super.transfer(from, to, money);
                }
            }

            @Override
            public Account deposit(UUID id, Money money) {
                if (depositCallsCounter.getAndIncrement() < failCount) {
                    throw new AccountOptimisticLockException(id);
                } else {
                    return super.deposit(id, money);
                }
            }

            @Override
            public Account withdraw(UUID id, Money money) {
                if (withdrawalCallsCounter.getAndIncrement() < failCount) {
                    throw new AccountOptimisticLockException(id);
                } else {
                    return super.withdraw(id, money);
                }
            }
        };
    }
}
