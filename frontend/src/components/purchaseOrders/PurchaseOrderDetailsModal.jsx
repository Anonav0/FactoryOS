import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { purchaseOrdersApi } from "../../api/purchaseOrders";
import { LoadingSpinner } from "../common/LoadingSpinner";

export function PurchaseOrderDetailsModal({ isOpen, onClose, orderId }) {
  const [orderDetails, setOrderDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isOpen && orderId) {
      loadDetails(orderId);
    } else {
      setOrderDetails(null);
      setError("");
    }
  }, [isOpen, orderId]);

  const loadDetails = async (id) => {
    setIsLoading(true);
    setError("");
    try {
      const data = await purchaseOrdersApi.getById(id);
      setOrderDetails(data);
    } catch (err) {
      setError(err.message || "Failed to load purchase order details.");
    } finally {
      setIsLoading(false);
    }
  };

  const formatCurrency = (val) => {
    if (val === null || val === undefined) return "—";
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(val);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      const [year, month, day] = dateStr.split("-");
      const date = new Date(year, month - 1, day);
      return new Intl.DateTimeFormat("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }).format(date);
    } catch {
      return dateStr;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={
        orderDetails
          ? `Purchase Order ${orderDetails.orderNumber}`
          : "Purchase Order Details"
      }
      wide
    >
      <div className="modal-body">
        {isLoading ? (
          <LoadingSpinner label="Loading purchase order details..." />
        ) : error ? (
          <div
            style={{
              padding: "1rem",
              backgroundColor: "var(--danger-bg)",
              color: "var(--danger-text)",
              borderRadius: "var(--radius-sm)",
              fontSize: "0.875rem",
            }}
          >
            {error}
          </div>
        ) : orderDetails ? (
          <>
            {/* Header info */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(4, 1fr)",
                gap: "1rem",
                backgroundColor: "var(--bg-surface-subtle)",
                padding: "1rem 1.25rem",
                borderRadius: "var(--radius-sm)",
                border: "1px solid var(--border-subtle)",
                marginBottom: "1.5rem",
              }}
            >
              <div>
                <div
                  style={{
                    fontSize: "0.75rem",
                    color: "var(--text-muted)",
                    textTransform: "uppercase",
                  }}
                >
                  Supplier
                </div>
                <div
                  style={{
                    fontWeight: 600,
                    fontSize: "0.9375rem",
                    marginTop: "2px",
                  }}
                >
                  {orderDetails.supplierName}
                </div>
              </div>

              <div>
                <div
                  style={{
                    fontSize: "0.75rem",
                    color: "var(--text-muted)",
                    textTransform: "uppercase",
                  }}
                >
                  Status
                </div>
                <div style={{ marginTop: "2px" }}>
                  <span
                    className="badge"
                    style={{
                      backgroundColor: "#eff6ff",
                      color: "#1e40af",
                      border: "1px solid #bfdbfe",
                    }}
                  >
                    ● {orderDetails.status}
                  </span>
                </div>
              </div>

              <div>
                <div
                  style={{
                    fontSize: "0.75rem",
                    color: "var(--text-muted)",
                    textTransform: "uppercase",
                  }}
                >
                  Order Date
                </div>
                <div
                  style={{
                    fontWeight: 500,
                    fontSize: "0.875rem",
                    marginTop: "2px",
                  }}
                >
                  {formatDate(orderDetails.orderDate)}
                </div>
              </div>

              <div>
                <div
                  style={{
                    fontSize: "0.75rem",
                    color: "var(--text-muted)",
                    textTransform: "uppercase",
                  }}
                >
                  Expected Delivery
                </div>
                <div
                  style={{
                    fontWeight: 500,
                    fontSize: "0.875rem",
                    marginTop: "2px",
                  }}
                >
                  {formatDate(orderDetails.expectedDeliveryDate)}
                </div>
              </div>
            </div>

            {/* Items Table */}
            <div style={{ marginBottom: "1.5rem" }}>
              <h3
                style={{
                  fontSize: "0.9375rem",
                  fontWeight: 600,
                  marginBottom: "0.75rem",
                }}
              >
                Line Items ({orderDetails.items?.length || 0})
              </h3>
              <div
                className="table-card"
                style={{ border: "1px solid var(--border-subtle)" }}
              >
                <div className="table-scroll-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th style={{ width: "130px" }}>SKU</th>
                        <th>Product</th>
                        <th className="text-right" style={{ width: "100px" }}>
                          Qty
                        </th>
                        <th className="text-right" style={{ width: "130px" }}>
                          Unit Price
                        </th>
                        <th className="text-right" style={{ width: "140px" }}>
                          Subtotal
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {orderDetails.items?.map((item) => (
                        <tr key={item.id}>
                          <td className="font-mono" style={{ fontWeight: 600 }}>
                            {item.sku}
                          </td>
                          <td style={{ fontWeight: 500 }}>
                            {item.productName}
                          </td>
                          <td className="text-right font-mono">
                            {item.quantity}
                          </td>
                          <td className="text-right font-mono">
                            {formatCurrency(item.unitPrice)}
                          </td>
                          <td
                            className="text-right font-mono"
                            style={{ fontWeight: 600 }}
                          >
                            {formatCurrency(item.subtotal)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            {/* Total Callout */}
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                alignItems: "baseline",
                gap: "1rem",
                padding: "1rem 1.25rem",
                backgroundColor: "var(--bg-surface-subtle)",
                borderRadius: "var(--radius-sm)",
                border: "1px solid var(--border-subtle)",
              }}
            >
              <span
                style={{
                  fontSize: "0.9375rem",
                  fontWeight: 600,
                  color: "var(--text-secondary)",
                }}
              >
                Total Order Amount:
              </span>
              <span
                className="font-mono"
                style={{
                  fontSize: "1.5rem",
                  fontWeight: 700,
                  color: "var(--primary)",
                }}
              >
                {formatCurrency(orderDetails.totalAmount)}
              </span>
            </div>
          </>
        ) : null}
      </div>

      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onClose}>
          Close
        </button>
      </div>
    </Modal>
  );
}
