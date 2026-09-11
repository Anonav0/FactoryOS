import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { inventoryApi } from "../../api/inventory";
import { useToast } from "../../context/ToastContext";
import { AlertTriangle } from "lucide-react";

export function StockOutModal({ isOpen, onClose, onSuccess, item }) {
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
    if (generalError) {
      setGeneralError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFieldErrors({});
    setGeneralError("");

    try {
      const requestedQty = Number(formData.quantity);
      const payload = {
        productId: item.productId,
        quantity: requestedQty,
        reference: formData.reference.trim() || null,
        reason: formData.reason.trim() || null,
      };

      const updated = await inventoryApi.stockOut(payload);
      toast.success("Stock updated successfully.");
      onSuccess(updated);
      onClose();
    } catch (err) {
      if (err.status === 409) {
        // Formulate clear, professional insufficient stock explanation
        setGeneralError(
          `Unable to complete stock-out. Available stock: ${item.quantityAvailable}. Requested quantity: ${formData.quantity}.`,
        );
      } else if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(
          err.message || "Failed to complete stock-out operation.",
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
      title="Stock Out — Issue Inventory"
    >
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
              <AlertTriangle
                size={18}
                style={{ flexShrink: 0, marginTop: "1px" }}
              />
              <div>
                <div style={{ fontWeight: 600, marginBottom: "2px" }}>
                  Negative Stock Prevention
                </div>
                <div>{generalError}</div>
              </div>
            </div>
          )}

          <div className="context-box">
            <div>
              <div className="context-box-label">Product</div>
              <div className="context-box-value">{item.productName}</div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div className="context-box-label">Current Available</div>
              <div
                className="context-box-value font-mono"
                style={{
                  color:
                    item.quantityAvailable <= 0
                      ? "var(--danger)"
                      : "var(--text-primary)",
                }}
              >
                {item.quantityAvailable} units
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label required" htmlFor="stockout-quantity">
              Quantity to Dispatch
            </label>
            <input
              id="stockout-quantity"
              name="quantity"
              type="number"
              min="1"
              max={item.quantityAvailable}
              step="1"
              className={`form-control font-mono ${fieldErrors.quantity ? "has-error" : ""}`}
              value={formData.quantity}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder={`Max: ${item.quantityAvailable}`}
              required
              autoFocus
            />
            {fieldErrors.quantity && (
              <p className="field-error">{fieldErrors.quantity}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="stockout-reference">
              Reference / Work Order
            </label>
            <input
              id="stockout-reference"
              name="reference"
              type="text"
              className={`form-control font-mono ${fieldErrors.reference ? "has-error" : ""}`}
              value={formData.reference}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. WO-2041, PROD-LINE-2"
            />
            {fieldErrors.reference && (
              <p className="field-error">{fieldErrors.reference}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="stockout-reason">
              Reason / Destination
            </label>
            <input
              id="stockout-reason"
              name="reason"
              type="text"
              className={`form-control ${fieldErrors.reason ? "has-error" : ""}`}
              value={formData.reason}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Production line assembly, customer dispatch"
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
            {isSubmitting ? "Issuing..." : "Stock Out"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
