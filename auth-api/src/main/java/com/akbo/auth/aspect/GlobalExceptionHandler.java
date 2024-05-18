package com.akbo.auth.aspect;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import com.akbo.auth.dto.ErrorDto;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorDto> handleException(HttpStatusCodeException e) {
        final ErrorDto error = new ErrorDto();
        error.setStatusCode(e.getRawStatusCode());
        error.setMessage(e.getStatusText());
        return new ResponseEntity<>(error, e.getStatusCode());
    }

}
