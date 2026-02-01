package com.looplink.stickerengine.controller;

import com.looplink.stickerengine.model.ShopperStatus;
import com.looplink.stickerengine.model.TransactionRequest;
import com.looplink.stickerengine.model.TransactionResponse;
import com.looplink.stickerengine.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transaction and shopper endpoints.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /api/transactions
     * Submit a purchase transaction to earn stickers.
     */
    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> submitTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Received transaction request: txId={}, shopperId={}, storeId={}",
                request.transactionId(), request.shopperId(), request.storeId());
        TransactionResponse response = transactionService.processTransaction(request);
        log.info("Transaction processed: txId={}, stickersEarned={}",
                request.transactionId(),  response.stickersEarned());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/shoppers/{shopperId}
     * Get a shopper's sticker balance and transaction history.
     */
    @GetMapping("/shoppers/{shopperId}")
    public ResponseEntity<ShopperStatus> getShopperStatus(@PathVariable String shopperId) {
        log.info("Fetching status for shopperId={}", shopperId);
        return transactionService.getShopperStatus(shopperId)
            .map(status -> {
                log.info("Found shopper: shopperId={}, totalStickers={}", shopperId, status.totalStickers());
                return ResponseEntity.ok(status);
            })
            .orElseGet(() -> {
                log.warn("Shopper not found: shopperId={}", shopperId);
                return ResponseEntity.notFound().build();
            });
    }
}
