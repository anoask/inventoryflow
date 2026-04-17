import { api } from './client'
import type { SalesOrderCreateRequest, SalesOrderResponse, SalesOrderUpdateRequest } from '../types/salesOrder'

export const salesOrdersApi = {
  list() {
    return api.get<SalesOrderResponse[]>('/api/sales-orders').then((r) => r.data)
  },
  get(id: number) {
    return api.get<SalesOrderResponse>(`/api/sales-orders/${id}`).then((r) => r.data)
  },
  create(body: SalesOrderCreateRequest) {
    return api.post<SalesOrderResponse>('/api/sales-orders', body).then((r) => r.data)
  },
  update(id: number, body: SalesOrderUpdateRequest) {
    return api.put<SalesOrderResponse>(`/api/sales-orders/${id}`, body).then((r) => r.data)
  },
  delete(id: number) {
    return api.delete<void>(`/api/sales-orders/${id}`).then((r) => r.data)
  },
}
