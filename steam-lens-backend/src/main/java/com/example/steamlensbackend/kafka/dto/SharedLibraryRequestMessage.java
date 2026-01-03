package com.example.steamlensbackend.kafka.dto;

import java.io.Serializable;

public record SharedLibraryRequestMessage(
        String requestId,
        String accessToken,
        String familyGroupId,
        String steamId,
        String apiKey,
        int page,
        int size
) implements Serializable {
}
