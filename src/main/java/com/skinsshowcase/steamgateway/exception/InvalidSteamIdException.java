package com.skinsshowcase.steamgateway.exception;

/**
 * Некорректный формат Steam ID (ожидается SteamID64).
 */
public class InvalidSteamIdException extends RuntimeException {

    public InvalidSteamIdException(String message) {
        super(message);
    }
}
