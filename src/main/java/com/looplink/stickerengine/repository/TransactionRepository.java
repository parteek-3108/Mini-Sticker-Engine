package com.looplink.stickerengine.repository;

import com.looplink.stickerengine.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for transactions.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    List<TransactionEntity> findByShopperId(String shopperId);

    boolean existsByTransactionId(String transactionId);
}
