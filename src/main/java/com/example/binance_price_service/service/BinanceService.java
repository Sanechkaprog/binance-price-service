package com.example.binance_price_service.service;

import com.example.binance_price_service.dto.BinanceResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BinanceService {
    private final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(BinanceService.class);

    @Autowired
    public BinanceService(RestClient restClient) {
        this.restClient = restClient;
    }

    public BinanceResponseDto getResponse(String symbol) {
               BinanceResponseDto binanceResponseDto = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v3/avgPrice")
                                .queryParam("symbol", symbol)
                                .build())
                        .retrieve()
                        .body(BinanceResponseDto.class);
               if (binanceResponseDto == null || binanceResponseDto.price() == null || binanceResponseDto.price().isBlank()) {
                   logger.warn("Invalid response from Binance");
                   throw new IllegalStateException("Binance send empty response");
               }
               return binanceResponseDto;
    }
}
