package com.example.steamlensbackend.steam.exceptions;

import org.apache.tomcat.util.http.Parameters;

public class SteamException extends RuntimeException {
  public SteamException(String message, Throwable cause) {
      super(message, cause);
  }
}
