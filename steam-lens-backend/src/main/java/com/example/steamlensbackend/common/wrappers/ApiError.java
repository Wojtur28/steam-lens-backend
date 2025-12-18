package com.example.steamlensbackend.common.wrappers;

import java.util.List;
import java.util.Map;

public record ApiError(
        String status,
        String message,
        Map<String, List<String>> details
) {
}
