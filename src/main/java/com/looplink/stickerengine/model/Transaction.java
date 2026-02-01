package com.looplink.stickerengine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Stored transaction record with calculated stickers.
 */
public record Transaction(
    String transactionId,
    String shopperId,
    String storeId,
    Instant timestamp,
    List<Item> items,
    BigDecimal totalAmount,
    int stickersEarned
) {
    public static Transaction from(TransactionRequest request, int stickersEarned) {
        BigDecimal total = request.items().stream()
            .map(Item::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Transaction(
            request.transactionId(),
            request.shopperId(),
            request.storeId(),
            request.timestamp(),
            request.items(),
            total,
            stickersEarned
        );
    }
}
