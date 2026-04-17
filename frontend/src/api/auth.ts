import { api } from './client'
import type { AuthResponse, LoginRequest } from '../types/auth'

export const authApi = {
  login(body: LoginRequest) {
    return api.post<AuthResponse>('/api/auth/login', body).then((r) => r.data)
  },
}
