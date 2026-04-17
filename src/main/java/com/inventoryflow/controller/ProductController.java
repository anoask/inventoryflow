package com.inventoryflow.controller;

import com.inventoryflow.dto.common.PageDto;
import com.inventoryflow.dto.product.ProductCreateRequest;
import com.inventoryflow.dto.product.ProductResponse;
import com.inventoryflow.dto.product.ProductUpdateRequest;
import com.inventoryflow.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public ResponseEntity<PageDto<ProductResponse>> list(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20, sort = "sku") Pageable pageable
  ) {
    return ResponseEntity.ok(productService.listProducts(search, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProduct(id));
  }

  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
    ProductResponse created = productService.createProduct(request);
    return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ProductUpdateRequest request
  ) {
    return ResponseEntity.ok(productService.updateProduct(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}

