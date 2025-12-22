package com.example.steamlensbackend.game.service;

import com.example.steamlensbackend.game.dto.PriceOverview;
import com.example.steamlensbackend.game.dto.SteamGameDetailsResponse;
import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
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
            MonetaryAmount price = document.getPrice();
            int priceInCents = price.getNumber().numberValue(Integer.class);
            String currencyCode = price.getCurrency().getCurrencyCode();
            String priceFormatted = String.format("%.2f %s", priceInCents / 100.0, currencyCode);

            priceOverview = new PriceOverview(
                    currencyCode,
                    priceInCents,
                    priceInCents,
                    0,
                    priceFormatted,
                    priceFormatted
            );
        }

        return new SteamGameDetailsResponse(
                null,
                document.getName(),
                document.getAppId().intValue(),
                0,
                false,
                null,
                null,
                null,
                null,
                null,
                document.getHeaderImage(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                priceOverview
        );
    }
}
