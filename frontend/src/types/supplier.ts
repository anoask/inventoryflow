export interface SupplierResponse {
  id: number
  name: string
  email: string
  phone: string | null
  address: string | null
}

export interface SupplierCreateRequest {
  name: string
  email: string
  phone?: string
  address?: string
}

export type SupplierUpdateRequest = SupplierCreateRequest
