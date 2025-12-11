package com.example.steamlensbackend.steam.services;

import com.example.steamlensbackend.steam.dto.response.*;
import com.example.steamlensbackend.steam.wrappers.SuccessResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StatisticsService {

    private final SteamService steamService;

    public StatisticsService(SteamService steamService) {
        this.steamService = steamService;
    }

    public SuccessResponse<DashboardStatisticResponse> getDashboardStatistics(String steamId) {
        List<GameResponse> games = steamService.getUserOwnedGames(steamId, null).response().games();
        List<GameResponse> gamesFromLastTwoWeeks = getLastTwoWeeksPlayedGames(games);
        TotalLastTwoWeeksPlaytime lastTwoWeeksPlaytime = totalLastTwoWeeksPlaytime(gamesFromLastTwoWeeks);
        Integer numberOfRecentPlayedGames = gamesFromLastTwoWeeks.size();
        List<RecentPlayedGame> recentPlayedGames = getRecentPlayedGame(gamesFromLastTwoWeeks);

        DashboardStatisticResponse statistics = new DashboardStatisticResponse(lastTwoWeeksPlaytime, numberOfRecentPlayedGames, recentPlayedGames);
        return SuccessResponse.of(statistics);
    }

    private List<RecentPlayedGame> getRecentPlayedGame(List<GameResponse> gamesFromLastTwoWeeks) {
        return gamesFromLastTwoWeeks.stream().map(
                game -> {
                    String imgIconUrl = String.format(
                            "http://media.steampowered.com/steamcommunity/public/images/apps/%s/%s.jpg",
                            game.appid(), game.imgIconUrl()
                    );
                    long foreverPlaytimeMinutes = game.playtimeForever();
                    long foreverPlaytimeHours = game.playtimeForever() / 60;
                    long playtime2WeeksMinutes = game.playtime2Weeks();
                    long playtime2WeeksHours = game.playtime2Weeks() / 60;

                    return new RecentPlayedGame(game.name(), imgIconUrl, foreverPlaytimeHours, foreverPlaytimeMinutes, playtime2WeeksHours, playtime2WeeksMinutes);
                }
        ).toList();
    }

    private TotalLastTwoWeeksPlaytime totalLastTwoWeeksPlaytime(List<GameResponse> games) {
        long totalMinutes = games.stream().mapToLong(GameResponse::playtime2Weeks).sum();
        long totalHours = totalMinutes / 60;

        return new TotalLastTwoWeeksPlaytime(totalHours, totalMinutes);
    }

    private List<GameResponse> getLastTwoWeeksPlayedGames(List<GameResponse> games) {
        return games.stream().filter(
                game -> {
                    Instant lastTimePlayed = Instant.ofEpochSecond(game.rtimeLastPlayed());
                    Instant twoWeeksAgo = Instant.now().minus(14, ChronoUnit.DAYS);

                    return lastTimePlayed.isAfter(twoWeeksAgo) && lastTimePlayed.isBefore(Instant.now());
                }
        ).toList();
    }
}
