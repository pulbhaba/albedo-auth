package com.akbo.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class ForbiddenException extends HttpStatusCodeException {

    public ForbiddenException(final String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

}
