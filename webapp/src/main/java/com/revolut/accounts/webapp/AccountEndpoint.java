package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Money;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/")
public class AccountEndpoint {
    private final AccountService accountService;

    public AccountEndpoint(AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto createAccount() {
        return new AccountDto(
                accountService.add(new Account(new Money(0)))
        );
    }

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountDto> getAllAccounts() {
        return accountService.getAll().stream()
                .map(AccountDto::new)
                .collect(toList());
    }
}
