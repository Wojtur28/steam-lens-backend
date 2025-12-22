package com.example.steamlensbackend.game.dto;

import javax.money.MonetaryAmount;
import java.util.List;

public record GamePriceInfo(
        Long appId,
        String name,
        String headerImage,
        MonetaryAmount price,
        List<String> ownerSteamIds
) {
}
