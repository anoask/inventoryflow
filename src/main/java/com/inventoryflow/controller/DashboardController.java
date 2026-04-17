package com.inventoryflow.controller;

import com.inventoryflow.dto.dashboard.LowStockProductResponse;
import com.inventoryflow.dto.dashboard.RecentPurchaseOrderDto;
import com.inventoryflow.dto.dashboard.RecentSalesOrderDto;
import com.inventoryflow.service.DashboardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/low-stock")
  public ResponseEntity<List<LowStockProductResponse>> lowStock() {
    return ResponseEntity.ok(dashboardService.lowStockProducts());
  }

  @GetMapping("/recent-purchase-orders")
  public ResponseEntity<List<RecentPurchaseOrderDto>> recentPurchaseOrders() {
    return ResponseEntity.ok(dashboardService.recentPurchaseOrders());
  }

  @GetMapping("/recent-sales-orders")
  public ResponseEntity<List<RecentSalesOrderDto>> recentSalesOrders() {
    return ResponseEntity.ok(dashboardService.recentSalesOrders());
  }
}

