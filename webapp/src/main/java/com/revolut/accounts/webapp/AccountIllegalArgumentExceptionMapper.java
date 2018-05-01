package com.revolut.accounts.webapp;

import com.revolut.accounts.core.AccountOperationIllegalArgumentException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class AccountIllegalArgumentExceptionMapper implements
        ExceptionMapper<AccountOperationIllegalArgumentException> {
    @Override
    public Response toResponse(AccountOperationIllegalArgumentException exception) {
        return Response
                .status(BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
