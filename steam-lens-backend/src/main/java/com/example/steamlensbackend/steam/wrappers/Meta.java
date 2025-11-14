package com.example.steamlensbackend.steam.wrappers;

public record Meta(
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
    public Meta {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be greater than zero");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
    }
}
