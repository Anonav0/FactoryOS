import React, { useState, useEffect } from "react";
import { productsApi } from "../api/products";
import { suppliersApi } from "../api/suppliers";
import { inventoryApi } from "../api/inventory";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import {
  Package,
  CheckCircle,
  Truck,
  AlertTriangle,
  RefreshCw,
  Layers,
} from "lucide-react";

export function DashboardPage({ onNavigate }) {
  const [metrics, setMetrics] = useState({
    totalProducts: 0,
    activeProducts: 0,
    totalSuppliers: 0,
    lowStockCount: 0,
  });
  const [lowStockItems, setLowStockItems] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchDashboardData = async () => {
    setIsLoading(true);
    setError("");
    try {
      const [products, suppliers, lowStock] = await Promise.all([
        productsApi.getAll(),
        suppliersApi.getAll(),
        inventoryApi.getLowStock(),
      ]);

      const active = (products || []).filter((p) => p.active).length;
      const lowList = lowStock || [];

      setMetrics({
        totalProducts: (products || []).length,
        activeProducts: active,
        totalSuppliers: (suppliers || []).length,
        lowStockCount: lowList.length,
      });

      setLowStockItems(lowList);
    } catch (err) {
      setError(err.message || "Failed to load dashboard statistics.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Operations Dashboard</h1>
          <p className="page-description">
            Real-time status overview of catalog, suppliers, and factory
            inventory balances.
          </p>
        </div>
        <div className="page-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={fetchDashboardData}
            disabled={isLoading}
            title="Refresh dashboard metrics"
          >
            <RefreshCw size={14} className={isLoading ? "spinner" : ""} />
            <span>Refresh</span>
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
        <LoadingSpinner label="Loading dashboard metrics..." />
      ) : (
        <>
          {/* KPI Summary Cards */}
          <div className="metrics-grid">
            <div className="metric-card">
              <div
                className="metric-label"
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <Package size={14} color="var(--primary)" />
                <span>Total Products</span>
              </div>
              <div className="metric-value">{metrics.totalProducts}</div>
              <div className="metric-subtitle">Configured in catalog</div>
            </div>

            <div className="metric-card">
              <div
                className="metric-label"
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <CheckCircle size={14} color="var(--success)" />
                <span>Active Products</span>
              </div>
              <div className="metric-value font-mono">
                {metrics.activeProducts}
              </div>
              <div className="metric-subtitle">Available for operations</div>
            </div>

            <div className="metric-card">
              <div
                className="metric-label"
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <Truck size={14} color="var(--text-secondary)" />
                <span>Suppliers</span>
              </div>
              <div className="metric-value">{metrics.totalSuppliers}</div>
              <div className="metric-subtitle">Active supply partners</div>
            </div>

            <div
              className={`metric-card ${metrics.lowStockCount > 0 ? "warning-border" : ""}`}
            >
              <div
                className="metric-label"
                style={{ display: "flex", alignItems: "center", gap: "6px" }}
              >
                <AlertTriangle
                  size={14}
                  color={
                    metrics.lowStockCount > 0
                      ? "var(--warning)"
                      : "var(--success)"
                  }
                />
                <span>Low Stock Items</span>
              </div>
              <div
                className="metric-value font-mono"
                style={{
                  color:
                    metrics.lowStockCount > 0
                      ? "var(--warning-text)"
                      : "inherit",
                }}
              >
                {metrics.lowStockCount}
              </div>
              <div className="metric-subtitle">
                {metrics.lowStockCount > 0
                  ? "Require immediate replenishment"
                  : "All stock levels healthy"}
              </div>
            </div>
          </div>

          {/* Low Stock Items Section */}
          <div className="table-card">
            <div className="table-card-header">
              <div
                style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}
              >
                <AlertTriangle size={18} color="var(--warning)" />
                <h2 className="table-title">Low Stock Items</h2>
              </div>
              {metrics.lowStockCount > 0 && onNavigate && (
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => onNavigate("inventory")}
                >
                  <Layers size={13} />
                  <span>Go to Inventory</span>
                </button>
              )}
            </div>

            {lowStockItems.length === 0 ? (
              <div
                className="state-container"
                style={{ padding: "2.5rem 1rem" }}
              >
                <div
                  style={{ color: "var(--success)", marginBottom: "0.5rem" }}
                >
                  <CheckCircle size={32} />
                </div>
                <h3 className="state-title">
                  All inventory levels are healthy.
                </h3>
                <p className="state-description">
                  No active products are currently at or below their configured
                  reorder thresholds.
                </p>
              </div>
            ) : (
              <div className="table-scroll-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th style={{ width: "150px" }}>SKU</th>
                      <th>Product</th>
                      <th className="text-right" style={{ width: "140px" }}>
                        Available
                      </th>
                      <th className="text-right" style={{ width: "140px" }}>
                        Reorder Level
                      </th>
                      <th className="text-right" style={{ width: "140px" }}>
                        Shortage
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {lowStockItems.map((item) => {
                      const shortage = Math.max(
                        0,
                        item.reorderLevel - item.quantityAvailable,
                      );
                      return (
                        <tr key={item.productId}>
                          <td className="font-mono" style={{ fontWeight: 600 }}>
                            {item.sku}
                          </td>
                          <td style={{ fontWeight: 600 }}>
                            {item.productName}
                          </td>
                          <td
                            className="text-right font-mono"
                            style={{
                              fontWeight: 700,
                              color: "var(--warning-text)",
                            }}
                          >
                            {item.quantityAvailable}
                          </td>
                          <td
                            className="text-right font-mono"
                            style={{ color: "var(--text-muted)" }}
                          >
                            {item.reorderLevel}
                          </td>
                          <td
                            className="text-right font-mono"
                            style={{ color: "var(--danger)", fontWeight: 600 }}
                          >
                            {shortage > 0 ? `-${shortage}` : "At Reorder"}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
