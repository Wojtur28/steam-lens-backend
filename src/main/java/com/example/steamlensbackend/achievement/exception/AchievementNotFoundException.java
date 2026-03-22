package com.example.steamlensbackend.achievement.exception;

public class AchievementNotFoundException extends RuntimeException {
    public AchievementNotFoundException(String message) {
        super(message);
    }

    public AchievementNotFoundException(String appId) {
        super("No achievements found for game: " + appId);
    }
}
