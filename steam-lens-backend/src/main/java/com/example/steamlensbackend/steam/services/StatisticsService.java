package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.RecentPlayedGame;
import com.example.steamlensbackend.steam.dto.response.TotalLastTwoWeeksPlaytime;
import com.example.steamlensbackend.steam.dto.response.DashboardStatisticResponse;
import com.example.steamlensbackend.steam.dto.response.GameResponse;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private final SteamService steamService;

    public StatisticsService(SteamService steamService) {
        this.steamService = steamService;
    }

    public Mono<SuccessResponse<DashboardStatisticResponse>> getDashboardStatistics(String steamId) {
        Mono<List<GameResponse>> games = this.steamService.getUserOwnedGames(steamId, null).map(
                response -> response.response().games()
        );
        Mono<List<GameResponse>> gamesFromLastTwoWeeks = this.getLastTwoWeeksPlayedGames(games);
        Mono<TotalLastTwoWeeksPlaytime> lastTwoWeeksPlaytime = this.totalLastTwoWeeksPlaytime(gamesFromLastTwoWeeks);
        Mono<Integer> numberOfRecentPlayedGames = gamesFromLastTwoWeeks.map(gameResponses -> gameResponses.size());
        Mono<List<RecentPlayedGame>> recentPlayedGames = this.getRecentPlayedGame(gamesFromLastTwoWeeks);

        return Mono.zip(lastTwoWeeksPlaytime, numberOfRecentPlayedGames, recentPlayedGames)
                .map(
                        tuple -> {
                            TotalLastTwoWeeksPlaytime totalLastTwoWeeksPlaytime = tuple.getT1();
                            Integer totalRecentPlayedGames = tuple.getT2();
                            List<RecentPlayedGame> recentPlayedGamesList = tuple.getT3();

                            return new DashboardStatisticResponse(totalLastTwoWeeksPlaytime, totalRecentPlayedGames, recentPlayedGamesList);
                        }
                ).map( statistics -> SuccessResponse.of(statistics));
    }

    private Mono<List<RecentPlayedGame>> getRecentPlayedGame(Mono<List<GameResponse>> gamesFromLastTwoWeeks) {
        return gamesFromLastTwoWeeks.map(
                gameResponses -> {
                    return gameResponses.stream().map(
                            game -> {
                                String imgIconUrl = String.format(
                                        "http://media.steampowered.com/steamcommunity/public/images/apps/%s/%s.jpg",
                                        game.appid(), game.imgIconUrl()
                                );
                                Long foreverPlaytimeMinutes = (long) game.playtimeForever();
                                Long foreverPlaytimeHours = (long) game.playtimeForever() / 60;
                                Long playtime2WeeksMinutes = (long) game.playtime2Weeks();
                                Long playtime2WeeksHours = (long) game.playtime2Weeks() / 60;


                                return new RecentPlayedGame(game.name(), imgIconUrl, foreverPlaytimeHours, foreverPlaytimeMinutes, playtime2WeeksHours, playtime2WeeksMinutes);
                            }
                    ).toList();
                }
        );
    }

    private Mono<TotalLastTwoWeeksPlaytime> totalLastTwoWeeksPlaytime(Mono<List<GameResponse>> games) {
        return games.map(
                gameList -> {
                    Long totalMinutes = gameList.stream().mapToLong(GameResponse::playtime2Weeks).sum();
                    Long totalHours = (long) (totalMinutes / 60);

                    return new TotalLastTwoWeeksPlaytime(totalHours, totalMinutes);
                }
        );
    }

    private Mono<List<GameResponse>> getLastTwoWeeksPlayedGames(Mono<List<GameResponse>> games) {
        return games.map(
                gameList ->
                        gameList.stream().filter(
                                game -> {
                                    Instant lastTimePlayed = Instant.ofEpochSecond(game.rtimeLastPlayed());
                                    Instant twoWeeksAgo = Instant.now().minus(14, ChronoUnit.DAYS);

                                    return lastTimePlayed.isAfter(twoWeeksAgo) && lastTimePlayed.isBefore(Instant.now());
                                }
                        ).toList()
        );
    }

}
