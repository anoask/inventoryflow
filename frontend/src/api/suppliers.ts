import { api } from './client'
import type { PageDto } from '../types/common'
import type { SupplierCreateRequest, SupplierResponse, SupplierUpdateRequest } from '../types/supplier'

export type SupplierListParams = {
  page?: number
  size?: number
  search?: string
  sort?: string
}

export const suppliersApi = {
  list(params?: SupplierListParams) {
    const query: Record<string, string | number> = {}
    if (params?.page != null) query.page = params.page
    if (params?.size != null) query.size = params.size
    if (params?.search?.trim()) query.search = params.search.trim()
    if (params?.sort) query.sort = params.sort
    return api
      .get<PageDto<SupplierResponse> | SupplierResponse[]>('/api/suppliers', { params: query })
      .then((r) => {
        const data = r.data
        if (Array.isArray(data)) {
          return {
            content: data,
            totalElements: data.length,
            totalPages: 1,
            number: 0,
            size: data.length,
          } satisfies PageDto<SupplierResponse>
        }
        return data
      })
  },
  get(id: number) {
    return api.get<SupplierResponse>(`/api/suppliers/${id}`).then((r) => r.data)
  },
  create(body: SupplierCreateRequest) {
    return api.post<SupplierResponse>('/api/suppliers', body).then((r) => r.data)
  },
  update(id: number, body: SupplierUpdateRequest) {
    return api.put<SupplierResponse>(`/api/suppliers/${id}`, body).then((r) => r.data)
  },
  delete(id: number) {
    return api.delete<void>(`/api/suppliers/${id}`).then(() => undefined)
  },
}
