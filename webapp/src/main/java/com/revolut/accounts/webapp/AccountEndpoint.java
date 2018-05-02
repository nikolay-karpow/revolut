package com.revolut.accounts.webapp;

import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Money;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

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
    public Response createAccount() throws URISyntaxException {
        Account account = accountService.add(new Account());
        return Response
                .created(new URI("/account/" + account.id()))
                .entity(new AccountDto(account))
                .build();
    }

    @GET
    @Path("/account/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto getAccount(@PathParam("id") UUID id) {
        return new AccountDto(accountService.get(id));
    }

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountDto> getAllAccounts() {
        return accountService.getAll().stream()
                .map(AccountDto::new)
                .collect(toList());
    }

    @POST
    @Path("/account/{id}/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto deposit(@PathParam("id") UUID id, @FormParam("amount") long amount) {
        return new AccountDto(
                accountService.deposit(id, new Money(amount))
        );
    }

    @POST
    @Path("/account/{id}/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto withdraw(@PathParam("id") UUID id, @FormParam("amount") long amount) {
        return new AccountDto(
                accountService.withdraw(id, new Money(amount))
        );
    }

    @POST
    @Path("/account/{id}/transferTo")
    @Produces(MediaType.APPLICATION_JSON)
    public void transfer(
            @PathParam("id") UUID from,
            @FormParam("to") UUID to,
            @FormParam("amount") long amount
    ) {
        accountService.transfer(from, to, new Money(amount));
    }
}
