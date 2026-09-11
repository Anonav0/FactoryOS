import React, { useState, useEffect } from "react";
import { suppliersApi } from "../api/suppliers";
import { SupplierTable } from "../components/suppliers/SupplierTable";
import { SupplierModal } from "../components/suppliers/SupplierModal";
import { ConfirmModal } from "../components/common/ConfirmModal";
import { LoadingSpinner } from "../components/common/LoadingSpinner";
import { EmptyState } from "../components/common/EmptyState";
import { useToast } from "../context/ToastContext";
import { Plus, RefreshCw, Truck } from "lucide-react";

export function SuppliersPage() {
  const toast = useToast();

  const [suppliers, setSuppliers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  // Modals state
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState(null);

  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [supplierToDeactivate, setSupplierToDeactivate] = useState(null);
  const [isDeactivating, setIsDeactivating] = useState(false);

  const fetchSuppliers = async () => {
    setIsLoading(true);
    setError("");
    try {
      const data = await suppliersApi.getAll();
      setSuppliers(data || []);
    } catch (err) {
      setError(err.message || "Failed to load suppliers list.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const handleOpenCreate = () => {
    setSelectedSupplier(null);
    setIsFormModalOpen(true);
  };

  const handleOpenEdit = (supplier) => {
    setSelectedSupplier(supplier);
    setIsFormModalOpen(true);
  };

  const handleOpenDeactivate = (supplier) => {
    setSupplierToDeactivate(supplier);
    setIsConfirmOpen(true);
  };

  const handleConfirmDeactivate = async () => {
    if (!supplierToDeactivate) return;
    setIsDeactivating(true);
    try {
      await suppliersApi.deactivate(supplierToDeactivate.id);
      toast.success(`Supplier "${supplierToDeactivate.name}" deactivated.`);
      setIsConfirmOpen(false);
      setSupplierToDeactivate(null);
      await fetchSuppliers();
    } catch (err) {
      toast.error(err.message || "Failed to deactivate supplier.");
    } finally {
      setIsDeactivating(false);
    }
  };

  const handleFormSuccess = () => {
    fetchSuppliers();
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Suppliers Directory</h1>
          <p className="page-description">
            Maintain authorized industrial vendors, contacts, email addresses,
            and logistical facilities.
          </p>
        </div>
        <div className="page-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={fetchSuppliers}
            disabled={isLoading}
            title="Refresh suppliers"
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
            <span>Add Supplier</span>
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
        <LoadingSpinner label="Loading suppliers..." />
      ) : suppliers.length === 0 ? (
        <EmptyState
          icon={Truck}
          title="No suppliers found."
          description="Add a supplier to get started."
          actionLabel="Add Supplier"
          onAction={handleOpenCreate}
        />
      ) : (
        <SupplierTable
          suppliers={suppliers}
          onEdit={handleOpenEdit}
          onDeactivate={handleOpenDeactivate}
        />
      )}

      <SupplierModal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        onSuccess={handleFormSuccess}
        initialSupplier={selectedSupplier}
      />

      <ConfirmModal
        isOpen={isConfirmOpen}
        onClose={() => {
          setIsConfirmOpen(false);
          setSupplierToDeactivate(null);
        }}
        onConfirm={handleConfirmDeactivate}
        title="Deactivate Supplier?"
        message="This supplier will no longer be available for new procurement and purchase orders."
        confirmText="Deactivate"
        confirmVariant="danger"
        isLoading={isDeactivating}
      />
    </div>
  );
}
