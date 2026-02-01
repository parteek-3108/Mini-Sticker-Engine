package com.looplink.stickerengine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", length = 64, unique = true, nullable = false)
    private String transactionId;

    @Column(name = "shopper_id", length = 64, nullable = false)
    private String shopperId;

    @Column(name = "store_id", length = 64, nullable = false)
    private String storeId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "stickers_earned", nullable = false)
    private int stickersEarned = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItemEntity> items = new ArrayList<>();

    public TransactionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getShopperId() {
        return shopperId;
    }

    public void setShopperId(String shopperId) {
        this.shopperId = shopperId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getStickersEarned() {
        return stickersEarned;
    }

    public void setStickersEarned(int stickersEarned) {
        this.stickersEarned = stickersEarned;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<TransactionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<TransactionItemEntity> items) {
        this.items = items;
    }

    public void addItem(TransactionItemEntity item) {
        items.add(item);
        item.setTransaction(this);
    }
}
