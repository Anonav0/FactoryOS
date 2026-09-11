import { request } from "./client";

export const suppliersApi = {
  getAll: () => request("/suppliers"),
  getActive: () => request("/suppliers/active"),
  getById: (id) => request(`/suppliers/${id}`),
  create: (data) => request("/suppliers", { method: "POST", body: data }),
  update: (id, data) =>
    request(`/suppliers/${id}`, { method: "PUT", body: data }),
  deactivate: (id) => request(`/suppliers/${id}`, { method: "DELETE" }),
};
