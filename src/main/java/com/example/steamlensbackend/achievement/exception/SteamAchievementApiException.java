package com.example.steamlensbackend.achievement.exception;

public class SteamAchievementApiException extends RuntimeException {
    public SteamAchievementApiException(String message) {
        super(message);
    }

    public SteamAchievementApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
