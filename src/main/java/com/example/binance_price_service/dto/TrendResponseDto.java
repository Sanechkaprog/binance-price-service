package com.example.binance_price_service.dto;

import java.time.LocalDate;

public record TrendResponseDto (
        String symbol,
        LocalDate start_date,
        LocalDate end_date,
        String trend
){
}
