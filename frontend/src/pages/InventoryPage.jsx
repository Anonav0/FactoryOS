import React, { useState, useEffect } from "react";
import { inventoryApi } from "../api/inventory";
import { InventoryTable } from "../components/inventory/InventoryTable";
import { StockInModal } from "../components/inventory/StockInModal";
import { StockOutModal } from "../components/inventory/StockOutModal";
import { StockAdjustModal } from "../components/inventory/StockAdjustModal";
import { MovementHistoryModal } from "../components/inventory/MovementHistoryModal";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import { EmptyState } from "../components/common/EmptyState";
import { Layers, RefreshCw, Filter } from "lucide-react";

export function InventoryPage() {
  const [inventory, setInventory] = useState([]);
  const [filterLowStockOnly, setFilterLowStockOnly] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  // Selected item for modals
  const [stockInItem, setStockInItem] = useState(null);
  const [stockOutItem, setStockOutItem] = useState(null);
  const [adjustItem, setAdjustItem] = useState(null);
  const [historyItem, setHistoryItem] = useState(null);

  const fetchInventory = async () => {
    setIsLoading(true);
    setError("");
    try {
      const data = await inventoryApi.getAll();
      setInventory(data || []);
    } catch (err) {
      setError(err.message || "Failed to load inventory balances.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInventory();
  }, []);

  const displayedInventory = filterLowStockOnly
    ? inventory.filter((item) => item.lowStock)
    : inventory;

  const handleStockActionSuccess = () => {
    fetchInventory();
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Inventory &amp; Stock Ledger</h1>
          <p className="page-description">
            Monitor real-time physical stock levels, execute stock movements,
            and audit chronological transactions.
          </p>
        </div>
        <div className="page-actions">
          <button
            type="button"
            className={`btn ${filterLowStockOnly ? "btn-primary" : "btn-secondary"}`}
            onClick={() => setFilterLowStockOnly((prev) => !prev)}
            title="Filter low-stock products"
          >
            <Filter size={14} />
            <span>
              {filterLowStockOnly
                ? "Showing Low Stock Only"
                : "Filter Low Stock"}
            </span>
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={fetchInventory}
            disabled={isLoading}
            title="Refresh inventory"
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
        <LoadingSpinner label="Loading inventory balances..." />
      ) : inventory.length === 0 ? (
        <EmptyState
          icon={Layers}
          title="No inventory records found."
          description="Create products in the Products tab to automatically initialize their inventory balance."
        />
      ) : displayedInventory.length === 0 && filterLowStockOnly ? (
        <div className="table-card">
          <div className="state-container" style={{ padding: "3rem 1.5rem" }}>
            <h3 className="state-title">No low-stock items detected</h3>
            <p className="state-description">
              All active product inventory levels are currently above their
              respective reorder thresholds.
            </p>
            <div className="state-action">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setFilterLowStockOnly(false)}
              >
                View All Inventory
              </button>
            </div>
          </div>
        </div>
      ) : (
        <InventoryTable
          inventory={displayedInventory}
          onStockIn={(item) => setStockInItem(item)}
          onStockOut={(item) => setStockOutItem(item)}
          onAdjust={(item) => setAdjustItem(item)}
          onViewHistory={(item) => setHistoryItem(item)}
        />
      )}

      {/* Stock In Modal */}
      <StockInModal
        isOpen={Boolean(stockInItem)}
        item={stockInItem}
        onClose={() => setStockInItem(null)}
        onSuccess={handleStockActionSuccess}
      />

      {/* Stock Out Modal */}
      <StockOutModal
        isOpen={Boolean(stockOutItem)}
        item={stockOutItem}
        onClose={() => setStockOutItem(null)}
        onSuccess={handleStockActionSuccess}
      />

      {/* Adjust Stock Modal */}
      <StockAdjustModal
        isOpen={Boolean(adjustItem)}
        item={adjustItem}
        onClose={() => setAdjustItem(null)}
        onSuccess={handleStockActionSuccess}
      />

      {/* Movement History Modal */}
      <MovementHistoryModal
        isOpen={Boolean(historyItem)}
        item={historyItem}
        onClose={() => setHistoryItem(null)}
      />
    </div>
  );
}
