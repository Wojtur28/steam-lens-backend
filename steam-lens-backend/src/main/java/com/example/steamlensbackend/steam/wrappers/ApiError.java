package com.example.steamlensbackend.steam.wrappers;

import java.util.List;
import java.util.Map;

public record ApiError(
        String status,
        String message,
        Map<String, List<String>> details
) {
}
