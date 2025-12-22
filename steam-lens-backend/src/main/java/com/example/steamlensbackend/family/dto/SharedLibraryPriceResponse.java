package com.example.steamlensbackend.family.dto;

import com.example.steamlensbackend.common.wrappers.PagedResponse;
import com.example.steamlensbackend.game.dto.GamePriceInfo;

import javax.money.MonetaryAmount;
import java.util.List;

public record SharedLibraryPriceResponse(
        PagedResponse<List<GamePriceInfo>> games,
        List<OwnerGameValue> owners,
        MonetaryAmount totalValue,
        int totalGames
) {
}
