package com.example.binance_price_service.service;


import com.example.binance_price_service.config.CollectorProperties;
import com.example.binance_price_service.dto.BinanceResponseDto;
import com.example.binance_price_service.model.PriceRecord;
import com.example.binance_price_service.repository.PriceRecordRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

@Service
public class PriceCollectorService {
    private final BinanceService binanceService;
    private final PriceRecordRepository priceRecordRepository;
    private final CollectorProperties collectorProperties;
    private final Logger logger = LoggerFactory.getLogger(PriceCollectorService.class);

    @Autowired
    public PriceCollectorService(BinanceService binanceService, PriceRecordRepository priceRecordRepository, CollectorProperties collectorProperties) {
        this.binanceService = binanceService;
        this.priceRecordRepository = priceRecordRepository;
        this.collectorProperties = collectorProperties;
    }

    @Scheduled(fixedDelayString = "${binance.poll-interval-ms:30000}")
    @Transactional
    public void record() {
        try {
            String symbol = normalizeSymbol(collectorProperties.getSymbol());
            BinanceResponseDto binanceResponseDto = binanceService.sendResponse(symbol);
            PriceRecord priceRecord = new PriceRecord(
                    symbol,
                    new BigDecimal(binanceResponseDto.price()),
                    Instant.now()
            );
            priceRecordRepository.save(priceRecord);
            logger.info("Record symbol={}, price={}, fetched={}", symbol, binanceResponseDto.price(), priceRecord.getFetchedAt());
        } catch (Exception ex) {
            logger.warn("Record fail");
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || !symbol.matches("[A-Za-z0-9]{5,20}")) {
            throw new IllegalArgumentException("symbol must contain 5-20 letters or digits");
        }
        return symbol.toUpperCase(Locale.ROOT);
    }



}
