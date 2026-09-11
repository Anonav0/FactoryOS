import React from "react";
import { StatusBadge } from "../common/StatusBadge";
import { Edit2, ShieldAlert } from "lucide-react";

export function ProductTable({ products, onEdit, onDeactivate }) {
  const formatCurrency = (val) => {
    if (val === null || val === undefined) return "—";
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(val);
  };

  return (
    <div className="table-card">
      <div className="table-scroll-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: "140px" }}>SKU</th>
              <th>Product Name</th>
              <th>Category</th>
              <th className="text-right" style={{ width: "130px" }}>
                Unit Price
              </th>
              <th className="text-right" style={{ width: "130px" }}>
                Reorder Level
              </th>
              <th style={{ width: "110px" }}>Status</th>
              <th className="text-right" style={{ width: "180px" }}>
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td className="font-mono" style={{ fontWeight: 600 }}>
                  {product.sku}
                </td>
                <td>
                  <div style={{ fontWeight: 600 }}>{product.name}</div>
                  {product.description && (
                    <div
                      style={{
                        fontSize: "0.75rem",
                        color: "var(--text-muted)",
                        maxWidth: "320px",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                      title={product.description}
                    >
                      {product.description}
                    </div>
                  )}
                </td>
                <td>{product.category || "—"}</td>
                <td className="text-right font-mono">
                  {formatCurrency(product.unitPrice)}
                </td>
                <td className="text-right font-mono">{product.reorderLevel}</td>
                <td>
                  <StatusBadge type="entity" active={product.active} />
                </td>
                <td className="text-right">
                  <div
                    className="action-buttons"
                    style={{ justifyContent: "flex-end" }}
                  >
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onEdit(product)}
                      title="Edit product parameters"
                    >
                      <Edit2 size={13} />
                      <span>Edit</span>
                    </button>
                    {product.active && (
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        style={{
                          color: "var(--danger)",
                          borderColor: "var(--danger-border)",
                        }}
                        onClick={() => onDeactivate(product)}
                        title="Deactivate product"
                      >
                        <ShieldAlert size={13} />
                        <span>Deactivate</span>
                      </button>
                    )}
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
