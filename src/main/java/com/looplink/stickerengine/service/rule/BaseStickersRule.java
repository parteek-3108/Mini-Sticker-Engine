package com.looplink.stickerengine.service.rule;

import com.looplink.stickerengine.model.Item;
import com.looplink.stickerengine.model.TransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Base earn rate rule: 1 sticker per $10 of total basket spend.
 * Uses floor division (e.g., $19 → 1 sticker, $21 → 2 stickers).
 */
@Component
public class BaseStickersRule implements StickerRule {

    private static final BigDecimal BASE_RATE_THRESHOLD = BigDecimal.valueOf(10);

    @Override
    public int apply(TransactionRequest request, int currentStickers) {
        BigDecimal totalSpend = request.items().stream()
            .map(Item::totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int baseStickers = totalSpend.divide(BASE_RATE_THRESHOLD, 0, RoundingMode.FLOOR).intValue();
        return currentStickers + baseStickers;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
