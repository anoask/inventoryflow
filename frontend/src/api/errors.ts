import type { AxiosError } from 'axios'
import type { ApiErrorBody } from '../types/api'

export function getErrorMessage(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'isAxiosError' in err) {
    const ax = err as AxiosError<ApiErrorBody>
    const data = ax.response?.data
    if (data?.code === 'INSUFFICIENT_STOCK' && data.message) {
      return data.message
    }
    if (data?.message) return data.message
    if (Array.isArray(data?.details) && data.details.length > 0) {
      return data.details.join('; ')
    }
    if (data?.error && ax.response?.status) {
      return `${data.error} (${ax.response.status})`
    }
    if (ax.message) return ax.message
  }
  if (err instanceof Error) return err.message
  return 'Something went wrong'
}
