package com.example.binance_price_service.controller;


import com.example.binance_price_service.service.RecordsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecordsNotFoundException.class)
    public ResponseEntity<?> notFound(RecordsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<?> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(error(400, ex.getMessage() == null ? "Invalid request" : ex.getMessage()));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<?> upstream(RestClientException ex) {
        logger.warn("Binance API request failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(error(502, "Failed to retrieve data from Binance"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> internal(Exception ex) {
        logger.error("Unhandled API error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(500, "Internal server error"));
    }

    private Map<String, Object> error(int status, String message) {
        return Map.of("timestamp", Instant.now().toString(), "status", status, "error", message);
    }
}
