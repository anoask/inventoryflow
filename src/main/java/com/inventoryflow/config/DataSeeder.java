package com.inventoryflow.config;

import com.inventoryflow.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.inventoryflow.dto.purchaseorder.PurchaseOrderItemCreateRequest;
import com.inventoryflow.dto.salesorder.SalesOrderCreateRequest;
import com.inventoryflow.dto.salesorder.SalesOrderItemCreateRequest;
import com.inventoryflow.model.entity.Product;
import com.inventoryflow.model.entity.Role;
import com.inventoryflow.model.entity.RoleName;
import com.inventoryflow.model.entity.Supplier;
import com.inventoryflow.repository.ProductRepository;
import com.inventoryflow.repository.RoleRepository;
import com.inventoryflow.repository.SupplierRepository;
import com.inventoryflow.repository.UserRepository;
import com.inventoryflow.service.PurchaseOrderService;
import com.inventoryflow.service.SalesOrderService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "inventoryflow", name = "seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final SupplierRepository supplierRepository;
  private final ProductRepository productRepository;

  private final PasswordEncoder passwordEncoder;
  private final PurchaseOrderService purchaseOrderService;
  private final SalesOrderService salesOrderService;

  public DataSeeder(
      RoleRepository roleRepository,
      UserRepository userRepository,
      SupplierRepository supplierRepository,
      ProductRepository productRepository,
      PasswordEncoder passwordEncoder,
      PurchaseOrderService purchaseOrderService,
      SalesOrderService salesOrderService
  ) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.supplierRepository = supplierRepository;
    this.productRepository = productRepository;
    this.passwordEncoder = passwordEncoder;
    this.purchaseOrderService = purchaseOrderService;
    this.salesOrderService = salesOrderService;
  }

  @Override
  @Transactional
  public void run(String... args) {
    seedRolesAndUsers();
    seedSuppliersAndProducts();
  }

  private void seedRolesAndUsers() {
    if (roleRepository.count() == 0) {
      roleRepository.save(new Role(RoleName.ADMIN.name()));
      roleRepository.save(new Role(RoleName.STAFF.name()));
    }

    if (userRepository.count() == 0) {
      Role adminRole = roleRepository.findByName(RoleName.ADMIN.name()).orElseThrow();
      Role staffRole = roleRepository.findByName(RoleName.STAFF.name()).orElseThrow();

      var admin = new com.inventoryflow.model.entity.User(
          "admin@inventoryflow.test",
          "admin",
          passwordEncoder.encode("admin123")
      );
      admin.addRole(adminRole);
      userRepository.save(admin);

      var staff = new com.inventoryflow.model.entity.User(
          "staff@inventoryflow.test",
          "staff",
          passwordEncoder.encode("staff123")
      );
      staff.addRole(staffRole);
      userRepository.save(staff);
    }
  }

  private void seedSuppliersAndProducts() {
    if (supplierRepository.count() == 0) {
      supplierRepository.save(new Supplier(
          "Northwind Supplies",
          "northwind@example.com",
          "+1-555-0100",
          "100 Market Street"
      ));
      supplierRepository.save(new Supplier(
          "Acme Components",
          "acme@example.com",
          "+1-555-0200",
          "200 Industrial Ave"
      ));
    }

    if (productRepository.count() == 0) {
      var suppliers = supplierRepository.findAll();
      if (suppliers.size() < 2) {
        throw new IllegalStateException("At least 2 suppliers are required for sample data");
      }
      Supplier supplier1 = suppliers.get(0);
      Supplier supplier2 = suppliers.get(1);

      Product p1 = new Product(
          "WID-001",
          "Widget",
          "Standard widget",
          "Widgets",
          new BigDecimal("19.99"),
          5,
          10,
          supplier1
      );
      Product p2 = new Product(
          "GAD-002",
          "Gadget",
          "Useful gadget",
          "Gadgets",
          new BigDecimal("9.49"),
          6,
          10,
          supplier1
      );
      Product p3 = new Product(
          "CAB-003",
          "Cable",
          "Charging cable",
          "Accessories",
          new BigDecimal("4.99"),
          25,
          10,
          supplier2
      );

      productRepository.saveAll(List.of(p1, p2, p3));

      // Demonstrate: Receiving a purchase order increases stock
      PurchaseOrderCreateRequest poRequest = new PurchaseOrderCreateRequest(
          supplier1.getId(),
          List.of(new PurchaseOrderItemCreateRequest(p1.getId(), 8, null))
      );
      var po = purchaseOrderService.createPurchaseOrder(poRequest);
      purchaseOrderService.markReceived(po.id());

      // Demonstrate: Creating a sales order reduces stock and enforces insufficient-stock checks
      SalesOrderCreateRequest soRequest = new SalesOrderCreateRequest(
          List.of(new SalesOrderItemCreateRequest(p2.getId(), 1))
      );
      salesOrderService.createSalesOrder(soRequest);
    }
  }
}

