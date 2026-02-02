package com.example.steamlensbackend.game.dto;

import java.math.BigDecimal;
import java.util.List;

public record GamePriceInfo(
        Long appId,
        String name,
        String headerImage,
        BigDecimal price,
        List<String> ownerSteamIds
) {
}
