package com.inventoryflow.repository;

import com.inventoryflow.model.entity.PurchaseOrder;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

  @EntityGraph(attributePaths = {"supplier", "items"})
  List<PurchaseOrder> findTop5ByOrderByCreatedAtDesc();
}

