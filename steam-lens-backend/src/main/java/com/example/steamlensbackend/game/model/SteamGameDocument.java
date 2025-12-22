package com.example.steamlensbackend.game.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.money.MonetaryAmount;

@Document(collection = "games")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SteamGameDocument {

    @Id
    private Long appId;

    private String name;

    private MonetaryAmount price;

    @Field("header_image")
    private String headerImage;
}
