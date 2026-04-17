package com.inventoryflow.service;

import com.inventoryflow.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderItemResponse;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderResponse;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderUpdateRequest;
import com.inventoryflow.exception.ConflictException;
import com.inventoryflow.exception.InvalidOrderStateException;
import com.inventoryflow.exception.ResourceNotFoundException;
import com.inventoryflow.model.entity.Product;
import com.inventoryflow.model.entity.PurchaseOrder;
import com.inventoryflow.model.entity.PurchaseOrderItem;
import com.inventoryflow.model.entity.PurchaseOrderStatus;
import com.inventoryflow.model.entity.Supplier;
import com.inventoryflow.repository.ProductRepository;
import com.inventoryflow.repository.PurchaseOrderRepository;
import com.inventoryflow.repository.SupplierRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderService {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final SupplierRepository supplierRepository;
  private final ProductRepository productRepository;

  public PurchaseOrderService(
      PurchaseOrderRepository purchaseOrderRepository,
      SupplierRepository supplierRepository,
      ProductRepository productRepository
  ) {
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.supplierRepository = supplierRepository;
    this.productRepository = productRepository;
  }

  @Transactional
  public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreateRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));

    PurchaseOrder po = new PurchaseOrder(supplier);

    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));
      BigDecimal unitPrice = itemReq.unitPrice() != null ? itemReq.unitPrice() : product.getPrice();
      po.addItem(new PurchaseOrderItem(product, itemReq.quantity(), unitPrice));
    });

    purchaseOrderRepository.save(po);
    return toResponse(po);
  }

  @Transactional(readOnly = true)
  public List<PurchaseOrderResponse> listPurchaseOrders() {
    return purchaseOrderRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PurchaseOrderResponse getPurchaseOrder(Long id) {
    PurchaseOrder po = purchaseOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    return toResponse(po);
  }

  @Transactional
  public PurchaseOrderResponse updatePurchaseOrder(Long id, PurchaseOrderUpdateRequest request) {
    PurchaseOrder po = purchaseOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));

    if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
      throw new InvalidOrderStateException("Received purchase orders cannot be modified");
    }

    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));

    po.setSupplier(supplier);
    po.getItems().clear();

    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));
      BigDecimal unitPrice = itemReq.unitPrice() != null ? itemReq.unitPrice() : product.getPrice();
      po.addItem(new PurchaseOrderItem(product, itemReq.quantity(), unitPrice));
    });

    return toResponse(po);
  }

  @Transactional
  public void deletePurchaseOrder(Long id) {
    PurchaseOrder po = purchaseOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));

    if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
      throw new InvalidOrderStateException("Received purchase orders cannot be deleted");
    }

    purchaseOrderRepository.delete(po);
  }

  @Transactional
  public PurchaseOrderResponse markReceived(Long id) {
    PurchaseOrder po = purchaseOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));

    if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
      throw new ConflictException("Purchase order is already received");
    }

    for (PurchaseOrderItem item : po.getItems()) {
      Product product = item.getProduct();
      product.increaseStock(item.getQuantity());
      productRepository.save(product);
    }

    po.setStatus(PurchaseOrderStatus.RECEIVED);
    return toResponse(po);
  }

  private PurchaseOrderResponse toResponse(PurchaseOrder po) {
    return new PurchaseOrderResponse(
        po.getId(),
        po.getStatus().name(),
        po.getCreatedAt(),
        po.getSupplier().getId(),
        po.getSupplier().getName(),
        po.getItems().stream().map(this::toItemResponse).toList()
    );
  }

  private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
    return new PurchaseOrderItemResponse(
        item.getProduct().getId(),
        item.getProduct().getSku(),
        item.getProduct().getName(),
        item.getQuantity(),
        item.getUnitPrice()
    );
  }
}

