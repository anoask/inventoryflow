import { api } from './client'
import type {
  PurchaseOrderCreateRequest,
  PurchaseOrderResponse,
  PurchaseOrderUpdateRequest,
} from '../types/purchaseOrder'

export const purchaseOrdersApi = {
  list() {
    return api.get<PurchaseOrderResponse[]>('/api/purchase-orders').then((r) => r.data)
  },
  get(id: number) {
    return api.get<PurchaseOrderResponse>(`/api/purchase-orders/${id}`).then((r) => r.data)
  },
  create(body: PurchaseOrderCreateRequest) {
    return api.post<PurchaseOrderResponse>('/api/purchase-orders', body).then((r) => r.data)
  },
  update(id: number, body: PurchaseOrderUpdateRequest) {
    return api.put<PurchaseOrderResponse>(`/api/purchase-orders/${id}`, body).then((r) => r.data)
  },
  delete(id: number) {
    return api.delete<void>(`/api/purchase-orders/${id}`).then((r) => r.data)
  },
  receive(id: number) {
    return api.patch<PurchaseOrderResponse>(`/api/purchase-orders/${id}/receive`).then((r) => r.data)
  },
}
