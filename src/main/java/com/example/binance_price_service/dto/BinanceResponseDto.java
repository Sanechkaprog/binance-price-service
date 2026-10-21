package com.example.binance_price_service.dto;

public record BinanceResponseDto (
        long mins,
        String price,
        long closeTime
){
}
