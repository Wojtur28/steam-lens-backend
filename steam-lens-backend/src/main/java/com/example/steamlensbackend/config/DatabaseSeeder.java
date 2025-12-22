package com.example.steamlensbackend.config;

import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final SteamGameRepository repository;
    private final ObjectMapper objectMapper;

    public DatabaseSeeder(SteamGameRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            logger.info("Baza danych nie jest pusta, pomijam import.");
            return;
        }

        logger.info("Rozpoczynam import danych z pliku JSON...");

        try (InputStream inputStream = new ClassPathResource("steam_games.json").getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);

            List<SteamGameDocument> gamesToSave = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                try {
                    Long appId = Long.parseLong(field.getKey());
                    JsonNode data = field.getValue();

                    SteamGameDocument game = new SteamGameDocument();
                    game.setAppId(appId);
                    game.setName(data.path("name").asText("Unknown"));

                    double priceVal = data.path("price").asDouble(0.0);
                    game.setPrice(Money.of(priceVal, "USD"));

                    game.setHeaderImage(data.path("header_image").asText(""));

                    gamesToSave.add(game);

                    if (gamesToSave.size() >= 1000) {
                        repository.saveAll(gamesToSave);
                        gamesToSave.clear();
                        logger.info("Zapisano paczkę gier...");
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Pominięto klucz (nie jest ID): " + field.getKey());
                }
            }

            if (!gamesToSave.isEmpty()) {
                repository.saveAll(gamesToSave);
            }

            logger.info("Import zakończony sukcesem! Liczba gier w bazie: " + repository.count());
        } catch (Exception e) {
            logger.error("Błąd podczas importu danych: ", e);
        }
    }
}
