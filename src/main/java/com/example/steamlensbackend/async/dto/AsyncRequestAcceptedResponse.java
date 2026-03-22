package com.example.steamlensbackend.async.dto;

public record AsyncRequestAcceptedResponse(
        String requestId,
        String message,
        String statusUrl
) {
    public static AsyncRequestAcceptedResponse of(String requestId, String baseUrl) {
        return new AsyncRequestAcceptedResponse(
                requestId,
                "Request accepted for processing",
                baseUrl + "/api/v1/family/status/" + requestId
        );
    }
}
