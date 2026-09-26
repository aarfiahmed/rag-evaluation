package com.example.rageval.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> bad(IllegalArgumentException e) {
        return Map.of("timestamp", Instant.now().toString(), "status", 400, "error", "Bad Request", "message", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Map<String, Object> other(Exception e) {
        return Map.of("timestamp", Instant.now().toString(), "status", 500, "error", "Internal Server Error", "message", "Unexpected error");
    }
}
