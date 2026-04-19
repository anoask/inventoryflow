import type { AxiosError } from 'axios'
import type { ApiErrorBody } from '../types/api'

function stringifyErrorField(value: unknown): string | null {
  if (value == null) return null
  if (typeof value === 'string') return value.trim() || null
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  try {
    return JSON.stringify(value)
  } catch {
    return null
  }
}

export function getErrorMessage(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'isAxiosError' in err) {
    const ax = err as AxiosError<ApiErrorBody>
    const data = ax.response?.data
    const status = ax.response?.status
    const code = ax.code

    if (
      !ax.response &&
      (code === 'ERR_NETWORK' || ax.message === 'Network Error')
    ) {
      return (
        'Cannot reach the API (often CORS or wrong URL). Check: (1) VITE_API_BASE_URL is your ' +
        'Railway https URL, frontend redeployed; (2) Railway INVENTORYFLOW_CORS_ORIGINS includes ' +
        'this page’s exact origin (e.g. https://your-app.vercel.app), backend redeployed; ' +
        '(3) Railway service is running.'
      )
    }

    if (status === 404) {
      return (
        'Server returned 404 (not found). Check VITE_API_BASE_URL in Vercel: it must be your ' +
        'Railway API origin only (e.g. https://….up.railway.app), no /api suffix, then redeploy.'
      )
    }

    if (data?.code === 'INSUFFICIENT_STOCK' && data.message) {
      return data.message
    }
    if (data?.message) return data.message
    if (Array.isArray(data?.details) && data.details.length > 0) {
      return data.details.join('; ')
    }
    if (data?.error != null && status) {
      const errPart = stringifyErrorField(data.error as unknown)
      if (errPart) return `${errPart} (${status})`
    }
    if (ax.message) return ax.message
  }
  if (err instanceof Error) return err.message
  return 'Something went wrong'
}
