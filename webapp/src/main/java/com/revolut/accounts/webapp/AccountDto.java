package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import lombok.*;

import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class AccountDto {
    private UUID id;
    private long balance;

    public AccountDto(Account account) {
        this.id = account.id();
        this.balance = account.balance().asCents();
    }
}
