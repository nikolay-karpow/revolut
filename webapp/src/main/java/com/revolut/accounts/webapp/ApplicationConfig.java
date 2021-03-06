package com.revolut.accounts.webapp;

import com.revolut.accounts.persistence.ConnectionHolder;
import com.revolut.accounts.persistence.JdbcAccounts;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import static com.revolut.accounts.persistence.DataSourceFactory.createDataSource;

public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        ConnectionHolder connectionHolder = new ConnectionHolder(createDataSource());
        JdbcAccounts accounts = new JdbcAccounts(connectionHolder);
        AccountService accountService = new RetryingAccountService(
                new AccountServiceImpl(connectionHolder, accounts)
        );
        register(new JacksonJsonProvider());
        register(new AccountEndpoint(accountService));
        register(new AccountNotFoundExceptionMapper());
        register(new AccountIllegalArgumentExceptionMapper());
    }
}
