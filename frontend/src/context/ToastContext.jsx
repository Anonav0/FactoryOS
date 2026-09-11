import React, { createContext, useContext, useState, useCallback } from "react";
import { CheckCircle2, AlertCircle, Info, X } from "lucide-react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (type, message, title = "") => {
      const id = Date.now() + Math.random();
      const newToast = { id, type, message, title };

      setToasts((prev) => [...prev, newToast]);

      setTimeout(() => {
        removeToast(id);
      }, 4500);
    },
    [removeToast],
  );

  const toast = {
    success: (msg, title = "Success") => addToast("success", msg, title),
    error: (msg, title = "Error") => addToast("error", msg, title),
    info: (msg, title = "Notice") => addToast("info", msg, title),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-container" aria-live="polite" aria-atomic="true">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} role="status">
            {t.type === "success" && (
              <CheckCircle2
                size={18}
                color="var(--success)"
                style={{ marginTop: "2px", flexShrink: 0 }}
              />
            )}
            {t.type === "error" && (
              <AlertCircle
                size={18}
                color="var(--danger)"
                style={{ marginTop: "2px", flexShrink: 0 }}
              />
            )}
            {t.type === "info" && (
              <Info
                size={18}
                color="var(--primary)"
                style={{ marginTop: "2px", flexShrink: 0 }}
              />
            )}
            <div className="toast-content">
              {t.title && <div className="toast-title">{t.title}</div>}
              <div className="toast-message">{t.message}</div>
            </div>
            <button
              className="toast-close"
              onClick={() => removeToast(t.id)}
              aria-label="Close notification"
            >
              <X size={14} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}
