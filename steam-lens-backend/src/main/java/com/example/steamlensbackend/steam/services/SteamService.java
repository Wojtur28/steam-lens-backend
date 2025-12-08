package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.options.GetOwnedGamesOptions;
import com.example.steamlensbackend.steam.dto.response.*;
import com.example.steamlensbackend.steam.dto.wrapper.SteamAppWrapper;
import com.example.steamlensbackend.steam.exceptions.SteamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.Map;

@Service
public class SteamService {

    private final WebClient webClient;
    private final WebClient storeWebClient;
    private final String apiKey;

    public SteamService(@Qualifier("steamWebClient") WebClient webClient,
                        @Qualifier("steamStoreWebClient") WebClient storeWebClient,
                        @Value("${steam.api.key}") String apiKey) {
        this.webClient = webClient;
        this.storeWebClient = storeWebClient;
        this.apiKey = apiKey;
    }

    private Mono<Throwable> handleHttpError(ClientResponse response) {
        return response.bodyToMono(String.class).flatMap(body -> {
            System.out.println("STEAM API ERROR: " + response.statusCode() + " Body: " + body);
            return Mono.error(new SteamException("Steam API returned error: " + response.statusCode() + " " + body, null));
        });
    }

    @Cacheable(value = "steamGames", key = "#steamId")
    public Mono<SteamBaseResponse<OwnedGamesResponse>> getUserOwnedGames(
            String steamId,
            GetOwnedGamesOptions options
    )
    {
        GetOwnedGamesOptions defaultOptions = GetOwnedGamesOptions.defaultOptions();
        GetOwnedGamesOptions finalOptions = options == null ? defaultOptions : options.mergeWithDefaults(defaultOptions);

        System.out.println("CACHE MISS! Pobieram z API Steam dla: " + steamId);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("IPlayerService/GetOwnedGames/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("include_appinfo", finalOptions.includeAppInfo())
                        .queryParam("include_played_free_games", finalOptions.includePlayedFreeGames())
                        .queryParam("language", finalOptions.language())
                        .queryParam("include_free_sub", finalOptions.includeFreeSub())
                        .queryParam("skip_unvetted_apps", finalOptions.skipUnvettedApps())
                        .queryParam("include_extended_appinfo", finalOptions.includeExtendedAppInfo())
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(new ParameterizedTypeReference<SteamBaseResponse<OwnedGamesResponse>>() {})
                .onErrorMap(e -> {
                    if (e instanceof SteamException) return e;
                    System.out.println("Error getting owned games" + e);
                    return new SteamException(e.getMessage(), e);
                });

    }

    public Mono<SteamBaseResponse<FamilyGroupForUserResponse>> getFamilyGroupForUser(String steamId, String accessToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("IFamilyGroupsService/GetFamilyGroupForUser/v1/")
                        .queryParam("access_token", accessToken)
                        .queryParam("include_family_group_response", true)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(new ParameterizedTypeReference<SteamBaseResponse<FamilyGroupForUserResponse>>() {})
                .onErrorMap(e -> {
                    if (e instanceof SteamException) return e;
                    return new SteamException("Error getting family group for user", e);
                });
    }

    public Mono<SteamBaseResponse<FamilyGroupDetailsResponse>> getFamilyGroupDetails(String familyGroupId, String accessToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("IFamilyGroupsService/GetFamilyGroup/v1/")
                        .queryParam("access_token", accessToken)
                        .queryParam("family_groupid", familyGroupId)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(new ParameterizedTypeReference<SteamBaseResponse<FamilyGroupDetailsResponse>>() {})
                .onErrorMap(e -> {
                    if (e instanceof SteamException) return e;
                    return new SteamException("Error getting family group details", e);
                });
    }


    public Mono<SteamGameDetailsResponse> getSteamGameDetails(String appId) {
        return storeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/appdetails")
                        .queryParam("appids", appId)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(new ParameterizedTypeReference<Map<String, SteamAppWrapper>>() {})
                .map(responseMap -> {
                    SteamAppWrapper wrapper = responseMap.get(appId);

                    if (wrapper != null && wrapper.success() && wrapper.data() != null) {
                        return wrapper.data();
                    } else {
                        throw new SteamException("Game details not found or success is false for appId: " + appId, null);
                    }
                })
                .onErrorMap(e -> {
                    if (e instanceof SteamException) return e;
                    return new SteamException("Error getting game details for appId: " + appId, e);
                });
    }

    public Mono<SteamBaseResponse<SharedLibraryAppsResponse>> getSharedLibraryApps(String accessToken, String familyGroupId, String steamId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("IFamilyGroupsService/GetSharedLibraryApps/v1/")
                        .queryParam("access_token", accessToken)
                        .queryParam("family_groupid", familyGroupId)
                        .queryParam("steamid", steamId)
                        .queryParam("include_own", true)
                        .queryParam("include_excluded", true)
                        .queryParam("include_free", false)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleHttpError)
                .bodyToMono(new ParameterizedTypeReference<SteamBaseResponse<SharedLibraryAppsResponse>>() {})
                .doOnError(e -> {
                    System.out.println("ERROR IN GET SHARED LIBRARY: " + e.getMessage());
                    e.printStackTrace();
                })
                .onErrorMap(e -> {
                    if (e instanceof SteamException) return e;
                    return new SteamException("Error getting shared library apps", e);
                });
    }
}


