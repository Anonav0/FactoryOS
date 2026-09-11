import React, { useState, useEffect } from "react";
import { Modal } from "../common/Modal";
import { suppliersApi } from "../../api/suppliers";
import { useToast } from "../../context/ToastContext";

export function SupplierModal({
  isOpen,
  onClose,
  onSuccess,
  initialSupplier = null,
}) {
  const isEdit = Boolean(initialSupplier);
  const toast = useToast();

  const [formData, setFormData] = useState({
    name: "",
    contactPerson: "",
    email: "",
    phone: "",
    address: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (initialSupplier) {
      setFormData({
        name: initialSupplier.name || "",
        contactPerson: initialSupplier.contactPerson || "",
        email: initialSupplier.email || "",
        phone: initialSupplier.phone || "",
        address: initialSupplier.address || "",
      });
    } else {
      setFormData({
        name: "",
        contactPerson: "",
        email: "",
        phone: "",
        address: "",
      });
    }
    setFieldErrors({});
    setGeneralError("");
  }, [initialSupplier, isOpen]);

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
        name: formData.name.trim(),
        contactPerson: formData.contactPerson.trim(),
        email: formData.email.trim(),
        phone: formData.phone.trim(),
        address: formData.address.trim(),
      };

      if (isEdit) {
        const updated = await suppliersApi.update(initialSupplier.id, payload);
        toast.success(`Supplier "${updated.name}" updated successfully.`);
        onSuccess(updated);
        onClose();
      } else {
        const created = await suppliersApi.create(payload);
        toast.success(`Supplier "${created.name}" created successfully.`);
        onSuccess(created);
        onClose();
      }
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors(err.validationErrors);
      } else {
        setGeneralError(
          err.message || "Failed to save supplier. Please check your input.",
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
      title={isEdit ? "Edit Supplier" : "Add New Supplier"}
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
            <label className="form-label required" htmlFor="supplier-name">
              Supplier / Company Name
            </label>
            <input
              id="supplier-name"
              name="name"
              type="text"
              className={`form-control ${fieldErrors.name ? "has-error" : ""}`}
              value={formData.name}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. SKF India Ltd."
              required
            />
            {fieldErrors.name && (
              <p className="field-error">{fieldErrors.name}</p>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="supplier-contact">
              Contact Person
            </label>
            <input
              id="supplier-contact"
              name="contactPerson"
              type="text"
              className={`form-control ${fieldErrors.contactPerson ? "has-error" : ""}`}
              value={formData.contactPerson}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="e.g. Rajesh Sharma"
            />
            {fieldErrors.contactPerson && (
              <p className="field-error">{fieldErrors.contactPerson}</p>
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
              <label className="form-label" htmlFor="supplier-email">
                Email Address
              </label>
              <input
                id="supplier-email"
                name="email"
                type="email"
                className={`form-control ${fieldErrors.email ? "has-error" : ""}`}
                value={formData.email}
                onChange={handleChange}
                disabled={isSubmitting}
                placeholder="supply@example.com"
              />
              {fieldErrors.email && (
                <p className="field-error">{fieldErrors.email}</p>
              )}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="supplier-phone">
                Phone Number
              </label>
              <input
                id="supplier-phone"
                name="phone"
                type="tel"
                className={`form-control ${fieldErrors.phone ? "has-error" : ""}`}
                value={formData.phone}
                onChange={handleChange}
                disabled={isSubmitting}
                placeholder="+91 98765 43210"
              />
              {fieldErrors.phone && (
                <p className="field-error">{fieldErrors.phone}</p>
              )}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="supplier-address">
              Address / Warehouse Facility
            </label>
            <textarea
              id="supplier-address"
              name="address"
              rows={3}
              className={`form-control ${fieldErrors.address ? "has-error" : ""}`}
              value={formData.address}
              onChange={handleChange}
              disabled={isSubmitting}
              placeholder="Industrial Area, Plot No. 42, Pune..."
            />
            {fieldErrors.address && (
              <p className="field-error">{fieldErrors.address}</p>
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
                ? "Update Supplier"
                : "Create Supplier"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
