package com.example.steamlensbackend.common.wrappers;

public record PagedResponse<T>(
    boolean success,
    T data,
    Meta meta
) implements ApiResponse<T> {
    public static <T> PagedResponse<T> of(T data, Meta meta) {
        return new PagedResponse<T>(true, data, meta);
    }
}
