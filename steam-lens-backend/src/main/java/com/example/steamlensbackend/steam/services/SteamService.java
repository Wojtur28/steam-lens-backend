package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.options.GetOwnedGamesOptions;
import com.example.steamlensbackend.steam.dto.response.*;
import com.example.steamlensbackend.steam.dto.wrapper.SteamAppWrapper;
import com.example.steamlensbackend.steam.exceptions.SteamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SteamService {

    private final RestTemplate webClient;
    private final RestTemplate storeWebClient;
    private final String apiKey;

    public SteamService(@Qualifier("steamWebClient") RestTemplate webClient,
                        @Qualifier("steamStoreWebClient") RestTemplate storeWebClient,
                        @Value("${steam.api.key}") String apiKey) {
        this.webClient = webClient;
        this.storeWebClient = storeWebClient;
        this.apiKey = apiKey;
    }

    @Cacheable(value = "steamGames", key = "#steamId")
    public SteamBaseResponse<OwnedGamesResponse> getUserOwnedGames(
            String steamId,
            GetOwnedGamesOptions options
    ) {
        GetOwnedGamesOptions defaultOptions = GetOwnedGamesOptions.defaultOptions();
        GetOwnedGamesOptions finalOptions = options == null ? defaultOptions : options.mergeWithDefaults(defaultOptions);

        System.out.println("CACHE MISS! Pobieram z API Steam dla: " + steamId);

        String url = UriComponentsBuilder.fromPath("/IPlayerService/GetOwnedGames/v1/")
                .queryParam("key", apiKey)
                .queryParam("steamid", steamId)
                .queryParam("include_appinfo", finalOptions.includeAppInfo())
                .queryParam("include_played_free_games", finalOptions.includePlayedFreeGames())
                .queryParam("language", finalOptions.language())
                .queryParam("include_free_sub", finalOptions.includeFreeSub())
                .queryParam("skip_unvetted_apps", finalOptions.skipUnvettedApps())
                .queryParam("include_extended_appinfo", finalOptions.includeExtendedAppInfo())
                .toUriString();

        try {
            ResponseEntity<SteamBaseResponse<OwnedGamesResponse>> response = webClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SteamBaseResponse<OwnedGamesResponse>>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("STEAM API ERROR: " + e.getStatusCode() + " Body: " + e.getResponseBodyAsString());
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            System.out.println("Error getting owned games: " + e.getMessage());
            throw new SteamException("Error getting owned games", e);
        }
    }

    public SteamBaseResponse<FamilyGroupForUserResponse> getFamilyGroupForUser(String steamId, String accessToken) {
        String url = UriComponentsBuilder.fromPath("/IFamilyGroupsService/GetFamilyGroupForUser/v1/")
                .queryParam("access_token", accessToken)
                .queryParam("include_family_group_response", true)
                .toUriString();
        try {
            ResponseEntity<SteamBaseResponse<FamilyGroupForUserResponse>> response = webClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SteamBaseResponse<FamilyGroupForUserResponse>>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new SteamException("Error getting family group for user", e);
        }
    }

    public SteamBaseResponse<FamilyGroupDetailsResponse> getFamilyGroupDetails(String familyGroupId, String accessToken) {
        String url = UriComponentsBuilder.fromPath("/IFamilyGroupsService/GetFamilyGroup/v1/")
                .queryParam("access_token", accessToken)
                .queryParam("family_groupid", familyGroupId)
                .toUriString();
        try {
            ResponseEntity<SteamBaseResponse<FamilyGroupDetailsResponse>> response = webClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SteamBaseResponse<FamilyGroupDetailsResponse>>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new SteamException("Error getting family group details", e);
        }
    }

    public SteamGameDetailsResponse getSteamGameDetails(String appId) {
        String url = UriComponentsBuilder.fromPath("/api/appdetails")
                .queryParam("appids", appId)
                .toUriString();

        try {
            ResponseEntity<Map<String, SteamAppWrapper>> response = storeWebClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, SteamAppWrapper>>() {}
            );

            Map<String, SteamAppWrapper> responseMap = response.getBody();
            if (responseMap != null) {
                SteamAppWrapper wrapper = responseMap.get(appId);
                if (wrapper != null && wrapper.success() && wrapper.data() != null) {
                    return wrapper.data();
                }
            }
            throw new SteamException("Game details not found or success is false for appId: " + appId, null);

        } catch (HttpClientErrorException e) {
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new SteamException("Error getting game details for appId: " + appId, e);
        }
    }

    public SharedLibraryWithOwnersResponse getSharedLibraryApps(String accessToken, String familyGroupId, String steamId) {
        String libraryUrl = UriComponentsBuilder.fromPath("/IFamilyGroupsService/GetSharedLibraryApps/v1/")
                .queryParam("access_token", accessToken)
                .queryParam("family_groupid", familyGroupId)
                .queryParam("steamid", steamId)
                .queryParam("include_own", true)
                .queryParam("include_excluded", true)
                .queryParam("include_free", false)
                .toUriString();

        try {
            ResponseEntity<SteamBaseResponse<SharedLibraryAppsResponse>> libraryResponseEntity = webClient.exchange(
                    libraryUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SteamBaseResponse<SharedLibraryAppsResponse>>() {}
            );

            SteamBaseResponse<SharedLibraryAppsResponse> libraryBaseResponse = libraryResponseEntity.getBody();
            if (libraryBaseResponse == null || libraryBaseResponse.response() == null) {
                throw new SteamException("Failed to retrieve shared library apps", null);
            }
            SharedLibraryAppsResponse libraryResponse = libraryBaseResponse.response();

            Set<String> uniqueOwnerIds = libraryResponse.apps().stream()
                    .flatMap(app -> app.ownerSteamIds().stream())
                    .collect(Collectors.toSet());

            if (uniqueOwnerIds.isEmpty()) {
                return new SharedLibraryWithOwnersResponse(libraryResponse, Collections.emptyList());
            }

            String steamIdsParam = String.join(",", uniqueOwnerIds);
            SteamPlayerSummariesResponse summaryResponse = getPlayerSummaries(steamIdsParam);
            return new SharedLibraryWithOwnersResponse(libraryResponse, summaryResponse.response().players());

        } catch (HttpClientErrorException e) {
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new SteamException("Error getting shared library apps", e);
        }
    }

    @Cacheable(value = "playerSummaries", key = "#steamids")
    public SteamPlayerSummariesResponse getPlayerSummaries(String steamids) {
        String url = UriComponentsBuilder.fromPath("/ISteamUser/GetPlayerSummaries/v2/")
                .queryParam("key", apiKey)
                .queryParam("steamids", steamids)
                .toUriString();
        try {
            ResponseEntity<SteamPlayerSummariesResponse> response = webClient.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SteamPlayerSummariesResponse>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SteamException("Steam API returned error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new SteamException("Error getting player summaries", e);
        }
    }
}


