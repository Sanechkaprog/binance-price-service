package com.example.binance_price_service;

import com.example.binance_price_service.config.CollectorProperties;
import com.example.binance_price_service.dto.BinanceResponseDto;
import com.example.binance_price_service.model.PriceRecord;
import com.example.binance_price_service.repository.PriceRecordRepository;
import com.example.binance_price_service.service.BinanceService;
import com.example.binance_price_service.service.PriceCollectorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceCollectorServiceTest {

    @Mock
    private BinanceService binanceService;

    @Mock
    private PriceRecordRepository repository;

    @Mock
    private CollectorProperties properties;

    @InjectMocks
    private PriceCollectorService collectorService;

    @Test
    void recordFetchesPriceAndSavesRecord() {
        when(properties.getSymbol()).thenReturn("bnbusdt");
        when(binanceService.getResponse("BNBUSDT"))
                .thenReturn(new BinanceResponseDto(5, "620.50", 123456L));

        collectorService.record();

        ArgumentCaptor<PriceRecord> captor =
                ArgumentCaptor.forClass(PriceRecord.class);

        verify(repository).save(captor.capture());

        PriceRecord saved = captor.getValue();
        assertEquals("BNBUSDT", saved.getSymbol());
        assertEquals(0, new BigDecimal("620.50").compareTo(saved.getPrice()));
        assertNotNull(saved.getFetchedAt());
        verify(binanceService).getResponse("BNBUSDT");
    }

    @Test
    void recordDoesNotSaveWhenSymbolIsInvalid() {
        when(properties.getSymbol()).thenReturn("BNB!");

        assertDoesNotThrow(() -> collectorService.record());

        verifyNoInteractions(binanceService);
        verify(repository, never()).save(any(PriceRecord.class));
    }

    @Test
    void recordDoesNotSaveWhenBinanceRequestFails() {
        when(properties.getSymbol()).thenReturn("BNBUSDT");
        when(binanceService.getResponse("BNBUSDT"))
                .thenThrow(new RuntimeException("Network error"));

        assertDoesNotThrow(() -> collectorService.record());

        verify(repository, never()).save(any(PriceRecord.class));
    }

    @Test
    void recordDoesNotSaveWhenPriceCannotBeParsed() {
        when(properties.getSymbol()).thenReturn("BNBUSDT");
        when(binanceService.getResponse("BNBUSDT"))
                .thenReturn(new BinanceResponseDto(5, "not-a-number", 123456L));

        assertDoesNotThrow(() -> collectorService.record());

        verify(repository, never()).save(any(PriceRecord.class));
    }
}
