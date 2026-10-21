package com.example.binance_price_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecordResponseDto (
        String symbol,
        BigDecimal price,
        Instant fetchedAt
) {
}
