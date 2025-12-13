package com.example.steamlensbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Domyślna konfiguracja (np. 10 minut)
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Specyficzne konfiguracje dla konkretnych cache'ów
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache dla listy gier użytkownika - rzadko się zmienia
        cacheConfigurations.put("steamGames", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        // Cache dla statusów graczy - zmienia się często
        cacheConfigurations.put("playerSummaries", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // Cache dla detali gier (sugeruję dodać @Cacheable do getSteamGameDetails)
        cacheConfigurations.put("gameDetails", defaultCacheConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
