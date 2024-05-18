package com.akbo.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class UnauthorizedException extends HttpStatusCodeException {

    public UnauthorizedException(final String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

}
