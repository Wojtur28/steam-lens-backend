package com.example.steamlensbackend.steam.exceptions;

import com.example.steamlensbackend.steam.wrappers.ApiResponse;
import com.example.steamlensbackend.steam.wrappers.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SteamException.class)
    public ResponseEntity<ApiResponse<Object>> handleSteamException(SteamException e) {
        ErrorResponse<Object> errorResponse = ErrorResponse.of("SOMETHING_WENT_WRONG", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        ErrorResponse<Object> errorResponse = ErrorResponse.of("SOMETHINHG_WENT_WRONG", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
