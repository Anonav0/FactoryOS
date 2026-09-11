import React from "react";
import {
  LayoutDashboard,
  Package,
  Truck,
  Layers,
  ShoppingCart,
} from "lucide-react";

const NAV_ITEMS = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "products", label: "Products", icon: Package },
  { id: "suppliers", label: "Suppliers", icon: Truck },
  { id: "inventory", label: "Inventory", icon: Layers },
  { id: "purchase-orders", label: "Purchase Orders", icon: ShoppingCart },
];

export function Sidebar({ currentTab, onSelectTab }) {
  return (
    <aside className="sidebar" aria-label="Main Navigation">
      <div className="brand-section">
        <div className="brand-logo" aria-hidden="true">
          F
        </div>
        <div className="brand-info">
          <span className="brand-title">FactoryOS</span>
          <span className="brand-subtitle">Operations Portal</span>
        </div>
      </div>

      <nav className="nav-section">
        <ul className="nav-list">
          {NAV_ITEMS.map(({ id, label, icon: Icon }) => {
            const isActive = currentTab === id;
            return (
              <li key={id}>
                <button
                  type="button"
                  className={`nav-item-btn ${isActive ? "active" : ""}`}
                  onClick={() => onSelectTab(id)}
                  aria-current={isActive ? "page" : undefined}
                >
                  <Icon size={18} />
                  <span>{label}</span>
                </button>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <span>FactoryOS v1.0</span>
        <span
          style={{ display: "inline-flex", alignItems: "center", gap: "4px" }}
        >
          <span className="status-dot" />
          <span>PostgreSQL</span>
        </span>
      </div>
    </aside>
  );
}
