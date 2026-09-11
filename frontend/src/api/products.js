import { request } from "./client";

export const productsApi = {
  getAll: () => request("/products"),
  getActive: () => request("/products/active"),
  getById: (id) => request(`/products/${id}`),
  search: (name) =>
    request(
      `/products/search${name ? `?name=${encodeURIComponent(name)}` : ""}`,
    ),
  create: (data) => request("/products", { method: "POST", body: data }),
  update: (id, data) =>
    request(`/products/${id}`, { method: "PUT", body: data }),
  deactivate: (id) => request(`/products/${id}`, { method: "DELETE" }),
};
