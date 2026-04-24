package com.skinsshowcase.steamgateway.exception;

/**
 * Ошибка вызова сервиса items (каталог цен).
 */
public class ItemsServiceException extends RuntimeException {

    public ItemsServiceException(String message) {
        super(message);
    }

    public ItemsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
