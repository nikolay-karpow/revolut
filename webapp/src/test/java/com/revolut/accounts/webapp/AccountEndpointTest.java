package com.revolut.accounts.webapp;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.revolut.accounts.core.Account;
import com.revolut.accounts.core.Money;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountEndpointTest {

    private static final int PORT = 8080;
    private static final WebServer server = new WebServer(UriBuilder.fromUri("http://localhost/").port(PORT).build());

    @BeforeClass
    public static void beforeClass() throws Exception {
        server.start();
        RestAssured.port = PORT;
        RestAssured.baseURI = "http://localhost:8080/account";
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    @Test
    public void canAddAccount() {
        Response response = createAccountResponse();
        assertThat(response.statusCode()).isEqualTo(200);
        AccountDto createdAccount = response.as(AccountDto.class);
        assertThat(createdAccount.getBalance()).isEqualTo(0);
    }

    @Test
    public void canGiveListOfAccounts() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();

        Response response = given().get();

        assertThat(response.statusCode()).isEqualTo(200);
        AccountDto[] accounts = response.as(AccountDto[].class);
        List<UUID> accountIds = Arrays.stream(accounts)
                .map(AccountDto::getId).collect(toList());
        assertThat(accountIds).contains(first.getId());
        assertThat(accountIds).contains(second.getId());
    }

    private AccountDto createAccount() {
        return createAccountResponse().as(AccountDto.class);
    }

    private Response createAccountResponse() {
        return given().post("").andReturn();
    }
}
