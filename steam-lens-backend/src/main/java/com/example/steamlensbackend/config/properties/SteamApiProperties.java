package com.example.steamlensbackend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "steam")
public record SteamApiProperties(
        ApiProperties api,
        StoreProperties store
) {
    public record ApiProperties(
            String baseUrl
    ) {}

    public record StoreProperties(
            ApiProperties api
    ) {}
}
