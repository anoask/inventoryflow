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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PurchaseOrderStatus status = PurchaseOrderStatus.CREATED;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "supplier_id", nullable = false)
  private Supplier supplier;

  @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PurchaseOrderItem> items = new ArrayList<>();

  public PurchaseOrder() {}

  public PurchaseOrder(Supplier supplier) {
    this.supplier = supplier;
  }

  public Long getId() {
    return id;
  }

  public PurchaseOrderStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public List<PurchaseOrderItem> getItems() {
    return items;
  }

  public void setStatus(PurchaseOrderStatus status) {
    this.status = status;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public void addItem(PurchaseOrderItem item) {
    items.add(item);
    item.setPurchaseOrder(this);
  }
}

