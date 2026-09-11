import React, { useState, useEffect } from "react";
import { productsApi } from "../api/products";
import { ProductTable } from "../components/products/ProductTable";
import { ProductModal } from "../components/products/ProductModal";
import { ConfirmModal } from "../components/common/ConfirmModal";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import { EmptyState } from "../components/common/EmptyState";
import { useToast } from "../context/ToastContext";
import { Plus, RefreshCw, Package } from "lucide-react";

export function ProductsPage() {
  const toast = useToast();

  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  // Modals state
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);

  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [productToDeactivate, setProductToDeactivate] = useState(null);
  const [isDeactivating, setIsDeactivating] = useState(false);

  const fetchProducts = async () => {
    setIsLoading(true);
    setError("");
    try {
      const data = await productsApi.getAll();
      setProducts(data || []);
    } catch (err) {
      setError(err.message || "Failed to load products list.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleOpenCreate = () => {
    setSelectedProduct(null);
    setIsFormModalOpen(true);
  };

  const handleOpenEdit = (product) => {
    setSelectedProduct(product);
    setIsFormModalOpen(true);
  };

  const handleOpenDeactivate = (product) => {
    setProductToDeactivate(product);
    setIsConfirmOpen(true);
  };

  const handleConfirmDeactivate = async () => {
    if (!productToDeactivate) return;
    setIsDeactivating(true);
    try {
      await productsApi.deactivate(productToDeactivate.id);
      toast.success(`Product "${productToDeactivate.name}" deactivated.`);
      setIsConfirmOpen(false);
      setProductToDeactivate(null);
      await fetchProducts();
    } catch (err) {
      toast.error(err.message || "Failed to deactivate product.");
    } finally {
      setIsDeactivating(false);
    }
  };

  const handleFormSuccess = () => {
    fetchProducts();
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Products Master Data</h1>
          <p className="page-description">
            Define and manage manufactured components, parts, specifications,
            and reorder levels.
          </p>
        </div>
        <div className="page-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={fetchProducts}
            disabled={isLoading}
            title="Refresh products"
          >
            <RefreshCw size={14} className={isLoading ? "spinner" : ""} />
            <span>Refresh</span>
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleOpenCreate}
          >
            <Plus size={16} />
            <span>Add Product</span>
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
        <LoadingSpinner label="Loading products..." />
      ) : products.length === 0 ? (
        <EmptyState
          icon={Package}
          title="No products found."
          description="Add your first product to get started."
          actionLabel="Add Product"
          onAction={handleOpenCreate}
        />
      ) : (
        <ProductTable
          products={products}
          onEdit={handleOpenEdit}
          onDeactivate={handleOpenDeactivate}
        />
      )}

      <ProductModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        onSuccess={handleFormSuccess}
        initialProduct={selectedProduct}
      />

      <ConfirmModal
        isOpen={isConfirmOpen}
        onClose={() => {
          setIsConfirmOpen(false);
          setProductToDeactivate(null);
        }}
        onConfirm={handleConfirmDeactivate}
        title="Deactivate Product?"
        message="This product will no longer be available for normal inventory operations."
        confirmText="Deactivate"
        confirmVariant="danger"
        isLoading={isDeactivating}
      />
    </div>
  );
}
