import React from "react";
import { StatusBadge } from "../common/StatusBadge";
import { PlusCircle, MinusCircle, Sliders, History } from "lucide-react";

export function InventoryTable({
  inventory,
  onStockIn,
  onStockOut,
  onAdjust,
  onViewHistory,
}) {
  return (
    <div className="table-card">
      <div className="table-scroll-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: "130px" }}>SKU</th>
              <th>Product Name</th>
              <th className="text-right" style={{ width: "130px" }}>
                Available
              </th>
              <th className="text-right" style={{ width: "130px" }}>
                Reorder Level
              </th>
              <th style={{ width: "130px" }}>Stock Status</th>
              <th className="text-right" style={{ width: "360px" }}>
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {inventory.map((item) => (
              <tr key={item.id || item.productId}>
                <td className="font-mono" style={{ fontWeight: 600 }}>
                  {item.sku}
                </td>
                <td>
                  <div style={{ fontWeight: 600 }}>{item.productName}</div>
                </td>
                <td
                  className="text-right font-mono"
                  style={{
                    fontWeight: 700,
                    fontSize: "0.9375rem",
                    color: item.lowStock ? "var(--warning-text)" : "inherit",
                  }}
                >
                  {item.quantityAvailable}
                </td>
                <td
                  className="text-right font-mono"
                  style={{ color: "var(--text-muted)" }}
                >
                  {item.reorderLevel !== undefined ? item.reorderLevel : "—"}
                </td>
                <td>
                  <StatusBadge type="stock" lowStock={item.lowStock} />
                </td>
                <td className="text-right">
                  <div
                    className="action-buttons"
                    style={{ justifyContent: "flex-end" }}
                  >
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onStockIn(item)}
                      title="Receive stock"
                    >
                      <PlusCircle size={13} color="var(--success)" />
                      <span>Stock In</span>
                    </button>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onStockOut(item)}
                      title="Issue stock"
                    >
                      <MinusCircle size={13} color="var(--danger)" />
                      <span>Stock Out</span>
                    </button>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onAdjust(item)}
                      title="Adjust stock balance"
                    >
                      <Sliders size={13} color="var(--text-secondary)" />
                      <span>Adjust</span>
                    </button>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onViewHistory(item)}
                      title="View movement history"
                    >
                      <History size={13} color="var(--text-muted)" />
                      <span>History</span>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
