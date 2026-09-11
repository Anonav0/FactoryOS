import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { purchaseOrdersApi } from "../../api/purchaseOrders";
import { suppliersApi } from "../../api/suppliers";
import { productsApi } from "../../api/products";
import { useToast } from "../../context/ToastContext";
import { Plus, Trash2, AlertCircle } from "lucide-react";

export function CreatePurchaseOrderModal({ isOpen, onClose, onSuccess }) {
  const toast = useToast();

  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoadingCatalogs, setIsLoadingCatalogs] = useState(false);

  const [supplierId, setSupplierId] = useState("");
  const [expectedDeliveryDate, setExpectedDeliveryDate] = useState("");
  const [items, setItems] = useState([
    { productId: "", quantity: 1, unitPrice: "" },
  ]);

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      loadCatalogs();
      setSupplierId("");
      setExpectedDeliveryDate("");
      setItems([{ productId: "", quantity: 1, unitPrice: "" }]);
      setFieldErrors({});
      setGeneralError("");
    }
  }, [isOpen]);

  const loadCatalogs = async () => {
    setIsLoadingCatalogs(true);
    try {
      const [activeSuppliers, activeProducts] = await Promise.all([
        suppliersApi.getActive(),
        productsApi.getActive(),
      ]);
      setSuppliers(activeSuppliers || []);
      setProducts(activeProducts || []);
    } catch {
      setGeneralError("Failed to load active suppliers or products.");
    } finally {
      setIsLoadingCatalogs(false);
    }
  };

  const handleProductSelect = (index, prodId) => {
    const selectedProd = products.find((p) => String(p.id) === String(prodId));
    setItems((prev) => {
      const copy = [...prev];
      copy[index] = {
        ...copy[index],
        productId: prodId,
        unitPrice: selectedProd ? String(selectedProd.unitPrice) : "",
      };
      return copy;
    });
    setGeneralError("");
  };

  const handleItemChange = (index, field, value) => {
    setItems((prev) => {
      const copy = [...prev];
      copy[index] = { ...copy[index], [field]: value };
      return copy;
    });
    setGeneralError("");
  };

  const handleAddItem = () => {
    setItems((prev) => [
      ...prev,
      { productId: "", quantity: 1, unitPrice: "" },
    ]);
  };

  const handleRemoveItem = (index) => {
    if (items.length <= 1) return;
    setItems((prev) => prev.filter((_, i) => i !== index));
  };

  // Calculate live item subtotals and grand total
  const calculatedItems = items.map((item) => {
    const qty = Number(item.quantity) || 0;
    const price = Number(item.unitPrice) || 0;
    const subtotal = qty > 0 && price >= 0 ? qty * price : 0;
    return { ...item, subtotal };
  });

  const totalAmount = calculatedItems.reduce(
    (acc, curr) => acc + curr.subtotal,
    0,
  );

  const formatCurrency = (val) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(val);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFieldErrors({});
    setGeneralError("");

    // Pre-validation checks
    if (!supplierId) {
      setGeneralError("Please select a supplier for the purchase order.");
      setIsSubmitting(false);
      return;
    }

    if (items.some((it) => !it.productId)) {
      setGeneralError("Please select a product for every line item.");
      setIsSubmitting(false);
      return;
    }

    // Check duplicate products in form
    const selectedProductIds = items.map((it) => it.productId);
    const uniqueProductIds = new Set(selectedProductIds);
    if (uniqueProductIds.size < selectedProductIds.length) {
      setGeneralError(
        "Duplicate products detected in order items. Please combine quantities into a single item.",
      );
      setIsSubmitting(false);
      return;
    }

    try {
      const payload = {
        supplierId: Number(supplierId),
        expectedDeliveryDate: expectedDeliveryDate || null,
        items: items.map((it) => ({
          productId: Number(it.productId),
          quantity: Number(it.quantity),
          unitPrice: Number(it.unitPrice),
        })),
      };

      const created = await purchaseOrdersApi.create(payload);
      toast.success(
        `Purchase Order ${created.orderNumber} created successfully.`,
      );
      onSuccess(created);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(err.message || "Failed to create purchase order.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create Purchase Order" wide>
      <form onSubmit={handleSubmit} noValidate>
        <div className="modal-body">
          {generalError && (
            <div
              style={{
                padding: "0.875rem 1rem",
                backgroundColor: "var(--danger-bg)",
                border: "1px solid var(--danger-border)",
                borderRadius: "var(--radius-sm)",
                color: "var(--danger-text)",
                fontSize: "0.8125rem",
                marginBottom: "1.25rem",
                display: "flex",
                alignItems: "flex-start",
                gap: "0.5rem",
              }}
            >
              <AlertCircle
                size={18}
                style={{ flexShrink: 0, marginTop: "1px" }}
              />
              <div>{generalError}</div>
            </div>
          )}

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "1rem",
              marginBottom: "1.25rem",
            }}
          >
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label required" htmlFor="po-supplier">
                Supplier
              </label>
              <select
                id="po-supplier"
                className={`form-control ${fieldErrors.supplierId ? "has-error" : ""}`}
                value={supplierId}
                onChange={(e) => setSupplierId(e.target.value)}
                disabled={isLoadingCatalogs || isSubmitting}
                required
              >
                <option value="">-- Select Active Supplier --</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} {s.contactPerson ? `(${s.contactPerson})` : ""}
                  </option>
                ))}
              </select>
              {fieldErrors.supplierId && (
                <p className="field-error">{fieldErrors.supplierId}</p>
              )}
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label" htmlFor="po-delivery-date">
                Expected Delivery Date
              </label>
              <input
                id="po-delivery-date"
                type="date"
                className="form-control"
                value={expectedDeliveryDate}
                onChange={(e) => setExpectedDeliveryDate(e.target.value)}
                disabled={isSubmitting}
              />
              <p className="field-hint">Estimated delivery to warehouse</p>
            </div>
          </div>

          {/* Line Items Table */}
          <div style={{ marginBottom: "1rem" }}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "0.5rem",
              }}
            >
              <label
                className="form-label required"
                style={{ marginBottom: 0 }}
              >
                Order Items ({items.length})
              </label>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={handleAddItem}
                disabled={isSubmitting}
              >
                <Plus size={13} />
                <span>Add Item</span>
              </button>
            </div>

            <div
              className="table-card"
              style={{ border: "1px solid var(--border-subtle)" }}
            >
              <div className="table-scroll-wrapper">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th style={{ width: "40%" }}>Product</th>
                      <th style={{ width: "18%" }}>Quantity</th>
                      <th style={{ width: "22%" }}>Unit Price (₹)</th>
                      <th className="text-right" style={{ width: "15%" }}>
                        Subtotal
                      </th>
                      <th style={{ width: "5%" }}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {calculatedItems.map((item, index) => (
                      <tr key={index}>
                        <td>
                          <select
                            className="form-control"
                            value={item.productId}
                            onChange={(e) =>
                              handleProductSelect(index, e.target.value)
                            }
                            disabled={isLoadingCatalogs || isSubmitting}
                            style={{ fontSize: "0.8125rem" }}
                          >
                            <option value="">-- Select Product --</option>
                            {products.map((p) => (
                              <option key={p.id} value={p.id}>
                                {p.sku} — {p.name}
                              </option>
                            ))}
                          </select>
                        </td>
                        <td>
                          <input
                            type="number"
                            min="1"
                            step="1"
                            className="form-control font-mono"
                            value={item.quantity}
                            onChange={(e) =>
                              handleItemChange(
                                index,
                                "quantity",
                                e.target.value,
                              )
                            }
                            disabled={isSubmitting}
                            style={{ fontSize: "0.8125rem" }}
                            placeholder="Qty"
                          />
                        </td>
                        <td>
                          <input
                            type="number"
                            step="0.01"
                            min="0"
                            className="form-control font-mono"
                            value={item.unitPrice}
                            onChange={(e) =>
                              handleItemChange(
                                index,
                                "unitPrice",
                                e.target.value,
                              )
                            }
                            disabled={isSubmitting}
                            style={{ fontSize: "0.8125rem" }}
                            placeholder="0.00"
                          />
                        </td>
                        <td
                          className="text-right font-mono"
                          style={{ fontWeight: 600 }}
                        >
                          {formatCurrency(item.subtotal)}
                        </td>
                        <td style={{ textAlign: "center" }}>
                          <button
                            type="button"
                            className="modal-close-btn"
                            onClick={() => handleRemoveItem(index)}
                            disabled={items.length <= 1 || isSubmitting}
                            title="Remove line item"
                            style={{
                              color:
                                items.length <= 1
                                  ? "var(--border-strong)"
                                  : "var(--danger)",
                            }}
                          >
                            <Trash2 size={14} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* Grand Total Bar */}
          <div
            style={{
              padding: "1rem 1.25rem",
              backgroundColor: "var(--bg-surface-subtle)",
              border: "1px solid var(--border-subtle)",
              borderRadius: "var(--radius-sm)",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <div>
              <div
                style={{ fontSize: "0.8125rem", color: "var(--text-muted)" }}
              >
                Total Purchase Order Value
              </div>
              <div style={{ fontSize: "0.75rem", color: "var(--text-muted)" }}>
                Authoritative subtotals will be verified and recorded on
                submission.
              </div>
            </div>
            <div
              className="font-mono"
              style={{
                fontSize: "1.375rem",
                fontWeight: 700,
                color: "var(--primary)",
              }}
            >
              {formatCurrency(totalAmount)}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onClose}
            disabled={isSubmitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Creating PO..." : "Create Purchase Order"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
