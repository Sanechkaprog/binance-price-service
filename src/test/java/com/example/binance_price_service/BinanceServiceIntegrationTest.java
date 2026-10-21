package com.example.binance_price_service;

import com.example.binance_price_service.dto.BinanceResponseDto;
import com.example.binance_price_service.service.BinanceService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

class BinanceServiceIntegrationTest {

    private WireMockServer wireMockServer;
    private BinanceService binanceService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMockServer.baseUrl())
                .build();

        binanceService = new BinanceService(restClient);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void getResponseSendsRequestAndParsesJson() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v3/avgPrice"))
                .withQueryParam("symbol", equalTo("BNBUSDT"))
                .willReturn(okJson("""
                        {
                          "mins": 5,
                          "price": "620.50",
                          "closeTime": 123456789
                        }
                        """)));

        BinanceResponseDto result = binanceService.getResponse("BNBUSDT");

        assertNotNull(result);
        assertEquals(5L, result.mins());
        assertEquals("620.50", result.price());
        assertEquals(123456789L, result.closeTime());

        wireMockServer.verify(
                1,
                getRequestedFor(urlPathEqualTo("/api/v3/avgPrice"))
                        .withQueryParam("symbol", equalTo("BNBUSDT"))
        );
    }

    @Test
    void getResponseThrowsWhenServerReturnsError() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v3/avgPrice"))
                .withQueryParam("symbol", equalTo("BNBUSDT"))
                .willReturn(serverError()));

        assertThrows(
                RuntimeException.class,
                () -> binanceService.getResponse("BNBUSDT")
        );
    }
}
