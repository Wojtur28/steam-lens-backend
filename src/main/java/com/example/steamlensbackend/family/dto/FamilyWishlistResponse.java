package com.example.steamlensbackend.family.dto;

import com.example.steamlensbackend.common.wrappers.PagedResponse;

import java.util.List;

public record FamilyWishlistResponse(
    PagedResponse<List<WishlistedGame>> games,
    int totalGames,
    int totalMembers
) {
    public record WishlistedGame(
        Long appId,
        String name,
        String headerImage,
        List<WishlistMember> members,
        int memberCount
    ) {}

    public record WishlistMember(
        String steamId,
        String personaName,
        String avatar
    ) {}
}
