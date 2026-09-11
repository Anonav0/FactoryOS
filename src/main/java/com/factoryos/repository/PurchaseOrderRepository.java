package com.factoryos.repository;

import com.factoryos.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(MAX(po.id), 0) FROM PurchaseOrder po")
    Long getMaxId();
}

