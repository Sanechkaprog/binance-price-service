package com.example.binance_price_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CollectorProperties.class)
public class HttpClientConfig {
    @Bean
    RestClient binanceRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.binance.com")
                .build();
    }
}
