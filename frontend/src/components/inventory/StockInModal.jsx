import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { inventoryApi } from "../../api/inventory";
import { useToast } from "../../context/ToastContext";

export function StockInModal({ isOpen, onClose, onSuccess, item }) {
  const toast = useToast();
  const [formData, setFormData] = useState({
    quantity: "",
    reference: "",
    reason: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormData({ quantity: "", reference: "", reason: "" });
    setFieldErrors({});
    setGeneralError("");
  }, [isOpen]);

  if (!item) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFieldErrors({});
    setGeneralError("");

    try {
      const payload = {
        productId: item.productId,
        quantity: Number(formData.quantity),
        reference: formData.reference.trim() || null,
        reason: formData.reason.trim() || null,
      };

      const updated = await inventoryApi.stockIn(payload);
      toast.success("Stock updated successfully.");
      onSuccess(updated);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(
          err.message || "Failed to complete stock-in operation.",
        );
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Stock In — Receive Inventory"
    >
      <form onSubmit={handleSubmit} noValidate>
        <div className="modal-body">
          {generalError && (
            <div
              style={{
                padding: "0.75rem 1rem",
                backgroundColor: "var(--danger-bg)",
                border: "1px solid var(--danger-border)",
                borderRadius: "var(--radius-sm)",
                color: "var(--danger-text)",
                fontSize: "0.8125rem",
                marginBottom: "1.25rem",
              }}
            >
              {generalError}
            </div>
          )}

          <div className="context-box">
            <div>
              <div className="context-box-label">Product</div>
              <div className="context-box-value">{item.productName}</div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div className="context-box-label">SKU / Current Stock</div>
              <div className="context-box-value font-mono">
                {item.sku} &middot; {item.quantityAvailable} units
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label required" htmlFor="stockin-quantity">
              Quantity to Receive
            </label>
            <input
              id="stockin-quantity"
              name="quantity"
              type="number"
              min="1"
              step="1"
              className={`form-control font-mono ${fieldErrors.quantity ? "has-error" : ""}`}
              value={formData.quantity}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. 50"
              required
              autoFocus
            />
            {fieldErrors.quantity && (
              <p className="field-error">{fieldErrors.quantity}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="stockin-reference">
              Reference Identifier
            </label>
            <input
              id="stockin-reference"
              name="reference"
              type="text"
              className={`form-control font-mono ${fieldErrors.reference ? "has-error" : ""}`}
              value={formData.reference}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. GRN-1001, PO-5542"
            />
            {fieldErrors.reference && (
              <p className="field-error">{fieldErrors.reference}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="stockin-reason">
              Reason / Notes
            </label>
            <input
              id="stockin-reason"
              name="reason"
              type="text"
              className={`form-control ${fieldErrors.reason ? "has-error" : ""}`}
              value={formData.reason}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Received from supplier, production return"
            />
            {fieldErrors.reason && (
              <p className="field-error">{fieldErrors.reason}</p>
            )}
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
            {isSubmitting ? "Receiving..." : "Stock In"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
