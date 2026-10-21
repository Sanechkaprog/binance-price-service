package com.example.binance_price_service.service;

public class RecordsNotFoundException extends RuntimeException {
    public RecordsNotFoundException(String message) {
        super(message);
    }
}
