package com.example.steamlensbackend.async.model;

import java.io.Serializable;
import java.time.Instant;

public record AsyncRequestState(
        String requestId,
        RequestStatus status,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {

    public static AsyncRequestState pending(String requestId) {
        Instant now = Instant.now();
        return new AsyncRequestState(requestId, RequestStatus.PENDING, null, now, now);
    }

    public AsyncRequestState withStatus(RequestStatus newStatus) {
        return new AsyncRequestState(requestId, newStatus, errorMessage, createdAt, Instant.now());
    }

    public AsyncRequestState withError(String error) {
        return new AsyncRequestState(requestId, RequestStatus.FAILED, error, createdAt, Instant.now());
    }
}
