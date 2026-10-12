package com.aliimran.personal_website_api.exception;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
