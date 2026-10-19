package com.example.binance_price_service.service;

import com.example.binance_price_service.dto.BinanceResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BinanceService {
    private final RestClient restClient;

    @Autowired
    public BinanceService(RestClient restClient) {
        this.restClient = restClient;
    }

    public BinanceResponseDto sendResponse(String symbol) {
               BinanceResponseDto binanceResponseDto = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v3/avgPrice")
                                .queryParam("symbol", symbol)
                                .build())
                        .retrieve()
                        .body(BinanceResponseDto.class);
               if (binanceResponseDto == null || binanceResponseDto.price() == null || binanceResponseDto.price().isBlank()) {
                   throw new IllegalStateException("Binance send empty response");
               }
               return binanceResponseDto;
    }
}
