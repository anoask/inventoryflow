package com.inventoryflow.service;

import com.inventoryflow.dto.common.PageDto;
import com.inventoryflow.dto.product.ProductCreateRequest;
import com.inventoryflow.dto.product.ProductResponse;
import com.inventoryflow.dto.product.ProductUpdateRequest;
import com.inventoryflow.exception.ConflictException;
import com.inventoryflow.exception.ResourceNotFoundException;
import com.inventoryflow.model.entity.Product;
import com.inventoryflow.model.entity.Supplier;
import com.inventoryflow.repository.ProductRepository;
import com.inventoryflow.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final SupplierRepository supplierRepository;

  public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
    this.productRepository = productRepository;
    this.supplierRepository = supplierRepository;
  }

  @Transactional(readOnly = true)
  public PageDto<ProductResponse> listProducts(String search, Pageable pageable) {
    String q = (search == null || search.isBlank()) ? null : search.trim();
    Page<Product> page = productRepository.search(q, pageable);
    return new PageDto<>(
        page.getContent().stream().map(this::toResponse).toList(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize()
    );
  }

  @Transactional(readOnly = true)
  public ProductResponse getProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    return toResponse(product);
  }

  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    if (productRepository.existsBySku(request.sku())) {
      throw new ConflictException("SKU already exists: " + request.sku());
    }

    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));

    Product product = new Product(
        request.sku(),
        request.name(),
        request.description(),
        request.category(),
        request.price(),
        request.quantityInStock(),
        request.reorderLevel(),
        supplier
    );

    productRepository.save(product);
    return toResponse(product);
  }

  @Transactional
  public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

    if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
      throw new ConflictException("SKU already exists: " + request.sku());
    }

    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));

    product.setSku(request.sku());
    product.setName(request.name());
    product.setDescription(request.description());
    product.setCategory(request.category());
    product.setPrice(request.price());
    product.setQuantityInStock(request.quantityInStock());
    product.setReorderLevel(request.reorderLevel());
    product.setSupplier(supplier);

    return toResponse(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found: " + id);
    }
    productRepository.deleteById(id);
  }

  private ProductResponse toResponse(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getSku(),
        product.getName(),
        product.getDescription(),
        product.getCategory(),
        product.getPrice(),
        product.getQuantityInStock(),
        product.getReorderLevel(),
        product.getSupplier().getId(),
        product.getSupplier().getName()
    );
  }
}

