package com.example.steamlensbackend.family.dto;

import java.math.BigDecimal;

public record OwnerGameValue(
        String steamId,
        String name,
        String avatarUrl,
        BigDecimal totalValue,
        int gameCount
) {
}
