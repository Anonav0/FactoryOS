import React from "react";
import { StatusBadge } from "../common/StatusBadge";
import { Edit2, ShieldAlert, Mail, Phone } from "lucide-react";

export function SupplierTable({ suppliers, onEdit, onDeactivate }) {
  return (
    <div className="table-card">
      <div className="table-scroll-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Supplier Name</th>
              <th>Contact Person</th>
              <th>Email</th>
              <th>Phone</th>
              <th style={{ width: "110px" }}>Status</th>
              <th className="text-right" style={{ width: "180px" }}>
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {suppliers.map((supplier) => (
              <tr key={supplier.id}>
                <td>
                  <div style={{ fontWeight: 600 }}>{supplier.name}</div>
                  {supplier.address && (
                    <div
                      style={{
                        fontSize: "0.75rem",
                        color: "var(--text-muted)",
                        maxWidth: "280px",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                      title={supplier.address}
                    >
                      {supplier.address}
                    </div>
                  )}
                </td>
                <td>{supplier.contactPerson || "—"}</td>
                <td>
                  {supplier.email ? (
                    <div
                      style={{
                        display: "inline-flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <Mail size={12} color="var(--text-muted)" />
                      <span className="font-mono">{supplier.email}</span>
                    </div>
                  ) : (
                    "—"
                  )}
                </td>
                <td>
                  {supplier.phone ? (
                    <div
                      style={{
                        display: "inline-flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <Phone size={12} color="var(--text-muted)" />
                      <span className="font-mono">{supplier.phone}</span>
                    </div>
                  ) : (
                    "—"
                  )}
                </td>
                <td>
                  <StatusBadge type="entity" active={supplier.active} />
                </td>
                <td className="text-right">
                  <div
                    className="action-buttons"
                    style={{ justifyContent: "flex-end" }}
                  >
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onEdit(supplier)}
                      title="Edit supplier parameters"
                    >
                      <Edit2 size={13} />
                      <span>Edit</span>
                    </button>
                    {supplier.active && (
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        style={{
                          color: "var(--danger)",
                          borderColor: "var(--danger-border)",
                        }}
                        onClick={() => onDeactivate(supplier)}
                        title="Deactivate supplier"
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
