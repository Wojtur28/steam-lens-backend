package com.example.steamlensbackend.family.dto;

import javax.money.MonetaryAmount;

public record OwnerGameValue(
        String steamId,
        String name,
        String avatarUrl,
        MonetaryAmount totalValue,
        int gameCount
) {
}
