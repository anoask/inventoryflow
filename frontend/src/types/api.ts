export interface ApiErrorBody {
  message?: string
  error?: string
  status?: number
  code?: string
  details?: string[]
  path?: string
}
