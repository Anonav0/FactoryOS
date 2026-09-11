package com.factoryos.repository;

import com.factoryos.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p WHERE p.active = true AND i.quantityAvailable <= p.reorderLevel")
    List<Inventory> findLowStockInventory();
}

