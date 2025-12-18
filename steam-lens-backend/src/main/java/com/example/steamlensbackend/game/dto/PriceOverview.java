package com.example.steamlensbackend.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PriceOverview(
        String currency,
        int initial,
        @JsonProperty("final") int finalPrice,
        @JsonProperty("discount_percent") int discountPercent,
        @JsonProperty("initial_formatted") String initialFormatted,
        @JsonProperty("final_formatted") String finalFormatted
) {}
