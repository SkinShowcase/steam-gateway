package com.skinsshowcase.steamgateway.exception;

/**
 * В инвентаре нет предмета с указанной парой assetId + classId.
 */
public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(String message) {
        super(message);
    }
}
