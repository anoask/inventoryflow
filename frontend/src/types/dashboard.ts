export interface LowStockProductResponse {
  id: number
  sku: string
  name: string
  category: string
  price: string
  quantityInStock: number
  reorderLevel: number
  supplierId: number
  supplierName: string
}

export interface RecentPurchaseOrderDto {
  id: number
  status: string
  createdAt: string
  supplierName: string
  lineItemCount: number
}

export interface RecentSalesOrderDto {
  id: number
  status: string
  createdAt: string
  lineItemCount: number
}
