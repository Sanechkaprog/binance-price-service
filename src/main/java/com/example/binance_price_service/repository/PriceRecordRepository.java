package com.example.binance_price_service.repository;

import com.example.binance_price_service.dto.AvgPriceInPeriodResponseDto;
import com.example.binance_price_service.dto.RecordResponseDto;
import com.example.binance_price_service.model.PriceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {

    Optional<RecordResponseDto> findTop1ByOrderByFetchedAtDesc();

    @Query("SELECT AVG(p.price) FROM PriceRecord p WHERE p.fetchedAt BETWEEN :from AND :to")
    AvgPriceInPeriodResponseDto getAveragePriceInPeriod(Instant from, Instant to);

    @Query("SELECT p FROM PriceRecord p WHERE p.fetchedAt BETWEEN :from AND :to")
    Page<PriceRecord> getPricePage(Instant from, Instant to, Pageable pageable);

    @Query("SELECT p FROM PriceRecord p WHERE p.fetchedAt BETWEEN :from AND :to ORDER BY p.fetchedAt")
    List<RecordResponseDto> getList(Instant from, Instant to);
}
