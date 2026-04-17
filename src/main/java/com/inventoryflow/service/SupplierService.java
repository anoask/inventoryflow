package com.inventoryflow.service;

import com.inventoryflow.dto.common.PageDto;
import com.inventoryflow.dto.supplier.SupplierCreateRequest;
import com.inventoryflow.dto.supplier.SupplierResponse;
import com.inventoryflow.dto.supplier.SupplierUpdateRequest;
import com.inventoryflow.exception.ConflictException;
import com.inventoryflow.exception.ResourceNotFoundException;
import com.inventoryflow.model.entity.Supplier;
import com.inventoryflow.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

  private final SupplierRepository supplierRepository;

  public SupplierService(SupplierRepository supplierRepository) {
    this.supplierRepository = supplierRepository;
  }

  @Transactional(readOnly = true)
  public PageDto<SupplierResponse> listSuppliers(String search, Pageable pageable) {
    String q = (search == null || search.isBlank()) ? null : search.trim();
    Page<Supplier> page = supplierRepository.search(q, pageable);
    return new PageDto<>(
        page.getContent().stream().map(this::toResponse).toList(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize()
    );
  }

  @Transactional(readOnly = true)
  public SupplierResponse getSupplier(Long id) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    return toResponse(supplier);
  }

  @Transactional
  public SupplierResponse createSupplier(SupplierCreateRequest request) {
    if (supplierRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email already exists: " + request.email());
    }
    Supplier supplier = new Supplier(request.name(), request.email(), request.phone(), request.address());
    supplierRepository.save(supplier);
    return toResponse(supplier);
  }

  @Transactional
  public SupplierResponse updateSupplier(Long id, SupplierUpdateRequest request) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));

    if (!supplier.getEmail().equals(request.email()) && supplierRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email already exists: " + request.email());
    }

    supplier.setName(request.name());
    supplier.setEmail(request.email());
    supplier.setPhone(request.phone());
    supplier.setAddress(request.address());

    return toResponse(supplier);
  }

  @Transactional
  public void deleteSupplier(Long id) {
    if (!supplierRepository.existsById(id)) {
      throw new ResourceNotFoundException("Supplier not found: " + id);
    }
    supplierRepository.deleteById(id);
  }

  private SupplierResponse toResponse(Supplier supplier) {
    return new SupplierResponse(
        supplier.getId(),
        supplier.getName(),
        supplier.getEmail(),
        supplier.getPhone(),
        supplier.getAddress()
    );
  }
}

