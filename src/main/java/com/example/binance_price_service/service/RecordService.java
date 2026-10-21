package com.example.binance_price_service.service;

import com.example.binance_price_service.config.CollectorProperties;
import com.example.binance_price_service.dto.AvgPriceInPeriodResponseDto;
import com.example.binance_price_service.dto.PricePageResponseDto;
import com.example.binance_price_service.dto.RecordResponseDto;
import com.example.binance_price_service.dto.TrendResponseDto;
import com.example.binance_price_service.model.PriceRecord;
import com.example.binance_price_service.repository.PriceRecordRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class RecordService {
    private final PriceRecordRepository priceRecordRepository;
    private final Logger logger = LoggerFactory.getLogger(RecordService.class);
    private final CollectorProperties collectorProperties;

    @Autowired
    public RecordService(PriceRecordRepository priceRecordRepository, CollectorProperties collectorProperties) {
        this.priceRecordRepository = priceRecordRepository;
        this.collectorProperties = collectorProperties;
    }

    @Transactional
    public RecordResponseDto getLast() {
        Optional<RecordResponseDto> recordResponseDtoOptional = priceRecordRepository.findTop1ByOrderByFetchedAtDesc();
        if (recordResponseDtoOptional.isEmpty()) {
            logger.warn("DataBase is empty");
            throw new IllegalStateException("There are no records");
        }
        return recordResponseDtoOptional.get();
    }

    @Transactional
    public AvgPriceInPeriodResponseDto getAvgPriceInPeriod(Instant from, Instant to) {
        return priceRecordRepository.getAveragePriceInPeriod(from, to);
    }

    @Transactional
    public PricePageResponseDto getPricePage(Instant from, Instant to, int pageNumber, int size) {
        PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by("fetchedAt").ascending());
        Page<PriceRecord> priceRecordPage = priceRecordRepository.getPricePage(from, to, pageRequest);
        return new PricePageResponseDto(
                priceRecordPage.getContent()
                        .stream()
                        .map(x -> new RecordResponseDto(x.getSymbol(), x.getPrice(), x.getFetchedAt()))
                        .toList(),
                priceRecordPage.getNumber(),
                priceRecordPage.getSize(),
                priceRecordPage.getTotalElements(),
                priceRecordPage.hasNext()
        );
    }

    @Transactional
    public TrendResponseDto getTrend(Instant from, Instant to) {
        List<RecordResponseDto> priceRecords = priceRecordRepository.getList(from, to);
        BigDecimal firstRecord = priceRecords.getFirst().price();
        BigDecimal lastRecord = priceRecords.getLast().price();
        String trend = lastRecord.compareTo(firstRecord) > 0 ? "UPWARD"
                : lastRecord.compareTo(firstRecord) < 0 ? "DOWNWARD"
                : "FLAT";
        return new TrendResponseDto (
                collectorProperties.getSymbol(),
                from.atZone(ZoneOffset.UTC).toLocalDate(),
                to.atZone(ZoneOffset.UTC).toLocalDate(),
                trend
        );

    }



}
