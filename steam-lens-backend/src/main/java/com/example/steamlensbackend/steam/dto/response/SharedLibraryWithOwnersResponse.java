package com.example.steamlensbackend.steam.dto.response;

import java.util.List;

public record SharedLibraryWithOwnersResponse(
    SharedLibraryAppsResponse library,
    List<SteamPlayerSummariesResponse.Player> owners
) {}
