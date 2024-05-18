package com.akbo.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends HttpStatusCodeException {

    public BadRequestException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
