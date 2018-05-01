package com.revolut.accounts.persistence;

public interface Transactional<T> {
    T execute();
}
