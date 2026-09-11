import React, { useState, useEffect } from "react";
import { purchaseOrdersApi } from "../api/purchaseOrders";
import { PurchaseOrderTable } from "../components/purchaseOrders/PurchaseOrderTable";
import { CreatePurchaseOrderModal } from "../components/purchaseOrders/CreatePurchaseOrderModal";
import { PurchaseOrderDetailsModal } from "../components/purchaseOrders/PurchaseOrderDetailsModal";
import { ConfirmModal } from "../components/common/ConfirmModal";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import { EmptyState } from "../components/common/EmptyState";
import { useToast } from "../context/ToastContext";
import { ShoppingCart, Plus, RefreshCw } from "lucide-react";

export function PurchaseOrdersPage() {
  const toast = useToast();
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

  // Workflow confirmation state
  const [receiveConfirmOrder, setReceiveConfirmOrder] = useState(null);
  const [cancelConfirmOrder, setCancelConfirmOrder] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);

  const fetchOrders = async () => {
    setIsLoading(true);
    setError("");
    try {
      const data = await purchaseOrdersApi.getAll();
      setOrders(data || []);
    } catch (err) {
      setError(err.message || "Failed to load purchase orders.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCreateSuccess = () => {
    fetchOrders();
  };

  const handleApprove = async (po) => {
    try {
      await purchaseOrdersApi.approve(po.id);
      toast.success(`Purchase order ${po.orderNumber} approved successfully.`);
      await fetchOrders();
    } catch (err) {
      toast.error(
        err.message || `Failed to approve purchase order ${po.orderNumber}.`,
      );
    }
  };

  const handleConfirmReceive = async () => {
    if (!receiveConfirmOrder) return;
    setIsProcessing(true);
    try {
      await purchaseOrdersApi.receive(receiveConfirmOrder.id);
      toast.success(
        `Purchase order ${receiveConfirmOrder.orderNumber} received successfully. Inventory has been updated.`,
      );
      setReceiveConfirmOrder(null);
      await fetchOrders();
    } catch (err) {
      toast.error(
        err.message ||
          `Failed to receive purchase order ${receiveConfirmOrder.orderNumber}.`,
      );
    } finally {
      setIsProcessing(false);
    }
  };

  const handleConfirmCancel = async () => {
    if (!cancelConfirmOrder) return;
    setIsProcessing(true);
    try {
      await purchaseOrdersApi.cancel(cancelConfirmOrder.id);
      toast.info(
        `Purchase order ${cancelConfirmOrder.orderNumber} has been cancelled.`,
      );
      setCancelConfirmOrder(null);
      await fetchOrders();
    } catch (err) {
      toast.error(
        err.message ||
          `Failed to cancel purchase order ${cancelConfirmOrder.orderNumber}.`,
      );
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Purchase Orders</h1>
          <p className="page-description">
            Procure parts and raw materials from authorized industrial suppliers
            with itemized cost tracking.
          </p>
        </div>
        <div className="page-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={fetchOrders}
            disabled={isLoading}
            title="Refresh purchase orders"
          >
            <RefreshCw size={14} className={isLoading ? "spinner" : ""} />
            <span>Refresh</span>
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setIsCreateOpen(true)}
          >
            <Plus size={16} />
            <span>Create Purchase Order</span>
          </button>
        </div>
      </div>

      {error && (
        <div
          style={{
            padding: "1rem",
            backgroundColor: "var(--danger-bg)",
            color: "var(--danger-text)",
            borderRadius: "var(--radius-sm)",
            marginBottom: "1.5rem",
            fontSize: "0.875rem",
          }}
        >
          {error}
        </div>
      )}

      {isLoading ? (
        <LoadingSpinner label="Loading purchase orders..." />
      ) : orders.length === 0 ? (
        <EmptyState
          icon={ShoppingCart}
          title="No purchase orders found."
          description="Create a purchase order to initiate procurement with your registered suppliers."
          actionLabel="Create Purchase Order"
          onAction={() => setIsCreateOpen(true)}
        />
      ) : (
        <PurchaseOrderTable
          orders={orders}
          onViewDetails={(po) => setSelectedOrderId(po.id)}
          onApprove={handleApprove}
          onReceive={(po) => setReceiveConfirmOrder(po)}
          onCancel={(po) => setCancelConfirmOrder(po)}
        />
      )}

      {/* Create PO Modal */}
      <CreatePurchaseOrderModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      {/* Details PO Modal */}
      <PurchaseOrderDetailsModal
        isOpen={Boolean(selectedOrderId)}
        onClose={() => setSelectedOrderId(null)}
        orderId={selectedOrderId}
      />

      {/* Receive Confirmation Modal */}
      <ConfirmModal
        isOpen={Boolean(receiveConfirmOrder)}
        onClose={() => !isProcessing && setReceiveConfirmOrder(null)}
        onConfirm={handleConfirmReceive}
        title="Receive Purchase Order?"
        message={`Receiving this order (${receiveConfirmOrder?.orderNumber}) will add all ordered quantities to inventory and create stock movements. This action cannot be undone.`}
        confirmText="Receive Goods"
        confirmVariant="primary"
        isLoading={isProcessing}
      />

      {/* Cancel Confirmation Modal */}
      <ConfirmModal
        isOpen={Boolean(cancelConfirmOrder)}
        onClose={() => !isProcessing && setCancelConfirmOrder(null)}
        onConfirm={handleConfirmCancel}
        title="Cancel Purchase Order?"
        message={`This purchase order (${cancelConfirmOrder?.orderNumber}) will be marked as cancelled and cannot be processed further.`}
        confirmText="Cancel Order"
        confirmVariant="danger"
        isLoading={isProcessing}
      />
    </div>
  );
}
