package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.AccountOptimisticLockException;
import com.revolut.accounts.core.Money;

import java.util.List;
import java.util.UUID;

public class RetryingAccountService implements AccountService {
    private static final int MAX_RETRY_COUNT = 5;
    private final AccountService origin;

    public RetryingAccountService(AccountService origin) {
        this.origin = origin;
    }

    @Override
    public Account add(Account account) {
        return origin.add(account);
    }

    @Override
    public List<Account> getAll() {
        return origin.getAll();
    }

    @Override
    public Account get(UUID id) {
        return origin.get(id);
    }

    @Override
    public void transfer(UUID from, UUID to, Money money) {
        executeWithRetry(() -> {
            origin.transfer(from, to, money);
            return null;
        });
    }

    @Override
    public Account deposit(UUID id, Money money) {
        return executeWithRetry(() -> origin.deposit(id, money));
    }

    @Override
    public Account withdraw(UUID id, Money money) {
        return executeWithRetry(() ->origin.withdraw(id, money));
    }

    @FunctionalInterface
    private interface Retryable<T> {
        T execute();
    }

    private <T> T executeWithRetry(Retryable<T> retryable) {
        int trialsCount = 0;
        while (true) {
            try {
                return retryable.execute();
            } catch (AccountOptimisticLockException e) {
                trialsCount++;
                if (trialsCount >= MAX_RETRY_COUNT) {
                    throw e;
                }
            }
        }
    }
}
