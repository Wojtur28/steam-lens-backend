package com.example.steamlensbackend.config;

import com.example.steamlensbackend.game.model.SteamGameDocument;
import com.example.steamlensbackend.game.repository.SteamGameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

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

                    long priceInCents = data.path("price").asLong(0);
                    String currencyCode = data.path("currency").asText("USD");
                    game.setPrice(BigDecimal.valueOf(priceInCents).movePointLeft(2));
                    game.setCurrency(currencyCode);

                    game.setHeaderImage(data.path("header_image").asText(""));
                    game.setBackground(data.path("background").asText(""));
                    game.setWebsite(data.path("website").asText(""));
                    game.setSupportedLanguages(data.path("supported_languages").asText(""));

                    game.setRequiredAge(data.path("required_age").asInt(0));
                    game.setIsFree(data.path("is_free").asBoolean(false));

                    game.setShortDescription(data.path("short_description").asText(""));
                    game.setDetailedDescription(data.path("detailed_description").asText(""));
                    game.setReleaseDate(data.path("release_date").asText(""));

                    if (data.has("metacritic")) {
                        game.setMetacritic(data.path("metacritic").asInt(0));
                    }

                    Map<String, String> pcReq = new HashMap<>();
                    pcReq.put("minimum", data.path("pc_requirements").path("minimum").asText(""));
                    pcReq.put("recommended", data.path("pc_requirements").path("recommended").asText(""));
                    game.setPcRequirements(pcReq);

                    Map<String, String> macReq = new HashMap<>();
                    macReq.put("minimum", data.path("mac_requirements").path("minimum").asText(""));
                    macReq.put("recommended", data.path("mac_requirements").path("recommended").asText(""));
                    game.setMacRequirements(macReq);

                    List<String> developers = new ArrayList<>();
                    data.path("developers").forEach(n -> developers.add(n.asText()));
                    game.setDevelopers(developers);

                    List<String> publishers = new ArrayList<>();
                    data.path("publishers").forEach(n -> publishers.add(n.asText()));
                    game.setPublishers(publishers);

                    List<String> genres = new ArrayList<>();
                    if (data.has("genres")) {
                        data.path("genres").forEach(n -> genres.add(n.asText()));
                    }
                    game.setGenres(genres);

                    List<String> categories = new ArrayList<>();
                    if (data.has("categories")) {
                        data.path("categories").forEach(n -> categories.add(n.asText()));
                    }
                    game.setCategories(categories);

                    Map<String, Boolean> platforms = new HashMap<>();
                    JsonNode platformsNode = data.path("platforms");
                    platforms.put("windows", platformsNode.path("windows").asBoolean(false));
                    platforms.put("mac", platformsNode.path("mac").asBoolean(false));
                    platforms.put("linux", platformsNode.path("linux").asBoolean(false));
                    game.setPlatforms(platforms);

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
