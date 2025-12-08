package com.example.steamlensbackend.steam.dto.wrapper;

import com.example.steamlensbackend.steam.dto.response.SteamGameDetailsResponse;

public record SteamAppWrapper(
    boolean success,
    SteamGameDetailsResponse data
) {}
