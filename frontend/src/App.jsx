import React, { useState } from "react";
import { ToastProvider } from "./context/ToastContext";
import { AppLayout } from "./components/layout/AppLayout";
import { DashboardPage } from "./pages/DashboardPage";
import { ProductsPage } from "./pages/ProductsPage";
import { SuppliersPage } from "./pages/SuppliersPage";
import { InventoryPage } from "./pages/InventoryPage";

const TAB_TITLES = {
  dashboard: "Dashboard",
  products: "Products",
  suppliers: "Suppliers",
  inventory: "Inventory",
};

export default function App() {
  const [currentTab, setCurrentTab] = useState("dashboard");

  const renderContent = () => {
    switch (currentTab) {
      case "dashboard":
        return <DashboardPage onNavigate={(tab) => setCurrentTab(tab)} />;
      case "products":
        return <ProductsPage />;
      case "suppliers":
        return <SuppliersPage />;
      case "inventory":
        return <InventoryPage />;
      default:
        return <DashboardPage onNavigate={(tab) => setCurrentTab(tab)} />;
    }
  };

  return (
    <ToastProvider>
      <AppLayout
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        pageTitle={TAB_TITLES[currentTab] || "FactoryOS"}
      >
        {renderContent()}
      </AppLayout>
    </ToastProvider>
  );
}
