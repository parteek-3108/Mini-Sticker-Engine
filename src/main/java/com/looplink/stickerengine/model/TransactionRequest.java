package com.looplink.stickerengine.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * Incoming transaction request payload.
 */
public record TransactionRequest(
    @NotBlank(message = "Transaction ID is required")
    String transactionId,

    @NotBlank(message = "Shopper ID is required")
    String shopperId,

    @NotBlank(message = "Store ID is required")
    String storeId,

    @NotNull(message = "Timestamp is required")
    Instant timestamp,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<Item> items
) {}
