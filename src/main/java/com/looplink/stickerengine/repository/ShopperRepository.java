package com.looplink.stickerengine.repository;

import com.looplink.stickerengine.entity.ShopperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for shopper sticker balances.
 */
@Repository
public interface ShopperRepository extends JpaRepository<ShopperEntity, String> {

    Optional<ShopperEntity> findByShopperId(String shopperId);
}
