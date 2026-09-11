import { request } from "./client";

export const purchaseOrdersApi = {
  getAll: () => request("/purchase-orders"),
  getById: (id) => request(`/purchase-orders/${id}`),
  getByOrderNumber: (orderNumber) =>
    request(`/purchase-orders/order-number/${encodeURIComponent(orderNumber)}`),
  create: (data) => request("/purchase-orders", { method: "POST", body: data }),
};
