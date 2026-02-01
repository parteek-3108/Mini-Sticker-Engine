package com.looplink.stickerengine.service.rule;

import com.looplink.stickerengine.model.TransactionRequest;

/**
 * Interface for sticker calculation rules.
 * Each rule applies specific logic to calculate stickers earned.
 */
public interface StickerRule {

    /**
     * Applies this rule to calculate stickers.
     * @param request the transaction request
     * @param currentStickers the stickers accumulated from previous rules
     * @return the updated sticker count after applying this rule
     */
    int apply(TransactionRequest request, int currentStickers);

    /**
     * Returns the priority/order of this rule. Lower values execute first.
     */
    int getOrder();
}
