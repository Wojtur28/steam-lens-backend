package com.example.steamlensbackend.common.wrappers;

public sealed interface ApiResponse<T> permits ErrorResponse, SuccessResponse, PagedResponse {
}
