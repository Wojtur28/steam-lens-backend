package com.example.steamlensbackend.async.dto;

import com.example.steamlensbackend.async.model.RequestStatus;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AsyncRequestStatusResponse(
        String requestId,
        RequestStatus status,
        SharedLibraryPriceResponse result,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public static AsyncRequestStatusResponse pending(String requestId, Instant createdAt, Instant updatedAt) {
        return new AsyncRequestStatusResponse(requestId, RequestStatus.PENDING, null, null, createdAt, updatedAt);
    }

    public static AsyncRequestStatusResponse processing(String requestId, Instant createdAt, Instant updatedAt) {
        return new AsyncRequestStatusResponse(requestId, RequestStatus.PROCESSING, null, null, createdAt, updatedAt);
    }

    public static AsyncRequestStatusResponse completed(String requestId, SharedLibraryPriceResponse result, Instant createdAt, Instant updatedAt) {
        return new AsyncRequestStatusResponse(requestId, RequestStatus.COMPLETED, result, null, createdAt, updatedAt);
    }

    public static AsyncRequestStatusResponse failed(String requestId, String error, Instant createdAt, Instant updatedAt) {
        return new AsyncRequestStatusResponse(requestId, RequestStatus.FAILED, null, error, createdAt, updatedAt);
    }
}
