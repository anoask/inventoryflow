export interface SalesOrderItemResponse {
  productId: number
  sku: string
  name: string
  quantity: number
  unitPrice: string
}

export interface SalesOrderResponse {
  id: number
  status: string
  createdAt: string
  items: SalesOrderItemResponse[]
}

export interface SalesOrderItemCreateRequest {
  productId: number
  quantity: number
}

export interface SalesOrderCreateRequest {
  items: SalesOrderItemCreateRequest[]
}

export type SalesOrderUpdateRequest = SalesOrderCreateRequest
