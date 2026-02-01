package com.looplink.stickerengine.service.rule;

import com.looplink.stickerengine.model.Item;
import com.looplink.stickerengine.model.TransactionRequest;
import org.springframework.stereotype.Component;

/**
 * Promo item bonus rule: +1 extra sticker per unit of items with category "promo".
 */
@Component
public class PromoBonusRule implements StickerRule {

    @Override
    public int apply(TransactionRequest request, int currentStickers) {
        int promoBonus = request.items().stream()
            .filter(Item::isPromo)
            .mapToInt(Item::quantity)
            .sum();

        return currentStickers + promoBonus;
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
