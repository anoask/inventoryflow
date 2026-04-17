export interface PurchaseOrderItemResponse {
  productId: number
  sku: string
  name: string
  quantity: number
  unitPrice: string
}

export interface PurchaseOrderResponse {
  id: number
  status: string
  createdAt: string
  supplierId: number
  supplierName: string
  items: PurchaseOrderItemResponse[]
}

export interface PurchaseOrderItemCreateRequest {
  productId: number
  quantity: number
  unitPrice?: number | null
}

export interface PurchaseOrderCreateRequest {
  supplierId: number
  items: PurchaseOrderItemCreateRequest[]
}

export type PurchaseOrderUpdateRequest = PurchaseOrderCreateRequest
