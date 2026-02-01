package com.looplink.stickerengine.model;

import java.util.List;

/**
 * Response DTO showing a shopper's sticker status.
 */
public record ShopperStatus(
    String shopperId,
    int totalStickers,
    List<TransactionSummary> transactions
) {
    public record TransactionSummary(
        String transactionId,
        String storeId,
        String timestamp,
        String totalAmount,
        int stickersEarned
    ) {
        public static TransactionSummary from(Transaction tx) {
            return new TransactionSummary(
                tx.transactionId(),
                tx.storeId(),
                tx.timestamp().toString(),
                "$" + tx.totalAmount().toString(),
                tx.stickersEarned()
            );
        }
    }
}
