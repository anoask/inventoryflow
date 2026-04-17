package com.inventoryflow.repository;

import com.inventoryflow.model.entity.Supplier;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

  Optional<Supplier> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query("""
      select s from Supplier s where
      (coalesce(:q, '') = '' or lower(s.name) like lower(concat('%', :q, '%')))
      """)
  Page<Supplier> search(@Param("q") String q, Pageable pageable);
}

