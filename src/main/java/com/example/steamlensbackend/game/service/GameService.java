package com.example.steamlensbackend.game.service;

import com.example.steamlensbackend.game.dto.PriceOverview;
import com.example.steamlensbackend.game.dto.SteamGameDetailsResponse;
import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final SteamGameRepository steamGameRepository;

    public GameService(SteamGameRepository steamGameRepository) {
        this.steamGameRepository = steamGameRepository;
    }

    public Map<Long, SteamGameDetailsResponse> getSteamGamesDetails(List<Long> appIds) {
        if (appIds == null || appIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SteamGameDocument> games = steamGameRepository.findAllById(appIds);

        return games.stream()
                .collect(Collectors.toMap(
                        SteamGameDocument::getAppId,
                        this::convertToSteamGameDetailsResponse
                ));
    }

    private SteamGameDetailsResponse convertToSteamGameDetailsResponse(SteamGameDocument document) {
        PriceOverview priceOverview = null;
        if (document.getPrice() != null) {
            BigDecimal price = document.getPrice();
            String currency = document.getCurrency() != null ? document.getCurrency() : "USD";

            int priceInCents = price.movePointRight(2).intValue();
            String priceFormatted = String.format("%.2f %s", price, currency);

            priceOverview = new PriceOverview(
                    currency,
                    priceInCents,
                    priceInCents,
                    0,
                    priceFormatted,
                    priceFormatted
            );
        }

        SteamGameDetailsResponse.Platforms platformsDto = new SteamGameDetailsResponse.Platforms(false, false, false);
        if (document.getPlatforms() != null) {
            platformsDto = new SteamGameDetailsResponse.Platforms(
                    document.getPlatforms().getOrDefault("windows", false),
                    document.getPlatforms().getOrDefault("mac", false),
                    document.getPlatforms().getOrDefault("linux", false)
            );
        }

        List<SteamGameDetailsResponse.Genre> genresDto = List.of();
        if (document.getGenres() != null) {
            genresDto = document.getGenres().stream()
                    .map(g -> new SteamGameDetailsResponse.Genre("0", g))
                    .toList();
        }

        List<SteamGameDetailsResponse.Category> categoriesDto = List.of();
        if (document.getCategories() != null) {
            categoriesDto = document.getCategories().stream()
                    .map(c -> new SteamGameDetailsResponse.Category(0, c))
                    .toList();
        }

        SteamGameDetailsResponse.Metacritic metacriticDto = new SteamGameDetailsResponse.Metacritic(
                document.getMetacritic() != null ? document.getMetacritic() : 0
        );

        SteamGameDetailsResponse.ReleaseDate releaseDateDto = new SteamGameDetailsResponse.ReleaseDate(
                false,
                document.getReleaseDate() != null ? document.getReleaseDate() : ""
        );

        List<String> developers = document.getDevelopers() != null ? document.getDevelopers() : List.of();

        SteamGameDetailsResponse.Requirements pcReqDto = null;
        if (document.getPcRequirements() != null) {
            pcReqDto = new SteamGameDetailsResponse.Requirements(
                    document.getPcRequirements().get("minimum"),
                    document.getPcRequirements().get("recommended")
            );
        }

        SteamGameDetailsResponse.Requirements macReqDto = null;
        if (document.getMacRequirements() != null) {
            macReqDto = new SteamGameDetailsResponse.Requirements(
                    document.getMacRequirements().get("minimum"),
                    document.getMacRequirements().get("recommended")
            );
        }

        return new SteamGameDetailsResponse(
                "game",
                document.getName(),
                document.getAppId().intValue(),
                document.getRequiredAge() != null ? document.getRequiredAge() : 0, // DANE Z BAZY
                document.getIsFree() != null ? document.getIsFree() : false,       // DANE Z BAZY
                document.getDetailedDescription(),
                null, // aboutTheGame (możesz użyć detailedDescription lub dodać pole)
                document.getShortDescription(),
                null, // fullGame
                document.getSupportedLanguages(), // DANE Z BAZY
                document.getHeaderImage(),
                null, // capsuleImage
                null, // capsuleImageV5
                document.getWebsite(),            // DANE Z BAZY
                pcReqDto,                         // DANE Z BAZY
                macReqDto,                        // DANE Z BAZY
                null, // linuxRequirements
                document.getDevelopers(),
                document.getPublishers(),         // DANE Z BAZY
                List.of(), // packageGroups
                platformsDto,
                metacriticDto,
                categoriesDto,
                genresDto,
                releaseDateDto,
                null, // supportInfo
                document.getBackground(),         // DANE Z BAZY
                null, // backgroundRaw
                null, // contentDescriptors
                null, // ratings
                priceOverview
        );
    }
}
