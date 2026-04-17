package com.inventoryflow.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private SalesOrderStatus status = SalesOrderStatus.CREATED;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SalesOrderItem> items = new ArrayList<>();

  public SalesOrder() {}

  public Long getId() {
    return id;
  }

  public SalesOrderStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<SalesOrderItem> getItems() {
    return items;
  }

  public void setStatus(SalesOrderStatus status) {
    this.status = status;
  }

  public void addItem(SalesOrderItem item) {
    items.add(item);
    item.setSalesOrder(this);
  }
}

