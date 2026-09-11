package com.factoryos.config;

import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.entity.PurchaseOrder;
import com.factoryos.entity.PurchaseOrderItem;
import com.factoryos.entity.PurchaseOrderStatus;
import com.factoryos.entity.StockMovement;
import com.factoryos.entity.StockMovementType;
import com.factoryos.entity.Supplier;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderRepository;
import com.factoryos.repository.StockMovementRepository;
import com.factoryos.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile({"dev", "demo"})
public class DevelopmentDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataInitializer.class);

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public DevelopmentDataInitializer(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (isAlreadyInitialized()) {
            log.info("Demo data already present in database. Skipping initialization.");
            return;
        }

        log.info("Seeding realistic development and demonstration data into FactoryOS...");

        // 1. Seed Industrial Products (8 items)
        Product motor = saveProduct("MOTOR-001", "Industrial Electric Motor",
                "5 HP 3-phase heavy-duty electric induction motor", "Motors",
                new BigDecimal("18500.00"), 10);

        Product bearing = saveProduct("BEARING-001", "Heavy Duty Ball Bearing",
                "Deep groove ball bearing 6205-2RS with rubber seals", "Bearings",
                new BigDecimal("450.00"), 25);

        Product belt = saveProduct("BELT-001", "Reinforced Conveyor Belt",
                "Multi-ply industrial rubber conveyor belt 50m roll", "Belts",
                new BigDecimal("1200.00"), 15);

        Product gear = saveProduct("GEAR-001", "Steel Gear Assembly",
                "Precision spur gear module 4 28 teeth hardened alloy steel", "Mechanical",
                new BigDecimal("3400.00"), 8);

        Product pump = saveProduct("PUMP-001", "Hydraulic Gear Pump",
                "High-pressure external gear pump 250 bar rated for heavy machinery", "Hydraulics",
                new BigDecimal("8200.00"), 5);

        Product valve = saveProduct("VALVE-001", "Industrial Control Valve",
                "2-inch pneumatic actuated globe control valve with positioner", "Valves",
                new BigDecimal("2650.00"), 12);

        Product sensor = saveProduct("SENSOR-001", "RTD Temperature Sensor",
                "Pt100 industrial temperature probe with 4-20mA head transmitter", "Instrumentation",
                new BigDecimal("950.00"), 20);

        Product filter = saveProduct("FILTER-001", "High-Pressure Hydraulic Filter",
                "10-micron spin-on hydraulic return filter cartridge", "Filtration",
                new BigDecimal("380.00"), 30);

        // 2. Seed Suppliers (5 items)
        Supplier supplierAbc = saveSupplier("ABC Industrial Supplies", "Rajesh Kumar",
                "sales@abc-industrial.example", "+91 98765 43210",
                "12 Industrial Estate, Sector 5, Kolkata, West Bengal");

        Supplier supplierEastern = saveSupplier("Eastern Engineering Components", "Ananya Sharma",
                "orders@eastern-eng.example", "+91 98301 22334",
                "45 GT Road, Howrah, West Bengal");

        Supplier supplierMetro = saveSupplier("Metro Machine Parts", "Vikram Singh",
                "info@metromachine.example", "+91 98112 34567",
                "88 Adityapur Industrial Area, Jamshedpur, Jharkhand");

        Supplier supplierNational = saveSupplier("National Hydraulic Systems", "Priya Patel",
                "contact@nationalhydraulics.example", "+91 98220 98765",
                "Plot 14, MIDC Bhosari, Pune, Maharashtra");

        Supplier supplierPrecision = saveSupplier("Precision Automation Ltd.", "Amit Banerjee",
                "sales@precisionauto.example", "+91 98450 11223",
                "7 Electronic City Phase 1, Bengaluru, Karnataka");

        // 3. Seed Inventory & Movements (Healthy, Low-stock, and Zero-stock representation)
        // Healthy: Motor (45), Bearing (120), Belt (35), Sensor (25), Filter (50)
        saveInventoryAndMovement(motor, 45, 0, "GRN-1001", "Initial warehouse stock delivery");
        saveInventoryAndMovement(bearing, 120, 0, "GRN-1002", "Supplier bulk delivery");
        saveInventoryAndMovement(belt, 35, 0, "GRN-1003", "Bulk conveyor stock receipt");
        saveInventoryAndMovement(sensor, 25, 0, "GRN-1004", "Instrumentation shipment");
        saveInventoryAndMovement(filter, 50, 0, "GRN-1005", "Hydraulic consumables receipt");

        // Low stock: Pump (3 <= 5), Valve (8 <= 12)
        saveInventoryAndMovement(pump, 3, 0, "GRN-1006", "Initial batch delivery (remaining after assembly issue)");
        saveInventoryAndMovement(valve, 8, 0, "GRN-1007", "Initial piping batch (remaining after plant fitout)");

        // Zero stock: Gear (0 <= 8)
        saveInventoryAndMovement(gear, 0, 0, "PROD-2005", "Exhausted for emergency plant machinery overhaul");

        // Extra realistic audit movements:
        saveMovement(motor, StockMovementType.STOCK_OUT, 5, "PROD-2001", "Production assembly line issue");
        saveMovement(bearing, StockMovementType.STOCK_OUT, 30, "PROD-2002", "Conveyor maintenance overhaul");
        saveMovement(belt, StockMovementType.ADJUSTMENT, -5, "ADJ-3001", "Cycle count discrepancy reconciliation");

        // 4. Seed Purchase Orders across distinct lifecycle states
        // PO 1: CREATED (Draft state awaiting approval)
        createDemoPurchaseOrder("PO-000001", supplierAbc, PurchaseOrderStatus.CREATED,
                LocalDate.now(), LocalDate.now().plusDays(14),
                List.of(
                        createItem(motor, 5, new BigDecimal("18500.00")),
                        createItem(gear, 8, new BigDecimal("3400.00"))
                ));

        // PO 2: APPROVED (Authorized order awaiting goods receipt)
        createDemoPurchaseOrder("PO-000002", supplierEastern, PurchaseOrderStatus.APPROVED,
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(10),
                List.of(
                        createItem(bearing, 50, new BigDecimal("450.00")),
                        createItem(belt, 10, new BigDecimal("1200.00"))
                ));

        // PO 3: RECEIVED (Completed procurement order with inventory received)
        createDemoPurchaseOrder("PO-000003", supplierMetro, PurchaseOrderStatus.RECEIVED,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(2),
                List.of(
                        createItem(filter, 20, new BigDecimal("380.00")),
                        createItem(sensor, 15, new BigDecimal("950.00"))
                ));

        // PO 4: CANCELLED (Aborted procurement order)
        createDemoPurchaseOrder("PO-000004", supplierPrecision, PurchaseOrderStatus.CANCELLED,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(7),
                List.of(
                        createItem(valve, 5, new BigDecimal("2650.00"))
                ));

        log.info("Demo data initialized successfully: 8 products, 5 suppliers, 8 inventory records, 4 purchase orders across CREATED, APPROVED, RECEIVED, and CANCELLED states.");
    }

    public boolean isAlreadyInitialized() {
        return productRepository.existsBySku("MOTOR-001") ||
                purchaseOrderRepository.existsByOrderNumber("PO-000001");
    }

    private Product saveProduct(String sku, String name, String description, String category,
                                BigDecimal unitPrice, Integer reorderLevel) {
        Product product = Product.builder()
                .sku(sku)
                .name(name)
                .description(description)
                .category(category)
                .unitPrice(unitPrice)
                .reorderLevel(reorderLevel)
                .active(true)
                .build();
        return productRepository.save(product);
    }

    private Supplier saveSupplier(String name, String contactPerson, String email, String phone, String address) {
        Supplier supplier = Supplier.builder()
                .name(name)
                .contactPerson(contactPerson)
                .email(email)
                .phone(phone)
                .address(address)
                .active(true)
                .build();
        return supplierRepository.save(supplier);
    }

    private void saveInventoryAndMovement(Product product, int quantityAvailable, int reservedQuantity,
                                         String reference, String reason) {
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .quantityAvailable(quantityAvailable)
                        .reservedQuantity(reservedQuantity)
                        .build());
        inventory.setQuantityAvailable(quantityAvailable);
        inventory.setReservedQuantity(reservedQuantity);
        inventoryRepository.save(inventory);

        if (quantityAvailable > 0) {
            saveMovement(product, StockMovementType.STOCK_IN, quantityAvailable, reference, reason);
        }
    }

    private void saveMovement(Product product, StockMovementType type, int quantity, String reference, String reason) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .reference(reference)
                .reason(reason)
                .build();
        stockMovementRepository.save(movement);
    }

    private PurchaseOrder createDemoPurchaseOrder(String orderNumber, Supplier supplier,
                                                  PurchaseOrderStatus status, LocalDate orderDate,
                                                  LocalDate expectedDeliveryDate,
                                                  List<PurchaseOrderItem> items) {
        BigDecimal totalAmount = items.stream()
                .map(PurchaseOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseOrder po = PurchaseOrder.builder()
                .orderNumber(orderNumber)
                .supplier(supplier)
                .status(status)
                .orderDate(orderDate)
                .expectedDeliveryDate(expectedDeliveryDate)
                .totalAmount(totalAmount)
                .build();

        for (PurchaseOrderItem item : items) {
            po.addItem(item);
        }

        return purchaseOrderRepository.save(po);
    }

    private PurchaseOrderItem createItem(Product product, Integer quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return PurchaseOrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }
}
