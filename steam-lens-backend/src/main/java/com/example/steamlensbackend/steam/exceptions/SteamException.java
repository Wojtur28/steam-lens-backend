package com.example.steamlensbackend.steam.exceptions;


public class SteamException extends RuntimeException {
  public SteamException(String message, Throwable cause) {
      super(message, cause);
  }
}
