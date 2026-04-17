package com.inventoryflow.controller;

import com.inventoryflow.dto.salesorder.SalesOrderCreateRequest;
import com.inventoryflow.dto.salesorder.SalesOrderResponse;
import com.inventoryflow.dto.salesorder.SalesOrderUpdateRequest;
import com.inventoryflow.service.SalesOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

  private final SalesOrderService salesOrderService;

  public SalesOrderController(SalesOrderService salesOrderService) {
    this.salesOrderService = salesOrderService;
  }

  @PostMapping
  public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderCreateRequest request) {
    SalesOrderResponse created = salesOrderService.createSalesOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/api/sales-orders/" + created.id()))
        .body(created);
  }

  @GetMapping
  public ResponseEntity<List<SalesOrderResponse>> list() {
    return ResponseEntity.ok(salesOrderService.listSalesOrders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<SalesOrderResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(salesOrderService.getSalesOrder(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SalesOrderResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody SalesOrderUpdateRequest request
  ) {
    return ResponseEntity.ok(salesOrderService.updateSalesOrder(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    salesOrderService.deleteSalesOrder(id);
    return ResponseEntity.noContent().build();
  }
}

