package com.skinsshowcase.steamgateway.exception;

/**
 * Ошибка при обращении к Steam API / Community.
 */
public class SteamApiException extends RuntimeException {

    public SteamApiException(String message) {
        super(message);
    }

    public SteamApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
