package com.inventoryflow.repository;

import com.inventoryflow.model.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsBySku(String sku);

  Optional<Product> findBySku(String sku);

  @Query("select p from Product p where p.quantityInStock < p.reorderLevel")
  List<Product> findLowStock();

  @Query("""
      select p from Product p where
      (coalesce(:q, '') = '' or
       lower(p.sku) like lower(concat('%', :q, '%')) or
       lower(p.name) like lower(concat('%', :q, '%')) or
       lower(p.category) like lower(concat('%', :q, '%')))
      """)
  Page<Product> search(@Param("q") String q, Pageable pageable);
}

