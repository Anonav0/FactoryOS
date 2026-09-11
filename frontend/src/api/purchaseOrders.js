import { request } from "./client";

export const purchaseOrdersApi = {
  getAll: () => request("/purchase-orders"),
  getById: (id) => request(`/purchase-orders/${id}`),
  getByOrderNumber: (orderNumber) =>
    request(`/purchase-orders/order-number/${encodeURIComponent(orderNumber)}`),
  create: (data) => request("/purchase-orders", { method: "POST", body: data }),
  approve: (id) =>
    request(`/purchase-orders/${id}/approve`, { method: "POST" }),
  receive: (id) =>
    request(`/purchase-orders/${id}/receive`, { method: "POST" }),
  cancel: (id) => request(`/purchase-orders/${id}/cancel`, { method: "POST" }),
};
