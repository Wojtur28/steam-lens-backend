package com.example.steamlensbackend.steam.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SteamGameDetailsResponse(
        String type,
        String name,
        @JsonProperty("steam_appid") int steamAppId,
        @JsonProperty("required_age") int requiredAge,
        @JsonProperty("is_free") boolean isFree,
        @JsonProperty("detailed_description") String detailedDescription,
        @JsonProperty("about_the_game") String aboutTheGame,
        @JsonProperty("short_description") String shortDescription,
        @JsonProperty("fullgame") FullGame fullGame,
        @JsonProperty("supported_languages") String supportedLanguages,
        @JsonProperty("header_image") String headerImage,
        @JsonProperty("capsule_image") String capsuleImage,
        @JsonProperty("capsule_imagev5") String capsuleImageV5,
        String website,
        @JsonProperty("pc_requirements") Requirements pcRequirements,
        @JsonProperty("mac_requirements") Requirements macRequirements,
        @JsonProperty("linux_requirements") Object linuxRequirements,
        List<String> developers,
        @JsonProperty("package_groups") List<Object> packageGroups,
        Platforms platforms,
        Metacritic metacritic,
        List<Category> categories,
        List<Genre> genres,
        @JsonProperty("release_date") ReleaseDate releaseDate,
        @JsonProperty("support_info") SupportInfo supportInfo,
        String background,
        @JsonProperty("background_raw") String backgroundRaw,
        @JsonProperty("content_descriptors") ContentDescriptors contentDescriptors,
        Object ratings
) {

    public record FullGame(String appid, String name) {}

    public record Requirements(String minimum, String recommended) {}

    public record Platforms(boolean windows, boolean mac, boolean linux) {}

    public record Metacritic(int score) {}

    public record Category(int id, String description) {}

    public record Genre(String id, String description) {}

    public record ReleaseDate(@JsonProperty("coming_soon") boolean comingSoon, String date) {}

    public record SupportInfo(String url, String email) {}

    public record ContentDescriptors(List<Integer> ids, String notes) {}
}
