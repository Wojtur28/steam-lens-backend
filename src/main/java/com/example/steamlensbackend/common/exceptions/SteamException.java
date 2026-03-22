package com.example.steamlensbackend.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SteamException extends RuntimeException {
  private final HttpStatus httpStatus;

  public SteamException(String message, Throwable cause) {
      this(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public SteamException(String message, Throwable cause, HttpStatus httpStatus) {
      super(message, cause);
      this.httpStatus = httpStatus;
  }
}
