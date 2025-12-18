package com.example.steamlensbackend.game.dto;

public record SteamAppWrapper(
    boolean success,
    SteamGameDetailsResponse data
) {}
