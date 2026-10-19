package com.example.binance_price_service.repository;

import com.example.binance_price_service.model.PriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {
}
