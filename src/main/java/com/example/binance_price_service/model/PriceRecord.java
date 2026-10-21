package com.example.binance_price_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_records", indexes = {@Index(name = "idx_price_symbol_fetched_at", columnList = "symbol,fetched_at")})
public class PriceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "price", nullable = false, scale = 30, precision = 12)
    private BigDecimal price;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public PriceRecord() {}

    public PriceRecord(String symbol, BigDecimal price, Instant fetchedAt) {
        this.symbol = symbol;
        this.price = price;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}
