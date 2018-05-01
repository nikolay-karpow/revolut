package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AccountDto {
    private UUID id;
    private long balance;

    public AccountDto(Account account) {
        this.id = account.id();
        this.balance = account.balance().asLong();
    }
}
