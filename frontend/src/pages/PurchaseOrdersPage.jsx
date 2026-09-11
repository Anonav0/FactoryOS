import React, { useState, useEffect } from "react";
import { purchaseOrdersApi } from "../api/purchaseOrders";
import { PurchaseOrderTable } from "../components/purchaseOrders/PurchaseOrderTable";
import { CreatePurchaseOrderModal } from "../components/purchaseOrders/CreatePurchaseOrderModal";
import { PurchaseOrderDetailsModal } from "../components/purchaseOrders/PurchaseOrderDetailsModal";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import { EmptyState } from "../components/common/EmptyState";
import { ShoppingCart, Plus, RefreshCw } from "lucide-react";

export function PurchaseOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

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
    </div>
  );
}
