package com.example.steamlensbackend.common.wrappers;

public record SuccessResponse<T>(
        boolean success,
        T data
) implements ApiResponse<T>
{
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<T>(true, data);
    }
}
