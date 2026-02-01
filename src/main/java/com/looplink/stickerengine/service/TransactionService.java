package com.looplink.stickerengine.service;

import com.looplink.stickerengine.entity.ShopperEntity;
import com.looplink.stickerengine.entity.TransactionEntity;
import com.looplink.stickerengine.entity.TransactionItemEntity;
import com.looplink.stickerengine.model.*;
import com.looplink.stickerengine.repository.ShopperRepository;
import com.looplink.stickerengine.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Core service for processing transactions and managing shopper stickers.
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String SHOPPER_LOCK_PREFIX = "shopper:";

    private final TransactionRepository transactionRepository;
    private final ShopperRepository shopperRepository;
    private final StickerCalculator stickerCalculator;
    private final IdempotencyService idempotencyService;
    private final DistributedLockService lockService;

    public TransactionService(
            TransactionRepository transactionRepository,
            ShopperRepository shopperRepository,
            StickerCalculator stickerCalculator,
            IdempotencyService idempotencyService,
            DistributedLockService lockService) {
        this.transactionRepository = transactionRepository;
        this.shopperRepository = shopperRepository;
        this.stickerCalculator = stickerCalculator;
        this.idempotencyService = idempotencyService;
        this.lockService = lockService;
    }

    /**
     * Processes a transaction request.
     * Uses Redis for idempotency check and distributed locking to prevent race conditions.
     */
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        String txId = request.transactionId();
        String shopperId = request.shopperId();

        if (!idempotencyService.tryAcquire(txId)) {
            log.info("Duplicate transaction detected: txId={}", txId);
            return handleDuplicateTransaction(txId);
        }

        String lockToken = lockService.tryLock(SHOPPER_LOCK_PREFIX + shopperId);
        if (lockToken == null) {
            log.warn("Failed to acquire lock for shopperId={}, txId={}", shopperId, txId);
            idempotencyService.release(txId);
            throw new DistributedLockService.LockAcquisitionException(
                "Failed to acquire lock for shopper: " + shopperId);
        }
        log.debug("Acquired lock for shopperId={}", shopperId);

        try {
            Optional<TransactionEntity> existing = transactionRepository.findByTransactionId(txId);
            if (existing.isPresent()) {
                log.info("Transaction already exists in DB: txId={}", txId);
                idempotencyService.markCompleted(txId);
                return handleDuplicateTransaction(txId);
            }

            int stickersEarned = stickerCalculator.calculate(request);
            log.debug("Calculated stickers: txId={}, stickersEarned={}", txId, stickersEarned);
            Transaction transaction = Transaction.from(request, stickersEarned);

            ShopperEntity shopper = shopperRepository.findByShopperId(shopperId)
                .orElseGet(() -> {
                    ShopperEntity newShopper = new ShopperEntity(shopperId);
                    return shopperRepository.save(newShopper);
                });

            TransactionEntity txEntity = toEntity(transaction);
            transactionRepository.save(txEntity);

            shopper.setTotalStickers(shopper.getTotalStickers() + stickersEarned);
            shopperRepository.save(shopper);

            idempotencyService.markCompleted(txId);
            log.info("Transaction completed: txId={}, shopperId={}, stickersEarned={}, newBalance={}",
                    txId, shopperId, stickersEarned, shopper.getTotalStickers());
            return TransactionResponse.success(transaction, shopper.getTotalStickers());
        } catch (Exception e) {
            log.error("Transaction failed: txId={}, shopperId={}, error={}", txId, shopperId, e.getMessage());
            idempotencyService.release(txId);
            throw e;
        } finally {
            lockService.unlock(SHOPPER_LOCK_PREFIX + shopperId, lockToken);
            log.debug("Released lock for shopperId={}", shopperId);
        }
    }

    private TransactionResponse handleDuplicateTransaction(String txId) {
        Optional<TransactionEntity> existing = transactionRepository.findByTransactionId(txId);
        if (existing.isPresent()) {
            TransactionEntity txEntity = existing.get();
            Transaction tx = toTransaction(txEntity);
            int currentBalance = shopperRepository.findByShopperId(tx.shopperId())
                .map(ShopperEntity::getTotalStickers)
                .orElse(0);
            return TransactionResponse.duplicate(tx, currentBalance);
        }
        throw new IllegalStateException("Transaction marked as duplicate but not found in DB: " + txId);
    }

    /**
     * Gets a shopper's sticker status including balance and transaction history.
     */
    @Transactional(readOnly = true)
    public Optional<ShopperStatus> getShopperStatus(String shopperId) {
        return shopperRepository.findByShopperId(shopperId)
            .map(shopper -> {
                List<TransactionEntity> txEntities = transactionRepository.findByShopperId(shopperId);
                List<ShopperStatus.TransactionSummary> summaries = txEntities.stream()
                    .map(this::toTransaction)
                    .map(ShopperStatus.TransactionSummary::from)
                    .toList();
                return new ShopperStatus(shopperId, shopper.getTotalStickers(), summaries);
            });
    }

    private TransactionEntity toEntity(Transaction tx) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(tx.transactionId());
        entity.setShopperId(tx.shopperId());
        entity.setStoreId(tx.storeId());
        entity.setTimestamp(tx.timestamp());
        entity.setTotalAmount(tx.totalAmount());
        entity.setStickersEarned(tx.stickersEarned());

        for (Item item : tx.items()) {
            TransactionItemEntity itemEntity = new TransactionItemEntity();
            itemEntity.setSku(item.sku());
            itemEntity.setName(item.name());
            itemEntity.setQuantity(item.quantity());
            itemEntity.setUnitPrice(item.unitPrice());
            itemEntity.setCategory(item.category());
            entity.addItem(itemEntity);
        }

        return entity;
    }

    private Transaction toTransaction(TransactionEntity entity) {
        List<Item> items = entity.getItems().stream()
            .map(itemEntity -> new Item(
                itemEntity.getSku(),
                itemEntity.getName(),
                itemEntity.getQuantity(),
                itemEntity.getUnitPrice(),
                itemEntity.getCategory()
            ))
            .toList();

        return new Transaction(
            entity.getTransactionId(),
            entity.getShopperId(),
            entity.getStoreId(),
            entity.getTimestamp(),
            items,
            entity.getTotalAmount(),
            entity.getStickersEarned()
        );
    }
}
