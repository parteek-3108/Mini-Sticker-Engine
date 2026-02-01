package com.looplink.stickerengine.model;

/**
 * Response DTO for transaction submission.
 */
public record TransactionResponse(
    String transactionId,
    String shopperId,
    int stickersEarned,
    int newTotalBalance,
    boolean duplicate,
    String message
) {
    public static TransactionResponse success(Transaction tx, int totalBalance) {
        return new TransactionResponse(
            tx.transactionId(),
            tx.shopperId(),
            tx.stickersEarned(),
            totalBalance,
            false,
            "Transaction processed successfully. Earned " + tx.stickersEarned() + " sticker(s)."
        );
    }

    public static TransactionResponse duplicate(Transaction tx, int totalBalance) {
        return new TransactionResponse(
            tx.transactionId(),
            tx.shopperId(),
            tx.stickersEarned(),
            totalBalance,
            true,
            "Duplicate transaction. Previously awarded " + tx.stickersEarned() + " sticker(s)."
        );
    }
}
