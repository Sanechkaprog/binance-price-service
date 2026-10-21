package com.example.binance_price_service.dto;

import java.util.List;

public record PricePageResponseDto (
        List<RecordResponseDto> recordsList,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {
}
