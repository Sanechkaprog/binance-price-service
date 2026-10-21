package com.example.binance_price_service.controller;

import com.example.binance_price_service.dto.AvgPriceInPeriodResponseDto;
import com.example.binance_price_service.dto.PricePageResponseDto;
import com.example.binance_price_service.dto.RecordResponseDto;
import com.example.binance_price_service.dto.TrendResponseDto;
import com.example.binance_price_service.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RequestMapping("/api/v1/prices")
@RestController
public class BinanceController {
    private final RecordService recordService;

    @Autowired
    public BinanceController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/last")
    public RecordResponseDto getLastRecord() {
        return recordService.getLast();
    }

    @GetMapping("/average")
    public AvgPriceInPeriodResponseDto getAvgPriceInPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return recordService.getAvgPriceInPeriod(from, to);
    }

    @GetMapping()
    public PricePageResponseDto getPricePage(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                             @RequestParam int page,
                                             @RequestParam int size) {

        return recordService.getPricePage(from, to, page, size);
    }

    @GetMapping("/trend")
    public TrendResponseDto getTrend(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return recordService.getTrend(from, to);
    }
}
