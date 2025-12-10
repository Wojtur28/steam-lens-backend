package com.example.steamlensbackend.steam.controllers;

import com.example.steamlensbackend.steam.dto.response.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.steam.dto.response.FamilyGroupForUserResponse;
import com.example.steamlensbackend.steam.dto.response.SharedLibraryAppsResponse;
import com.example.steamlensbackend.steam.dto.response.SharedLibraryWithOwnersResponse;
import com.example.steamlensbackend.steam.services.SteamService;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/steam/family")
public class FamilyController {

    private final SteamService steamService;

    public FamilyController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/my-group")
    public Mono<SuccessResponse<FamilyGroupForUserResponse>> getMyFamilyGroup(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @RequestParam String steamId) {

        return steamService.getFamilyGroupForUser(steamId, accessToken)
                .map(response -> SuccessResponse.of(response.response()));
    }

    @GetMapping("/details/{familyGroupId}")
    public Mono<SuccessResponse<FamilyGroupDetailsResponse>> getFamilyDetails(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId) {

        return steamService.getFamilyGroupDetails(familyGroupId, accessToken)
                .map(response -> SuccessResponse.of(response.response()));
    }

    @GetMapping("/shared-library/{familyGroupId}")
    public Mono<SuccessResponse<SharedLibraryWithOwnersResponse>> getSharedLibrary(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId,
            @RequestParam String steamId) {

        return steamService.getSharedLibraryApps(accessToken, familyGroupId, steamId)
                .map(SuccessResponse::of);
    }
}
