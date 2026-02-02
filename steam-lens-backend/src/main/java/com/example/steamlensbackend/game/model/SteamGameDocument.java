package com.example.steamlensbackend.game.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Document(collection = "games")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SteamGameDocument {

    @Id
    private Long appId;

    private String name;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    private String currency;

    @Field("header_image")
    private String headerImage;

    @Field("required_age")
    private Integer requiredAge;

    @Field("is_free")
    private Boolean isFree;

    @Field("detailed_description")
    private String detailedDescription;

    @Field("short_description")
    private String shortDescription;

    @Field("supported_languages")
    private String supportedLanguages;

    private String website;

    @Field("pc_requirements")
    private Map<String, String> pcRequirements;

    @Field("mac_requirements")
    private Map<String, String> macRequirements;

    private List<String> developers;
    private List<String> publishers;

    private Map<String, Boolean> platforms;

    private Integer metacritic;

    private List<String> categories;
    private List<String> genres;

    @Field("release_date")
    private String releaseDate;

    private String background;
}
