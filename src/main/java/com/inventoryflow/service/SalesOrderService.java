package com.inventoryflow.service;

import com.inventoryflow.dto.salesorder.SalesOrderCreateRequest;
import com.inventoryflow.dto.salesorder.SalesOrderItemResponse;
import com.inventoryflow.dto.salesorder.SalesOrderResponse;
import com.inventoryflow.dto.salesorder.SalesOrderUpdateRequest;
import com.inventoryflow.exception.InsufficientStockException;
import com.inventoryflow.exception.InvalidOrderStateException;
import com.inventoryflow.exception.ResourceNotFoundException;
import com.inventoryflow.model.entity.Product;
import com.inventoryflow.model.entity.SalesOrder;
import com.inventoryflow.model.entity.SalesOrderItem;
import com.inventoryflow.model.entity.SalesOrderStatus;
import com.inventoryflow.repository.ProductRepository;
import com.inventoryflow.repository.SalesOrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesOrderService {

  private final SalesOrderRepository salesOrderRepository;
  private final ProductRepository productRepository;

  public SalesOrderService(SalesOrderRepository salesOrderRepository, ProductRepository productRepository) {
    this.salesOrderRepository = salesOrderRepository;
    this.productRepository = productRepository;
  }

  @Transactional
  public SalesOrderResponse createSalesOrder(SalesOrderCreateRequest request) {
    // Validate stock first to avoid partially applied changes.
    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));
      if (product.getQuantityInStock() < itemReq.quantity()) {
        throw new InsufficientStockException(
            "Insufficient stock for product " + product.getSku() + ". Requested=" + itemReq.quantity() +
                ", Available=" + product.getQuantityInStock()
        );
      }
    });

    SalesOrder so = new SalesOrder();
    so.setStatus(SalesOrderStatus.CREATED);

    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

      product.decreaseStock(itemReq.quantity());
      SalesOrderItem item = new SalesOrderItem(product, itemReq.quantity(), product.getPrice());
      so.addItem(item);
      productRepository.save(product);
    });

    salesOrderRepository.save(so);
    return toResponse(so);
  }

  @Transactional(readOnly = true)
  public List<SalesOrderResponse> listSalesOrders() {
    return salesOrderRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public SalesOrderResponse getSalesOrder(Long id) {
    SalesOrder so = salesOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sales order not found: " + id));
    return toResponse(so);
  }

  @Transactional
  public SalesOrderResponse updateSalesOrder(Long id, SalesOrderUpdateRequest request) {
    SalesOrder so = salesOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sales order not found: " + id));

    if (so.getStatus() != SalesOrderStatus.CREATED) {
      throw new InvalidOrderStateException("Only CREATED sales orders can be modified");
    }

    // Reverse current stock allocations.
    for (SalesOrderItem existingItem : so.getItems()) {
      Product product = existingItem.getProduct();
      product.increaseStock(existingItem.getQuantity());
      productRepository.save(product);
    }

    // Remove current items.
    so.getItems().clear();

    // Validate new stock allocation.
    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));
      if (product.getQuantityInStock() < itemReq.quantity()) {
        throw new InsufficientStockException(
            "Insufficient stock for product " + product.getSku() + ". Requested=" + itemReq.quantity() +
                ", Available=" + product.getQuantityInStock()
        );
      }
    });

    // Apply new stock allocations.
    request.items().forEach(itemReq -> {
      Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

      product.decreaseStock(itemReq.quantity());
      SalesOrderItem item = new SalesOrderItem(product, itemReq.quantity(), product.getPrice());
      so.addItem(item);
      productRepository.save(product);
    });

    salesOrderRepository.save(so);
    return toResponse(so);
  }

  @Transactional
  public void deleteSalesOrder(Long id) {
    SalesOrder so = salesOrderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Sales order not found: " + id));

    if (so.getStatus() != SalesOrderStatus.CREATED) {
      throw new InvalidOrderStateException("Only CREATED sales orders can be deleted");
    }

    // Reverse stock allocations.
    for (SalesOrderItem existingItem : so.getItems()) {
      Product product = existingItem.getProduct();
      product.increaseStock(existingItem.getQuantity());
      productRepository.save(product);
    }

    salesOrderRepository.delete(so);
  }

  private SalesOrderResponse toResponse(SalesOrder so) {
    return new SalesOrderResponse(
        so.getId(),
        so.getStatus().name(),
        so.getCreatedAt(),
        so.getItems().stream().map(this::toItemResponse).toList()
    );
  }

  private SalesOrderItemResponse toItemResponse(SalesOrderItem item) {
    return new SalesOrderItemResponse(
        item.getProduct().getId(),
        item.getProduct().getSku(),
        item.getProduct().getName(),
        item.getQuantity(),
        item.getUnitPrice()
    );
  }
}

