package com.example.steamlensbackend.async.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AsyncRequestNotFoundException extends RuntimeException {

    public AsyncRequestNotFoundException(String requestId) {
        super("Async request not found: " + requestId);
    }
}
