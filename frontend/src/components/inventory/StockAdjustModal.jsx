import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { inventoryApi } from "../../api/inventory";
import { useToast } from "../../context/ToastContext";

export function StockAdjustModal({ isOpen, onClose, onSuccess, item }) {
  const toast = useToast();
  const [formData, setFormData] = useState({
    newQuantity: "",
    reference: "",
    reason: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (item) {
      setFormData({
        newQuantity: String(item.quantityAvailable),
        reference: "",
        reason: "",
      });
    }
    setFieldErrors({});
    setGeneralError("");
  }, [item, isOpen]);

  if (!item) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const delta =
    formData.newQuantity !== "" && !isNaN(Number(formData.newQuantity))
      ? Number(formData.newQuantity) - item.quantityAvailable
      : 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFieldErrors({});
    setGeneralError("");

    try {
      const payload = {
        productId: item.productId,
        newQuantity: Number(formData.newQuantity),
        reference: formData.reference.trim() || null,
        reason: formData.reason.trim() || null,
      };

      const updated = await inventoryApi.adjust(payload);
      toast.success("Stock adjusted successfully.");
      onSuccess(updated);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(err.message || "Failed to adjust stock balance.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Stock Adjustment — Audit Balance"
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
              <div className="context-box-label">Current Recorded Stock</div>
              <div className="context-box-value font-mono">
                {item.quantityAvailable} units
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label required" htmlFor="adjust-newquantity">
              New Verified Quantity
            </label>
            <input
              id="adjust-newquantity"
              name="newQuantity"
              type="number"
              min="0"
              step="1"
              className={`form-control font-mono ${fieldErrors.newQuantity ? "has-error" : ""}`}
              value={formData.newQuantity}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. 95"
              required
              autoFocus
            />
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginTop: "4px",
              }}
            >
              <span className="field-hint">
                Calculated adjustment delta:{" "}
                <strong
                  style={{
                    color:
                      delta > 0
                        ? "var(--success)"
                        : delta < 0
                          ? "var(--danger)"
                          : "var(--text-secondary)",
                  }}
                >
                  {delta > 0 ? `+${delta}` : delta} units
                </strong>
              </span>
            </div>
            {fieldErrors.newQuantity && (
              <p className="field-error">{fieldErrors.newQuantity}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="adjust-reference">
              Reference / Audit ID
            </label>
            <input
              id="adjust-reference"
              name="reference"
              type="text"
              className={`form-control font-mono ${fieldErrors.reference ? "has-error" : ""}`}
              value={formData.reference}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. STOCK-COUNT-001, CYCLE-AUDIT-Q3"
            />
            {fieldErrors.reference && (
              <p className="field-error">{fieldErrors.reference}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="adjust-reason">
              Reason for Adjustment
            </label>
            <input
              id="adjust-reason"
              name="reason"
              type="text"
              className={`form-control ${fieldErrors.reason ? "has-error" : ""}`}
              value={formData.reason}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Physical stock count variance, damaged stock write-off"
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
            {isSubmitting ? "Adjusting..." : "Save Adjustment"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
