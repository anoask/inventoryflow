package com.inventoryflow.repository;

import com.inventoryflow.model.entity.SalesOrder;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

  @EntityGraph(attributePaths = {"items"})
  List<SalesOrder> findTop5ByOrderByCreatedAtDesc();
}

