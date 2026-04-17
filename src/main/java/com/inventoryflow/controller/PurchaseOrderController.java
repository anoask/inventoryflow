package com.inventoryflow.controller;

import com.inventoryflow.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderResponse;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderUpdateRequest;
import com.inventoryflow.service.PurchaseOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

  private final PurchaseOrderService purchaseOrderService;

  public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
    this.purchaseOrderService = purchaseOrderService;
  }

  @PostMapping
  public ResponseEntity<PurchaseOrderResponse> create(
      @Valid @RequestBody PurchaseOrderCreateRequest request
  ) {
    PurchaseOrderResponse created = purchaseOrderService.createPurchaseOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(URI.create("/api/purchase-orders/" + created.id()))
        .body(created);
  }

  @GetMapping
  public ResponseEntity<List<PurchaseOrderResponse>> list() {
    return ResponseEntity.ok(purchaseOrderService.listPurchaseOrders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PurchaseOrderResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(purchaseOrderService.getPurchaseOrder(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PurchaseOrderResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody PurchaseOrderUpdateRequest request
  ) {
    return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrder(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    purchaseOrderService.deletePurchaseOrder(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/receive")
  public ResponseEntity<PurchaseOrderResponse> receive(@PathVariable Long id) {
    return ResponseEntity.ok(purchaseOrderService.markReceived(id));
  }
}

