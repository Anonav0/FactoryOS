/**
 * Centralized API client for FactoryOS REST APIs.
 * Translates HTTP errors and backend ErrorResponse format into structured errors.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export class ApiError extends Error {
  constructor(status, message, validationErrors = null, raw = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.validationErrors = validationErrors; // Map of { fieldName: errorMessage }
    this.raw = raw;
  }
}

export async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint.startsWith("/") ? endpoint : `/${endpoint}`}`;
  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  if (
    config.body &&
    typeof config.body === "object" &&
    !(config.body instanceof FormData)
  ) {
    config.body = JSON.stringify(config.body);
  }

  let res;
  try {
    res = await fetch(url, config);
  } catch (err) {
    throw new ApiError(
      0,
      "Unable to connect to the backend server. Please verify the backend is running at " +
        BASE_URL,
      null,
      err,
    );
  }

  // 204 No Content
  if (res.status === 204) {
    return null;
  }

  let data;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (!res.ok) {
    // Backend standard ErrorResponse format: { timestamp, status, error, message, path, validationErrors }
    let userMessage =
      data?.message || res.statusText || "An unexpected error occurred";

    if (res.status === 409 && !data?.message) {
      userMessage = "Resource conflict or concurrent modification occurred.";
    } else if (res.status === 404 && !data?.message) {
      userMessage = "The requested resource was not found.";
    }

    throw new ApiError(
      res.status,
      userMessage,
      data?.validationErrors || null,
      data,
    );
  }

  return data;
}
