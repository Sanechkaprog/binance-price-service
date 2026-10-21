package com.example.binance_price_service.dto;

import java.math.BigDecimal;
import java.util.Optional;

public record AvgPriceInPeriodResponseDto (
        Optional<BigDecimal> averagePrice
) {
}
