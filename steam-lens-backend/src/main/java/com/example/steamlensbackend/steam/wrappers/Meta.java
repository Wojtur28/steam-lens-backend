package com.example.steamlensbackend.steam.wrappers;

public record Meta(
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
