import React from "react";
import { PackageOpen } from "lucide-react";

export function EmptyState({
  icon: Icon = PackageOpen,
  title,
  description,
  actionLabel,
  onAction,
}) {
  return (
    <div className="state-container">
      <div style={{ color: "var(--text-muted)", marginBottom: "0.75rem" }}>
        <Icon size={36} strokeWidth={1.5} />
      </div>
      <h3 className="state-title">{title}</h3>
      {description && <p className="state-description">{description}</p>}
      {actionLabel && onAction && (
        <div className="state-action">
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={onAction}
          >
            {actionLabel}
          </button>
        </div>
      )}
    </div>
  );
}
