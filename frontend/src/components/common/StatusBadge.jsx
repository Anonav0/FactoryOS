import React from "react";

export function StatusBadge({ type, active, lowStock }) {
  if (type === "entity") {
    return active ? (
      <span className="badge badge-active">
        <span style={{ fontSize: "10px" }}>●</span> Active
      </span>
    ) : (
      <span className="badge badge-inactive">
        <span style={{ fontSize: "10px" }}>○</span> Inactive
      </span>
    );
  }

  if (type === "stock") {
    return lowStock ? (
      <span
        className="badge badge-lowstock"
        role="status"
        aria-label="Low Stock Alert"
      >
        <span style={{ fontSize: "10px" }}>●</span> Low Stock
      </span>
    ) : (
      <span className="badge badge-instock" role="status" aria-label="In Stock">
        <span style={{ fontSize: "10px" }}>●</span> In Stock
      </span>
    );
  }

  return null;
}
