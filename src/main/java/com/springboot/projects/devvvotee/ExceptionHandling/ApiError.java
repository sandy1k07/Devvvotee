package com.springboot.projects.devvvotee.ExceptionHandling;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus httpStatus,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<String> subErrors,
        Instant timeStamp
) {
    public ApiError(HttpStatus status, String message){
        this(status, message, null,  Instant.now());
    }

    public ApiError(HttpStatus status, String message, List<String> errors){
        this(status, message, errors, Instant.now());
    }
}
