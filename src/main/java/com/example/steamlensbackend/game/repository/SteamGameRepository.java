package com.example.steamlensbackend.game.repository;

import com.example.steamlensbackend.game.model.SteamGameDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SteamGameRepository extends MongoRepository<SteamGameDocument, Long> {
}
