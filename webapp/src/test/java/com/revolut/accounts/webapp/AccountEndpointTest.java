package com.revolut.accounts.webapp;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
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
        assertThat(response.statusCode()).isEqualTo(201);
        AccountDto createdAccount = response.as(AccountDto.class);
        assertThat(createdAccount.getBalance()).isEqualTo(0);
        AccountDto loadedByLocationHeader = given()
                .get(response.getHeader("location"))
                .as(AccountDto.class);
        assertThat(loadedByLocationHeader).isEqualTo(createdAccount);
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

    @Test
    public void canAddMoneyToExistingAccount() {
        AccountDto account = createAccount();
        Response response = depositResponse(account.getId(), 5000);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.as(AccountDto.class)).isEqualTo(new AccountDto(account.getId(), 5000));
    }

    @Test
    public void canWithdrawMoneyFromAccount() {
        AccountDto account = createAccount();
        depositResponse(account.getId(), 7000);

        Response response = given()
                .param("amount", 5000)
                .post("/withdraw/{id}", account.getId())
                .andReturn();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.as(AccountDto.class)).isEqualTo(new AccountDto(account.getId(), 2000));
    }

    @Test
    public void canTransferMoneyFromOneAccountToAnother() {
        AccountDto from = createAccount();
        AccountDto to = createAccount();
        depositResponse(from.getId(), 7000);

        Response response = transfer(from.getId(), to.getId(), 6000);
        assertThat(response.statusCode()).isEqualTo(204);

        AccountDto fromAfter = getAccount(from.getId());
        AccountDto toAfter = getAccount(to.getId());
        assertThat(fromAfter.getBalance()).isEqualTo(1000);
        assertThat(toAfter.getBalance()).isEqualTo(6000);
    }

    @Test
    public void transferReturnsNotFound_whenFromAccountDoesNotExist() {
        AccountDto to = createAccount();
        UUID from = UUID.randomUUID();
        Response response = transfer(from, to.getId(), 5670);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.asString()).isEqualTo("Account [" + from + "] is not found");
    }

    @Test
    public void transferReturnsBadRequest_whenAmountIsNegative() {
        AccountDto from = createAccount();
        AccountDto to = createAccount();
        depositResponse(from.getId(), 7000);

        Response response = transfer(from.getId(), to.getId(), -6000);
        assertThat(response.statusCode()).isEqualTo(400);
    }

    private Response transfer(UUID from, UUID to, int amount) {
        return given()
                    .param("from", from)
                    .param("to", to)
                    .param("amount", amount)
                    .post("/transfer");
    }

    private AccountDto getAccount(UUID id) {
        return given().get("/{id}", id).as(AccountDto.class);
    }

    private Response depositResponse(UUID id, int amount) {
        return given()
                .param("amount", amount)
                .post("/deposit/{id}", id)
                .andReturn();
    }

    private AccountDto createAccount() {
        return createAccountResponse().as(AccountDto.class);
    }

    private Response createAccountResponse() {
        return given().post("").andReturn();
    }
}
