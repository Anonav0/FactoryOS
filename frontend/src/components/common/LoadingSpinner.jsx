import React from "react";

export function LoadingSpinner({ label = "Loading data..." }) {
  return (
    <div className="state-container" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <div className="state-title">{label}</div>
    </div>
  );
}
