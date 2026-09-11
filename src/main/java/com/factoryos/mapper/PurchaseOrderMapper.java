package com.factoryos.mapper;

import com.factoryos.dto.PurchaseOrderItemResponse;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;
import com.factoryos.entity.PurchaseOrder;
import com.factoryos.entity.PurchaseOrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        if (item == null) {
            return null;
        }
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getSku() : null,
                item.getProduct() != null ? item.getProduct().getName() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    public List<PurchaseOrderItemResponse> toItemResponseList(List<PurchaseOrderItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toItemResponse)
                .toList();
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder po) {
        if (po == null) {
            return null;
        }
        return new PurchaseOrderResponse(
                po.getId(),
                po.getOrderNumber(),
                po.getSupplier() != null ? po.getSupplier().getId() : null,
                po.getSupplier() != null ? po.getSupplier().getName() : null,
                po.getStatus(),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getTotalAmount(),
                toItemResponseList(po.getItems()),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }

    public PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder po) {
        if (po == null) {
            return null;
        }
        return new PurchaseOrderSummaryResponse(
                po.getId(),
                po.getOrderNumber(),
                po.getSupplier() != null ? po.getSupplier().getId() : null,
                po.getSupplier() != null ? po.getSupplier().getName() : null,
                po.getStatus(),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getItems() != null ? po.getItems().size() : 0,
                po.getTotalAmount(),
                po.getCreatedAt()
        );
    }
}

