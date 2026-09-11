import { request } from "./client";

export const inventoryApi = {
  getAll: () => request("/inventory"),
  getByProductId: (productId) => request(`/inventory/${productId}`),
  getLowStock: () => request("/inventory/low-stock"),
  getMovements: (productId) => request(`/inventory/${productId}/movements`),
  stockIn: (data) =>
    request("/inventory/stock-in", { method: "POST", body: data }),
  stockOut: (data) =>
    request("/inventory/stock-out", { method: "POST", body: data }),
  adjust: (data) =>
    request("/inventory/adjust", { method: "POST", body: data }),
};
