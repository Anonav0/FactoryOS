import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { inventoryApi } from "../../api/inventory";
import { LoadingSpinner } from "../common/LoadingSpinner";
import { EmptyState } from "../common/EmptyState";
import {
  History,
  ArrowDownLeft,
  ArrowUpRight,
  SlidersHorizontal,
} from "lucide-react";

export function MovementHistoryModal({ isOpen, onClose, item }) {
  const [movements, setMovements] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isOpen && item) {
      loadMovements(item.productId);
    } else {
      setMovements([]);
      setError("");
    }
  }, [isOpen, item]);

  const loadMovements = async (productId) => {
    setIsLoading(true);
    setError("");
    try {
      const data = await inventoryApi.getMovements(productId);
      setMovements(data || []);
    } catch (err) {
      setError(err.message || "Failed to load stock movements history.");
    } finally {
      setIsLoading(false);
    }
  };

  if (!item) return null;

  const formatDate = (isoStr) => {
    if (!isoStr) return "—";
    try {
      const date = new Date(isoStr);
      return new Intl.DateTimeFormat("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(date);
    } catch {
      return isoStr;
    }
  };

  const renderTypeBadge = (type) => {
    switch (type) {
      case "STOCK_IN":
        return (
          <span className="badge badge-movement-in">
            <ArrowDownLeft size={12} />
            <span>STOCK_IN</span>
          </span>
        );
      case "STOCK_OUT":
        return (
          <span className="badge badge-movement-out">
            <ArrowUpRight size={12} />
            <span>STOCK_OUT</span>
          </span>
        );
      case "ADJUSTMENT":
        return (
          <span className="badge badge-movement-adjust">
            <SlidersHorizontal size={12} />
            <span>ADJUSTMENT</span>
          </span>
        );
      default:
        return <span className="badge">{type}</span>;
    }
  };

  const renderQuantity = (movement) => {
    if (movement.movementType === "STOCK_IN") {
      return (
        <span
          className="font-mono"
          style={{ color: "var(--success-text)", fontWeight: 600 }}
        >
          +{movement.quantity}
        </span>
      );
    }
    if (movement.movementType === "STOCK_OUT") {
      return (
        <span
          className="font-mono"
          style={{ color: "var(--danger)", fontWeight: 600 }}
        >
          -{movement.quantity}
        </span>
      );
    }
    // ADJUSTMENT
    const qty = movement.quantity;
    const isPositive = qty > 0;
    return (
      <span
        className="font-mono"
        style={{
          color: isPositive
            ? "var(--success-text)"
            : qty < 0
              ? "var(--danger)"
              : "var(--text-secondary)",
          fontWeight: 600,
        }}
      >
        {isPositive ? `+${qty}` : qty}
      </span>
    );
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Stock Movement History"
      wide
    >
      <div className="modal-body">
        <div className="context-box">
          <div>
            <div className="context-box-label">Product</div>
            <div className="context-box-value">{item.productName}</div>
          </div>
          <div style={{ textAlign: "right" }}>
            <div className="context-box-label">SKU / Current Stock</div>
            <div className="context-box-value font-mono">
              {item.sku} &middot; {item.quantityAvailable} units
            </div>
          </div>
        </div>

        {isLoading ? (
          <LoadingSpinner label="Loading movement ledger..." />
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
        ) : movements.length === 0 ? (
          <EmptyState
            icon={History}
            title="No Movements Recorded"
            description="No inventory transactions have been performed for this product yet."
          />
        ) : (
          <div className="table-scroll-wrapper" style={{ maxHeight: "420px" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: "160px" }}>Date &amp; Time</th>
                  <th style={{ width: "130px" }}>Type</th>
                  <th className="text-right" style={{ width: "100px" }}>
                    Quantity
                  </th>
                  <th style={{ width: "140px" }}>Reference</th>
                  <th>Reason / Notes</th>
                </tr>
              </thead>
              <tbody>
                {movements.map((m) => (
                  <tr key={m.id}>
                    <td style={{ fontSize: "0.8125rem", whiteSpace: "nowrap" }}>
                      {formatDate(m.createdAt)}
                    </td>
                    <td>{renderTypeBadge(m.movementType)}</td>
                    <td className="text-right">{renderQuantity(m)}</td>
                    <td className="font-mono" style={{ fontSize: "0.8125rem" }}>
                      {m.reference || "—"}
                    </td>
                    <td
                      style={{
                        fontSize: "0.8125rem",
                        color: "var(--text-secondary)",
                      }}
                    >
                      {m.reason || "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onClose}>
          Close
        </button>
      </div>
    </Modal>
  );
}
