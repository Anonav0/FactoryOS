import React from "react";
import { Eye, CheckCircle, PackageCheck, Ban } from "lucide-react";

export function PurchaseOrderTable({
  orders,
  onViewDetails,
  onApprove,
  onReceive,
  onCancel,
}) {
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

  const renderStatusBadge = (status) => {
    switch (status) {
      case "CREATED":
        return (
          <span
            className="badge"
            style={{
              backgroundColor: "#eff6ff",
              color: "#1e40af",
              border: "1px solid #bfdbfe",
            }}
          >
            <span style={{ fontSize: "10px" }}>●</span> CREATED
          </span>
        );
      case "APPROVED":
        return (
          <span className="badge badge-lowstock">
            <span style={{ fontSize: "10px" }}>●</span> APPROVED
          </span>
        );
      case "RECEIVED":
        return (
          <span className="badge badge-instock">
            <span style={{ fontSize: "10px" }}>●</span> RECEIVED
          </span>
        );
      case "CANCELLED":
        return (
          <span className="badge badge-inactive">
            <span style={{ fontSize: "10px" }}>○</span> CANCELLED
          </span>
        );
      default:
        return <span className="badge">{status}</span>;
    }
  };

  return (
    <div className="table-card">
      <div className="table-scroll-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: "130px" }}>Order #</th>
              <th>Supplier</th>
              <th style={{ width: "120px" }}>Order Date</th>
              <th style={{ width: "140px" }}>Expected Delivery</th>
              <th className="text-right" style={{ width: "90px" }}>
                Items
              </th>
              <th className="text-right" style={{ width: "130px" }}>
                Total Amount
              </th>
              <th style={{ width: "120px" }}>Status</th>
              <th className="text-right" style={{ width: "260px" }}>
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {orders.map((po) => (
              <tr key={po.id}>
                <td
                  className="font-mono"
                  style={{ fontWeight: 600, color: "var(--primary)" }}
                >
                  {po.orderNumber}
                </td>
                <td>
                  <div style={{ fontWeight: 600 }}>{po.supplierName}</div>
                </td>
                <td style={{ fontSize: "0.8125rem" }}>
                  {formatDate(po.orderDate)}
                </td>
                <td
                  style={{
                    fontSize: "0.8125rem",
                    color: po.expectedDeliveryDate
                      ? "inherit"
                      : "var(--text-muted)",
                  }}
                >
                  {formatDate(po.expectedDeliveryDate)}
                </td>
                <td
                  className="text-right font-mono"
                  style={{ fontWeight: 500 }}
                >
                  {po.itemCount} {po.itemCount === 1 ? "item" : "items"}
                </td>
                <td
                  className="text-right font-mono"
                  style={{ fontWeight: 700 }}
                >
                  {formatCurrency(po.totalAmount)}
                </td>
                <td>{renderStatusBadge(po.status)}</td>
                <td className="text-right">
                  <div
                    className="action-buttons"
                    style={{ justifyContent: "flex-end" }}
                  >
                    {/* CREATED state actions */}
                    {po.status === "CREATED" && (
                      <>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          style={{
                            color: "var(--primary)",
                            borderColor: "var(--primary-border)",
                          }}
                          onClick={() => onApprove(po)}
                          title="Approve purchase order"
                        >
                          <CheckCircle size={13} />
                          <span>Approve</span>
                        </button>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          style={{
                            color: "var(--danger)",
                            borderColor: "var(--danger-border)",
                          }}
                          onClick={() => onCancel(po)}
                          title="Cancel purchase order"
                        >
                          <Ban size={13} />
                          <span>Cancel</span>
                        </button>
                      </>
                    )}

                    {/* APPROVED state actions */}
                    {po.status === "APPROVED" && (
                      <>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          style={{
                            color: "var(--success)",
                            borderColor: "var(--success-border)",
                          }}
                          onClick={() => onReceive(po)}
                          title="Receive goods into inventory"
                        >
                          <PackageCheck size={13} />
                          <span>Receive</span>
                        </button>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          style={{
                            color: "var(--danger)",
                            borderColor: "var(--danger-border)",
                          }}
                          onClick={() => onCancel(po)}
                          title="Cancel purchase order"
                        >
                          <Ban size={13} />
                          <span>Cancel</span>
                        </button>
                      </>
                    )}

                    {/* All states can view details */}
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => onViewDetails(po)}
                      title="View order line items and details"
                    >
                      <Eye size={13} />
                      <span>Details</span>
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
