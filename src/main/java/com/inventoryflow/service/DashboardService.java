package com.inventoryflow.service;

import com.inventoryflow.dto.dashboard.LowStockProductResponse;
import com.inventoryflow.dto.dashboard.RecentPurchaseOrderDto;
import com.inventoryflow.dto.dashboard.RecentSalesOrderDto;
import com.inventoryflow.model.entity.Product;
import com.inventoryflow.model.entity.PurchaseOrder;
import com.inventoryflow.model.entity.SalesOrder;
import com.inventoryflow.repository.ProductRepository;
import com.inventoryflow.repository.PurchaseOrderRepository;
import com.inventoryflow.repository.SalesOrderRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private static final int RECENT_LIMIT = 5;

  private final ProductRepository productRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final SalesOrderRepository salesOrderRepository;

  public DashboardService(
      ProductRepository productRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      SalesOrderRepository salesOrderRepository
  ) {
    this.productRepository = productRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.salesOrderRepository = salesOrderRepository;
  }

  @Transactional(readOnly = true)
  public List<LowStockProductResponse> lowStockProducts() {
    return productRepository.findLowStock().stream().map(this::toLowStockResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<RecentPurchaseOrderDto> recentPurchaseOrders() {
    var pageable = PageRequest.of(0, RECENT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
    return purchaseOrderRepository.findAll(pageable).getContent().stream()
        .map(this::toRecentPurchase)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RecentSalesOrderDto> recentSalesOrders() {
    var pageable = PageRequest.of(0, RECENT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
    return salesOrderRepository.findAll(pageable).getContent().stream()
        .map(this::toRecentSales)
        .toList();
  }

  private RecentPurchaseOrderDto toRecentPurchase(PurchaseOrder po) {
    return new RecentPurchaseOrderDto(
        po.getId(),
        po.getStatus().name(),
        po.getCreatedAt(),
        po.getSupplier().getName(),
        po.getItems().size()
    );
  }

  private RecentSalesOrderDto toRecentSales(SalesOrder so) {
    return new RecentSalesOrderDto(
        so.getId(),
        so.getStatus().name(),
        so.getCreatedAt(),
        so.getItems().size()
    );
  }

  private LowStockProductResponse toLowStockResponse(Product p) {
    return new LowStockProductResponse(
        p.getId(),
        p.getSku(),
        p.getName(),
        p.getCategory(),
        p.getPrice(),
        p.getQuantityInStock(),
        p.getReorderLevel(),
        p.getSupplier().getId(),
        p.getSupplier().getName()
    );
  }
}
