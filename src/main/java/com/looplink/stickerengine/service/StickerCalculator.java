package com.looplink.stickerengine.service;

import com.looplink.stickerengine.model.TransactionRequest;
import com.looplink.stickerengine.service.rule.StickerRule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Calculates stickers earned based on campaign rules using a rule engine pattern.
 * 
 * Rules are applied in order based on their priority (getOrder()).
 * New rules can be added by implementing StickerRule interface.
 */
@Component
public class StickerCalculator {

    private final List<StickerRule> rules;

    public StickerCalculator(List<StickerRule> rules) {
        this.rules = rules.stream()
            .sorted(Comparator.comparingInt(StickerRule::getOrder))
            .toList();
    }

    public int calculate(TransactionRequest request) {
        int stickers = 0;
        for (StickerRule rule : rules) {
            stickers = rule.apply(request, stickers);
        }
        return stickers;
    }
}
