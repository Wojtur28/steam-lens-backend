package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.options.GetOwnedGamesOptions;
import com.example.steamlensbackend.steam.dto.response.OwnedGamesResponse;
import com.example.steamlensbackend.steam.dto.response.SteamBaseResponse;
import com.example.steamlensbackend.steam.exceptions.SteamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SteamService {

    private final WebClient webClient;
    private final String apiKey;

    public SteamService(@Qualifier("steamWebClient") WebClient webClient, @Value("${steam.api.key}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
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
                .bodyToMono(new ParameterizedTypeReference<SteamBaseResponse<OwnedGamesResponse>>() {})
                .onErrorMap(e -> {
                    System.out.println("Error getting owned games" + e);
                    return new SteamException(e.getMessage(), e);
                });

    }
}
