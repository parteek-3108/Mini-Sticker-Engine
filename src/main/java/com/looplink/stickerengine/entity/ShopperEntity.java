package com.looplink.stickerengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shoppers")
public class ShopperEntity {

    @Id
    @Column(name = "shopper_id", length = 64)
    private String shopperId;

    @Column(name = "total_stickers", nullable = false)
    private int totalStickers = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ShopperEntity() {}

    public ShopperEntity(String shopperId) {
        this.shopperId = shopperId;
        this.totalStickers = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getShopperId() {
        return shopperId;
    }

    public void setShopperId(String shopperId) {
        this.shopperId = shopperId;
    }

    public int getTotalStickers() {
        return totalStickers;
    }

    public void setTotalStickers(int totalStickers) {
        this.totalStickers = totalStickers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
