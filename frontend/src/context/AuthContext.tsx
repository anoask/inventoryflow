import {
  createContext,
  useCallback,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { AUTH_KEY } from '../api/client'
import { authApi } from '../api/auth'
import type { StoredAuth } from '../types/auth'

export type AuthContextValue = {
  user: StoredAuth | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  isAdmin: boolean
}

export const AuthContext = createContext<AuthContextValue | null>(null)

function readStored(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(AUTH_KEY)
    if (!raw) return null
    return JSON.parse(raw) as StoredAuth
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<StoredAuth | null>(() => readStored())

  const login = useCallback(async (email: string, password: string) => {
    const res = await authApi.login({ email, password })
    const next: StoredAuth = {
      token: res.token,
      email: res.email,
      username: res.username,
      roles: res.roles ?? [],
    }
    localStorage.setItem(AUTH_KEY, JSON.stringify(next))
    setUser(next)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_KEY)
    setUser(null)
  }, [])

  const isAdmin = useMemo(
    () => Boolean(user?.roles?.includes('ADMIN')),
    [user]
  )

  const value = useMemo(
    () => ({
      user,
      login,
      logout,
      isAdmin,
    }),
    [user, login, logout, isAdmin]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
