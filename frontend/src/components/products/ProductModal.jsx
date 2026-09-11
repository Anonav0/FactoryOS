import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { productsApi } from "../../api/products";
import { useToast } from "../../context/ToastContext";

export function ProductModal({
  isOpen,
  onClose,
  onSuccess,
  initialProduct = null,
}) {
  const isEdit = Boolean(initialProduct);
  const toast = useToast();

  const [formData, setFormData] = useState({
    sku: "",
    name: "",
    description: "",
    category: "",
    unitPrice: "",
    reorderLevel: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (initialProduct) {
      setFormData({
        sku: initialProduct.sku || "",
        name: initialProduct.name || "",
        description: initialProduct.description || "",
        category: initialProduct.category || "",
        unitPrice:
          initialProduct.unitPrice !== undefined
            ? String(initialProduct.unitPrice)
            : "",
        reorderLevel:
          initialProduct.reorderLevel !== undefined
            ? String(initialProduct.reorderLevel)
            : "",
      });
    } else {
      setFormData({
        sku: "",
        name: "",
        description: "",
        category: "",
        unitPrice: "",
        reorderLevel: "",
      });
    }
    setFieldErrors({});
    setGeneralError("");
  }, [initialProduct, isOpen]);

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
      if (isEdit) {
        // SKU is immutable and omitted from UpdateProductRequest
        const payload = {
          name: formData.name.trim(),
          description: formData.description.trim(),
          category: formData.category.trim(),
          unitPrice:
            formData.unitPrice === "" ? null : Number(formData.unitPrice),
          reorderLevel:
            formData.reorderLevel === "" ? null : Number(formData.reorderLevel),
        };

        const updated = await productsApi.update(initialProduct.id, payload);
        toast.success(`Product "${updated.name}" updated successfully.`);
        onSuccess(updated);
        onClose();
      } else {
        const payload = {
          sku: formData.sku.trim(),
          name: formData.name.trim(),
          description: formData.description.trim(),
          category: formData.category.trim(),
          unitPrice:
            formData.unitPrice === "" ? null : Number(formData.unitPrice),
          reorderLevel:
            formData.reorderLevel === "" ? null : Number(formData.reorderLevel),
        };

        const created = await productsApi.create(payload);
        toast.success(`Product "${created.name}" created successfully.`);
        onSuccess(created);
        onClose();
      }
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(
          err.message || "Failed to save product. Please check your input.",
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
      title={isEdit ? "Edit Product" : "Add New Product"}
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

          <div className="form-group">
            <label
              className={`form-label ${!isEdit ? "required" : ""}`}
              htmlFor="product-sku"
            >
              SKU (Stock Keeping Unit)
            </label>
            <input
              id="product-sku"
              name="sku"
              type="text"
              className={`form-control font-mono ${fieldErrors.sku ? "has-error" : ""}`}
              value={formData.sku}
              onChange={handleChange}
              disabled={isEdit || isSubmitting}
              placeholder="e.g. BRG-6204"
              required={!isEdit}
            />
            {isEdit && (
              <p className="field-hint">
                SKU is immutable and cannot be altered after creation.
              </p>
            )}
            {fieldErrors.sku && (
              <p className="field-error">{fieldErrors.sku}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label required" htmlFor="product-name">
              Product Name
            </label>
            <input
              id="product-name"
              name="name"
              type="text"
              className={`form-control ${fieldErrors.name ? "has-error" : ""}`}
              value={formData.name}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Steel Ball Bearing 6204"
              required
            />
            {fieldErrors.name && (
              <p className="field-error">{fieldErrors.name}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="product-category">
              Category
            </label>
            <input
              id="product-category"
              name="category"
              type="text"
              className={`form-control ${fieldErrors.category ? "has-error" : ""}`}
              value={formData.category}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Bearings, Hydraulics, Fasteners"
            />
            {fieldErrors.category && (
              <p className="field-error">{fieldErrors.category}</p>
            )}
          </div>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "1rem",
            }}
          >
            <div className="form-group">
              <label className="form-label required" htmlFor="product-price">
                Unit Price (₹)
              </label>
              <input
                id="product-price"
                name="unitPrice"
                type="number"
                step="0.01"
                min="0"
                className={`form-control ${fieldErrors.unitPrice ? "has-error" : ""}`}
                value={formData.unitPrice}
                onChange={handleChange}
                disabled={isSubmitting}
                placeholder="0.00"
                required
              />
              {fieldErrors.unitPrice && (
                <p className="field-error">{fieldErrors.unitPrice}</p>
              )}
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="product-reorder">
                Reorder Level
              </label>
              <input
                id="product-reorder"
                name="reorderLevel"
                type="number"
                min="0"
                step="1"
                className={`form-control ${fieldErrors.reorderLevel ? "has-error" : ""}`}
                value={formData.reorderLevel}
                onChange={handleChange}
                disabled={isSubmitting}
                placeholder="e.g. 20"
                required
              />
              {fieldErrors.reorderLevel && (
                <p className="field-error">{fieldErrors.reorderLevel}</p>
              )}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="product-description">
              Description
            </label>
            <textarea
              id="product-description"
              name="description"
              rows={3}
              className={`form-control ${fieldErrors.description ? "has-error" : ""}`}
              value={formData.description}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="Technical specifications, material grade, or operating limits..."
            />
            {fieldErrors.description && (
              <p className="field-error">{fieldErrors.description}</p>
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
            {isSubmitting
              ? "Saving..."
              : isEdit
                ? "Update Product"
                : "Create Product"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
