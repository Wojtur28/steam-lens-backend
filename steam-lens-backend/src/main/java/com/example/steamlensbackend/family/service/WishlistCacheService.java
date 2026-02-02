package com.example.steamlensbackend.family.service;

import com.example.steamlensbackend.family.dto.FamilyWishlistResponse.WishlistedGame;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "wishlist:family:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public void cacheWishlist(String familyGroupId, List<WishlistedGame> games, int totalMembers) {
        String key = CACHE_PREFIX + familyGroupId;
        WishlistCacheEntry entry = new WishlistCacheEntry(games, totalMembers);
        redisTemplate.opsForValue().set(key, entry, CACHE_TTL);
        log.info("Cached wishlist for family {} with {} games", familyGroupId, games.size());
    }

    public Optional<WishlistCacheEntry> getCachedWishlist(String familyGroupId) {
        String key = CACHE_PREFIX + familyGroupId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            WishlistCacheEntry entry = objectMapper.convertValue(value, WishlistCacheEntry.class);
            log.info("Cache hit for family wishlist {}", familyGroupId);
            return Optional.of(entry);
        }
        log.info("Cache miss for family wishlist {}", familyGroupId);
        return Optional.empty();
    }

    public void invalidateCache(String familyGroupId) {
        String key = CACHE_PREFIX + familyGroupId;
        redisTemplate.delete(key);
        log.info("Invalidated cache for family wishlist {}", familyGroupId);
    }

    public record WishlistCacheEntry(
        List<WishlistedGame> games,
        int totalMembers
    ) {}
}
