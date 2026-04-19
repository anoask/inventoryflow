import axios from 'axios'

const rawBase = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
// Strip trailing slash and accidental `/api` suffix (paths already include `/api/...`).
const baseURL = rawBase.replace(/\/$/, '').replace(/\/api$/i, '')

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

const AUTH_KEY = 'inventoryflow.auth'

api.interceptors.request.use((config) => {
  try {
    const raw = localStorage.getItem(AUTH_KEY)
    if (raw) {
      const parsed = JSON.parse(raw) as { token?: string }
      if (parsed.token) {
        config.headers.Authorization = `Bearer ${parsed.token}`
      }
    }
  } catch {
    /* ignore */
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem(AUTH_KEY)
      const path = window.location.pathname
      if (!path.startsWith('/login')) {
        window.location.assign('/login')
      }
    }
    return Promise.reject(err)
  }
)

export { AUTH_KEY }
