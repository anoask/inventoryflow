export interface ProductResponse {
  id: number
  sku: string
  name: string
  description: string | null
  category: string
  price: string
  quantityInStock: number
  reorderLevel: number
  supplierId: number
  supplierName: string
}

export interface ProductCreateRequest {
  sku: string
  name: string
  description?: string
  category: string
  price: number
  quantityInStock: number
  reorderLevel: number
  supplierId: number
}

export type ProductUpdateRequest = ProductCreateRequest
