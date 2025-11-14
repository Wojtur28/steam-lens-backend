package com.example.steamlensbackend.steam.wrappers;

public record PagedResponse<T>(
    boolean success,
    T data,
    Meta meta
) implements ApiResponse<T> {
    public static <T> PagedResponse<T> of(boolean success,T data, Meta meta) {
        return new PagedResponse<T>(success, data, meta);
    }
}
