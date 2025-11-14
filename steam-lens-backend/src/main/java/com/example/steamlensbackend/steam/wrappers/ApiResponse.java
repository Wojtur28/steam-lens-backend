package com.example.steamlensbackend.steam.wrappers;

public sealed interface ApiResponse<T> permits ErrorResponse, SuccessResponse, PagedResponse {
}
