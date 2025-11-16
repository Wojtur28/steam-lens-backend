package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.RecentPlayedGame;
import com.example.steamlensbackend.steam.dto.response.TotalLastTwoWeeksPlaytime;
import com.example.steamlensbackend.steam.dto.response.DashboardStatisticResponse;
import com.example.steamlensbackend.steam.dto.response.GameResponse;
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

    public Mono<DashboardStatisticResponse> getDashboardStatistics(String steamId) {
        Mono<List<GameResponse>> games = this.steamService.getUserOwnedGames(steamId, null).map(
                response -> response.response().games()
        );
        Mono<List<GameResponse>> gamesFromLastTwoWeeks = this.getLastTwoWeeksPlayedGames(games);
        Mono<TotalLastTwoWeeksPlaytime> lastTwoWeeksPlaytime = this.totalLastTwoWeeksPlaytime(gamesFromLastTwoWeeks);
        Mono<Integer> numberOfRecentPlayedGames = gamesFromLastTwoWeeks.map(gameResponses -> gameResponses.size());

        return new DashboardStatisticResponse(lastTwoWeeksPlaytime, numberOfRecentPlayedGames, );
    }

    private Mono<RecentPlayedGame> getRecentPlayedGame(Mono<List<GameResponse>> gamesFromLastTwoWeeks) {
        return gamesFromLastTwoWeeks.map(
                gameResponses -> {
                    gameResponses.stream().map(
                            game -> {
                                String imgIconUrl = String.format(
                                        "http://media.steampowered.com/steamcommunity/public/images/apps/%s/%s.jpg",
                                        game.appid(), game.imgIconUrl()
                                );
                                Long foreverPlaytimeMinutes = (long) game.playtimeForever();


                                return new RecentPlayedGame(game.name(),)
                            }
                    )
                }
        )
    }

    private Mono<TotalLastTwoWeeksPlaytime> totalLastTwoWeeksPlaytime(Mono<List<GameResponse>> games) {
        return games.map(
                gameList -> {
                    Long totalMinutes = gameList.stream().mapToLong(GameResponse::playtime2Weeks).sum();
                    Long totalHours = (long) (totalMinutes / 60);

                    return new TotalLastTwoWeeksPlaytime(totalMinutes, totalHours);
                }
        );
    }

    private Mono<List<GameResponse>> getLastTwoWeeksPlayedGames(Mono<List<GameResponse>> games) {
        return games.map(
                gameList ->
                        gameList.stream().filter(
                                game -> {
                                    Instant lastTimePlayed = Instant.ofEpochSecond(game.rtimeLastPlayed());
                                    Instant twoWeeksAgo = Instant.now().minus(2, ChronoUnit.WEEKS);

                                    return lastTimePlayed.isAfter(twoWeeksAgo) && lastTimePlayed.isBefore(Instant.now());
                                }
                        ).toList()
        );
    }

}
