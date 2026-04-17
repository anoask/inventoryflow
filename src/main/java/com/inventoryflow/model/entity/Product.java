package com.inventoryflow.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String sku;

  @Column(nullable = false, length = 160)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private int quantityInStock;

  @Column(nullable = false)
  private int reorderLevel;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "supplier_id", nullable = false)
  private Supplier supplier;

  public Product() {}

  public Product(String sku, String name, String description, String category, BigDecimal price, int quantityInStock, int reorderLevel, Supplier supplier) {
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.category = category;
    this.price = price;
    this.quantityInStock = quantityInStock;
    this.reorderLevel = reorderLevel;
    this.supplier = supplier;
  }

  public Long getId() {
    return id;
  }

  public String getSku() {
    return sku;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public int getQuantityInStock() {
    return quantityInStock;
  }

  public int getReorderLevel() {
    return reorderLevel;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setQuantityInStock(int quantityInStock) {
    this.quantityInStock = quantityInStock;
  }

  public void setReorderLevel(int reorderLevel) {
    this.reorderLevel = reorderLevel;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public void increaseStock(int amount) {
    this.quantityInStock += amount;
  }

  public void decreaseStock(int amount) {
    this.quantityInStock -= amount;
  }
}

