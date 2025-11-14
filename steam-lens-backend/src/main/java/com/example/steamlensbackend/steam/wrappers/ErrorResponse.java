package com.example.steamlensbackend.steam.wrappers;

import java.util.List;
import java.util.Map;

public record ErrorResponse<T>(
        boolean success,
        ApiError error
) implements ApiResponse<T>
{
    public static <T> ErrorResponse<T> of(String code, String message) {
        return new ErrorResponse<>(false, new ApiError(code, message, null));
    }
    public static <T> ErrorResponse<T> of(String code, String message, Map<String, List<String>> details) {
        return new ErrorResponse<>(false, new ApiError(code, message, details));
    }
}
