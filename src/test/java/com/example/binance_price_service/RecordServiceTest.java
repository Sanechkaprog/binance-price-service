package com.example.binance_price_service;

import com.example.binance_price_service.config.CollectorProperties;
import com.example.binance_price_service.dto.AvgPriceInPeriodResponseDto;
import com.example.binance_price_service.dto.RecordResponseDto;
import com.example.binance_price_service.dto.TrendResponseDto;
import com.example.binance_price_service.model.PriceRecord;
import com.example.binance_price_service.repository.PriceRecordRepository;
import com.example.binance_price_service.service.RecordService;
import com.example.binance_price_service.service.RecordsNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private PriceRecordRepository repository;

    @Mock
    private CollectorProperties properties;

    @InjectMocks
    private RecordService service;

    private final Instant from = Instant.parse("2025-01-01T00:00:00Z");
    private final Instant to = Instant.parse("2025-01-02T00:00:00Z");

    @BeforeEach
    void setUp() {
        when(properties.getSymbol()).thenReturn("BNBUSDT");
    }

    @Test
    void getLastReturnsLatestRecord() {
        RecordResponseDto expected = new RecordResponseDto(
                "BNBUSDT",
                new BigDecimal("620.50"),
                to
        );
        when(repository.findTop1ByOrderByFetchedAtDesc())
                .thenReturn(Optional.of(expected));

        RecordResponseDto actual = service.getLast();

        assertEquals(expected, actual);
    }

    @Test
    void getLastThrowsWhenThereAreNoRecords() {
        when(repository.findTop1ByOrderByFetchedAtDesc())
                .thenReturn(Optional.empty());

        assertThrows(RecordsNotFoundException.class, () -> service.getLast());
    }

    @Test
    void getAveragePriceReturnsRepositoryResult() {
        AvgPriceInPeriodResponseDto expected =
                new AvgPriceInPeriodResponseDto(Optional.of(new BigDecimal("615.25")));

        when(repository.getAveragePriceInPeriod(from, to)).thenReturn(expected);

        AvgPriceInPeriodResponseDto actual = service.getAvgPriceInPeriod(from, to);

        assertEquals(expected, actual);
        verify(repository).getAveragePriceInPeriod(from, to);
    }

    @Test
    void getAveragePriceThrowsWhenPeriodHasNoRecords() {
        when(repository.getAveragePriceInPeriod(from, to))
                .thenReturn(new AvgPriceInPeriodResponseDto(Optional.empty()));

        assertThrows(
                RecordsNotFoundException.class,
                () -> service.getAvgPriceInPeriod(from, to)
        );
    }

    @Test
    void getAveragePriceThrowsWhenFromIsAfterTo() {
        assertThrows(
                IllegalStateException.class,
                () -> service.getAvgPriceInPeriod(to, from)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAveragePriceThrowsWhenDatesAreNull() {
        assertThrows(
                IllegalStateException.class,
                () -> service.getAvgPriceInPeriod(null, to)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getTrendReturnsUpwardWhenPriceIncreases() {
        when(repository.getList(from, to)).thenReturn(List.of(
                new RecordResponseDto("BNBUSDT", new BigDecimal("100"), from),
                new RecordResponseDto("BNBUSDT", new BigDecimal("110"), to)
        ));

        TrendResponseDto result = service.getTrend(from, to);

        assertEquals("UPWARD", result.trend());
        assertEquals("BNBUSDT", result.symbol());
    }

    @Test
    void getTrendReturnsDownwardWhenPriceDecreases() {
        when(repository.getList(from, to)).thenReturn(List.of(
                new RecordResponseDto("BNBUSDT", new BigDecimal("110"), from),
                new RecordResponseDto("BNBUSDT", new BigDecimal("100"), to)
        ));

        assertEquals("DOWNWARD", service.getTrend(from, to).trend());
    }

    @Test
    void getTrendReturnsFlatWhenPricesAreEqual() {
        when(repository.getList(from, to)).thenReturn(List.of(
                new RecordResponseDto("BNBUSDT", new BigDecimal("100.00"), from),
                new RecordResponseDto("BNBUSDT", new BigDecimal("100.0"), to)
        ));

        assertEquals("FLAT", service.getTrend(from, to).trend());
    }

    @Test
    void getTrendThrowsWhenThereAreFewerThanTwoRecords() {
        when(repository.getList(from, to)).thenReturn(List.of(
                new RecordResponseDto("BNBUSDT", new BigDecimal("100"), from)
        ));

        assertThrows(
                RecordsNotFoundException.class,
                () -> service.getTrend(from, to)
        );
    }

    @Test
    void getPricePageThrowsWhenPageNumberIsNegative() {
        assertThrows(
                IllegalStateException.class,
                () -> service.getPricePage(from, to, -1, 10)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getPricePageThrowsWhenSizeIsOutsideAllowedRange() {
        assertThrows(
                IllegalStateException.class,
                () -> service.getPricePage(from, to, 0, 201)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getPricePageReturnsMappedRecords() {
        PriceRecord record = new PriceRecord(
                "BNBUSDT",
                new BigDecimal("620.50"),
                from
        );

        Page<PriceRecord> page = new PageImpl<>(
                List.of(record),
                PageRequest.of(0, 10),
                1
        );

        when(repository.getPricePage(eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(page);

        var result = service.getPricePage(from, to, 0, 10);

        assertEquals(1, result.recordsList().size());
        assertEquals("BNBUSDT", result.recordsList().getFirst().symbol());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalElements());
        assertFalse(result.hasNext());
    }
}
