package com.factoryos.service;

import com.factoryos.dto.CreatePurchaseOrderRequest;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request);

    List<PurchaseOrderSummaryResponse> getAllPurchaseOrders();

    PurchaseOrderResponse getPurchaseOrderById(Long id);

    PurchaseOrderResponse getPurchaseOrderByOrderNumber(String orderNumber);

    PurchaseOrderResponse approvePurchaseOrder(Long id);

    PurchaseOrderResponse receivePurchaseOrder(Long id);

    PurchaseOrderResponse cancelPurchaseOrder(Long id);
}

