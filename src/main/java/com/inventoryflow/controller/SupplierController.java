package com.inventoryflow.controller;

import com.inventoryflow.dto.common.PageDto;
import com.inventoryflow.dto.supplier.SupplierCreateRequest;
import com.inventoryflow.dto.supplier.SupplierResponse;
import com.inventoryflow.dto.supplier.SupplierUpdateRequest;
import com.inventoryflow.service.SupplierService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/suppliers")
public class SupplierController {

  private final SupplierService supplierService;

  public SupplierController(SupplierService supplierService) {
    this.supplierService = supplierService;
  }

  @GetMapping
  public ResponseEntity<PageDto<SupplierResponse>> list(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20, sort = "name") Pageable pageable
  ) {
    return ResponseEntity.ok(supplierService.listSuppliers(search, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<SupplierResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(supplierService.getSupplier(id));
  }

  @PostMapping
  public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
    SupplierResponse created = supplierService.createSupplier(request);
    return ResponseEntity.created(URI.create("/api/suppliers/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SupplierResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody SupplierUpdateRequest request
  ) {
    return ResponseEntity.ok(supplierService.updateSupplier(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    supplierService.deleteSupplier(id);
    return ResponseEntity.noContent().build();
  }
}

