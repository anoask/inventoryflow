import { api } from './client'
import type {
  LowStockProductResponse,
  RecentPurchaseOrderDto,
  RecentSalesOrderDto,
} from '../types/dashboard'

export const dashboardApi = {
  lowStock() {
    return api.get<LowStockProductResponse[]>('/api/dashboard/low-stock').then((r) => r.data)
  },
  recentPurchaseOrders() {
    return api
      .get<RecentPurchaseOrderDto[]>('/api/dashboard/recent-purchase-orders')
      .then((r) => r.data)
  },
  recentSalesOrders() {
    return api
      .get<RecentSalesOrderDto[]>('/api/dashboard/recent-sales-orders')
      .then((r) => r.data)
  },
}
