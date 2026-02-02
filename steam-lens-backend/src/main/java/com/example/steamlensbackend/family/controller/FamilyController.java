package com.example.steamlensbackend.family.controller;

import com.example.steamlensbackend.async.dto.AsyncRequestAcceptedResponse;
import com.example.steamlensbackend.async.dto.AsyncRequestStatusResponse;
import com.example.steamlensbackend.async.service.AsyncRequestService;
import com.example.steamlensbackend.common.wrappers.SuccessResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupDetailsResponse;
import com.example.steamlensbackend.family.dto.FamilyGroupForUserResponse;
import com.example.steamlensbackend.family.dto.FamilyWishlistResponse;
import com.example.steamlensbackend.family.service.FamilyService;
import com.example.steamlensbackend.family.service.SharedLibraryAsyncService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final AsyncRequestService asyncRequestService;
    private final SharedLibraryAsyncService sharedLibraryAsyncService;

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
    public ResponseEntity<SuccessResponse<AsyncRequestAcceptedResponse>> getSharedLibrary(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @PathVariable String familyGroupId,
            @RequestParam String steamId,
            Pageable pageable,
            HttpServletRequest request) {

        AsyncRequestAcceptedResponse response = sharedLibraryAsyncService.initiateSharedLibraryRequest(
                accessToken, familyGroupId, steamId, apiKey, pageable, request
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(SuccessResponse.of(response));
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<SuccessResponse<AsyncRequestStatusResponse>> getRequestStatus(
            @PathVariable String requestId) {
        return ResponseEntity.ok(SuccessResponse.of(asyncRequestService.getRequestStatus(requestId)));
    }

    @GetMapping("/{familyGroupId}/wishlist")
    public ResponseEntity<SuccessResponse<FamilyWishlistResponse>> getFamilyWishlist(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestHeader("X-Steam-Access-Token") String accessToken,
            @RequestHeader("X-Steam-Id") String steamId,
            @PathVariable String familyGroupId,
            Pageable pageable) {
        return ResponseEntity.ok(SuccessResponse.of(
                familyService.getFamilyWishlist(accessToken, familyGroupId, steamId, apiKey, pageable)
        ));
    }

}
