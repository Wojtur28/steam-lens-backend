package com.example.steamlensbackend.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamBaseResponse<T>(
        @JsonProperty("response") T response
) {
}
