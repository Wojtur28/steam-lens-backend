package com.example.steamlensbackend.steam.controllers;

import com.example.steamlensbackend.steam.dto.response.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.steam.dto.response.FamilyGroupForUserResponse;
import com.example.steamlensbackend.steam.dto.response.SharedLibraryWithOwnersResponse;
import com.example.steamlensbackend.steam.services.SteamService;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/steam/family")
public class FamilyController {

    private final SteamService steamService;

    public FamilyController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/my-group")
    public ResponseEntity<SuccessResponse<FamilyGroupForUserResponse>> getMyFamilyGroup(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @RequestParam String steamId) {
        return ResponseEntity.ok(SuccessResponse.of(steamService.getFamilyGroupForUser(steamId, accessToken).response()));
    }

    @GetMapping("/details/{familyGroupId}")
    public ResponseEntity<SuccessResponse<FamilyGroupDetailsResponse>> getFamilyDetails(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId) {
        return ResponseEntity.ok(SuccessResponse.of(steamService.getFamilyGroupDetails(familyGroupId, accessToken).response()));
    }

    @GetMapping("/shared-library/{familyGroupId}")
    public ResponseEntity<SuccessResponse<SharedLibraryWithOwnersResponse>> getSharedLibrary(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId,
            @RequestParam String steamId) {
        return ResponseEntity.ok(SuccessResponse.of(steamService.getSharedLibraryApps(accessToken, familyGroupId, steamId)));
    }
}
