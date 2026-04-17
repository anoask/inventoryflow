export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  email: string
  username: string
  roles: string[]
}

export interface StoredAuth {
  token: string
  email: string
  username: string
  roles: string[]
}
