import { api } from './client'
import type { PageDto } from '../types/common'
import type { ProductCreateRequest, ProductResponse, ProductUpdateRequest } from '../types/product'

export type ProductListParams = {
  page?: number
  size?: number
  search?: string
  sort?: string
}

export const productsApi = {
  list(params?: ProductListParams) {
    const query: Record<string, string | number> = {}
    if (params?.page != null) query.page = params.page
    if (params?.size != null) query.size = params.size
    if (params?.search?.trim()) query.search = params.search.trim()
    if (params?.sort) query.sort = params.sort
    return api
      .get<PageDto<ProductResponse> | ProductResponse[]>('/api/products', { params: query })
      .then((r) => {
        const data = r.data
        if (Array.isArray(data)) {
          return {
            content: data,
            totalElements: data.length,
            totalPages: 1,
            number: 0,
            size: data.length,
          } satisfies PageDto<ProductResponse>
        }
        return data
      })
  },
  get(id: number) {
    return api.get<ProductResponse>(`/api/products/${id}`).then((r) => r.data)
  },
  create(body: ProductCreateRequest) {
    return api.post<ProductResponse>('/api/products', body).then((r) => r.data)
  },
  update(id: number, body: ProductUpdateRequest) {
    return api.put<ProductResponse>(`/api/products/${id}`, body).then((r) => r.data)
  },
  delete(id: number) {
    return api.delete<void>(`/api/products/${id}`).then(() => undefined)
  },
}
