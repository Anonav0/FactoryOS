package com.factoryos.util;

import com.factoryos.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static Product createProduct(String sku, String name, BigDecimal unitPrice, Integer reorderLevel, boolean active) {
        return Product.builder()
                .sku(sku)
                .name(name)
                .category("General")
                .description("Test Description for " + name)
                .unitPrice(unitPrice)
                .reorderLevel(reorderLevel)
                .active(active)
                .build();
    }

    public static Product createProduct(Long id, String sku, String name, BigDecimal unitPrice, Integer reorderLevel, boolean active) {
        Product p = createProduct(sku, name, unitPrice, reorderLevel, active);
        p.setId(id);
        return p;
    }

    public static Supplier createSupplier(String name, String email, boolean active) {
        return Supplier.builder()
                .name(name)
                .contactPerson("Test Contact")
                .email(email)
                .phone("+91-9876543210")
                .address("Industrial Estate, Sector 1")
                .active(active)
                .build();
    }

    public static Supplier createSupplier(Long id, String name, String email, boolean active) {
        Supplier s = createSupplier(name, email, active);
        s.setId(id);
        return s;
    }

    public static Inventory createInventory(Product product, Integer quantityAvailable, Integer reservedQuantity) {
        return Inventory.builder()
                .product(product)
                .quantityAvailable(quantityAvailable)
                .reservedQuantity(reservedQuantity)
                .build();
    }

    public static Inventory createInventory(Long id, Product product, Integer quantityAvailable, Integer reservedQuantity) {
        Inventory inv = createInventory(product, quantityAvailable, reservedQuantity);
        inv.setId(id);
        return inv;
    }

    public static PurchaseOrder createPurchaseOrder(Supplier supplier, String orderNumber, PurchaseOrderStatus status) {
        return PurchaseOrder.builder()
                .supplier(supplier)
                .orderNumber(orderNumber)
                .status(status)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    public static PurchaseOrder createPurchaseOrder(Long id, Supplier supplier, String orderNumber, PurchaseOrderStatus status) {
        PurchaseOrder po = createPurchaseOrder(supplier, orderNumber, status);
        po.setId(id);
        return po;
    }

    public static PurchaseOrderItem createPurchaseOrderItem(PurchaseOrder po, Product product, Integer quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .purchaseOrder(po)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
        if (po.getItems() == null) {
            po.setItems(new ArrayList<>());
        }
        po.getItems().add(item);
        po.setTotalAmount(po.getTotalAmount().add(subtotal));
        return item;
    }

    public static StockMovement createStockMovement(Product product, StockMovementType type, Integer quantity, String reference, String reason) {
        return StockMovement.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .reference(reference)
                .reason(reason)
                .build();
    }
}
