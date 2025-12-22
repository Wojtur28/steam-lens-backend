package com.example.steamlensbackend.family.controller;

import com.example.steamlensbackend.common.wrappers.SuccessResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupForUserResponse;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.example.steamlensbackend.family.service.FamilyService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/my-group")
    public ResponseEntity<SuccessResponse<FamilyGroupForUserResponse>> getMyFamilyGroup(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @RequestParam String steamId) {
        return ResponseEntity.ok(SuccessResponse.of(familyService.getFamilyGroupForUser(steamId, accessToken).response()));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<FamilyGroupDetailsResponse>> getFamilyDetails(
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @RequestHeader("X-Steam-Id") String steamId,
            @RequestHeader("X-Family-Id") String familyGroupId) {
        return ResponseEntity.ok(SuccessResponse.of(familyService.getFamilyGroupDetails(accessToken, familyGroupId, steamId).response()));
    }

    @GetMapping("/{familyGroupId}/shared-library")
    public ResponseEntity<SuccessResponse<SharedLibraryPriceResponse>> getSharedLibrary(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId,
            @RequestParam String steamId,
            Pageable pageable) {
        return ResponseEntity.ok(SuccessResponse.of(familyService.getSharedLibraryApps(accessToken, familyGroupId, steamId, pageable, apiKey)));
    }
}
