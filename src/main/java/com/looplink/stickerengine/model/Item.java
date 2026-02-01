package com.looplink.stickerengine.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Represents an item in a purchase transaction.
 */
public record Item(
    @NotBlank(message = "SKU is required")
    String sku,

    @NotBlank(message = "Item name is required")
    String name,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    BigDecimal unitPrice,

    @NotBlank(message = "Category is required")
    String category
) {
    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isPromo() {
        return "promo".equalsIgnoreCase(category);
    }
}
