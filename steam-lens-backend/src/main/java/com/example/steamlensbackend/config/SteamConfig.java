package com.example.steamlensbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SteamConfig {

    @Value("${steam.api.baseUrl}")
    private String steamBaseUrl;

    @Value("${steam.store.api.baseUrl}")
    private String steamStoreBaseUrl;

    @Bean
    public RestTemplate steamWebClient(RestTemplateBuilder builder) {
        return builder
                .rootUri(steamBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public RestTemplate steamStoreWebClient(RestTemplateBuilder builder) {
        return builder
                .rootUri(steamStoreBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
