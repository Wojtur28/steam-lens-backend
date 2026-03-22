package com.example.steamlensbackend.config;

import com.example.steamlensbackend.config.properties.SteamApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class SteamConfig {

    private final SteamApiProperties steamApiProperties;

    @Bean
    public RestTemplate steamWebClient(RestTemplateBuilder builder) {
        return builder
                .rootUri(steamApiProperties.api().baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public RestTemplate steamStoreWebClient(RestTemplateBuilder builder) {
        return builder
                .rootUri(steamApiProperties.store().api().baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
