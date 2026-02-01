package com.looplink.stickerengine.service.rule;

import com.looplink.stickerengine.model.TransactionRequest;
import org.springframework.stereotype.Component;

/**
 * Per-transaction cap rule: Maximum 5 stickers per transaction.
 * This rule should run last to cap the total stickers.
 */
@Component
public class MaxCapRule implements StickerRule {

    private static final int MAX_STICKERS_PER_TRANSACTION = 5;

    @Override
    public int apply(TransactionRequest request, int currentStickers) {
        return Math.min(currentStickers, MAX_STICKERS_PER_TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
