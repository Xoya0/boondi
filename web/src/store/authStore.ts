import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface UserInfo {
  id: string
  username: string
  email: string
  displayName: string | null
  profilePictureUrl: string | null
  role: string
}

interface AuthState {
  user: UserInfo | null
  accessToken: string | null
  refreshToken: string | null
  setAuth: (user: UserInfo, accessToken: string, refreshToken: string) => void
  updateTokens: (accessToken: string, refreshToken: string) => void
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,

      setAuth: (user, accessToken, refreshToken) => {
        set({ user, accessToken, refreshToken })
      },

      updateTokens: (accessToken, refreshToken) => {
        set({ accessToken, refreshToken })
      },

      logout: () => {
        set({ user: null, accessToken: null, refreshToken: null })
      },

      isAuthenticated: () => get().accessToken !== null,
    }),
    {
      name: 'boondi-auth',
      // This store (and its zustand-managed localStorage persistence under the 'boondi-auth'
      // key) is the SOLE source of truth for tokens — api/client.ts reads/writes tokens via
      // useAuthStore.getState() rather than a separate raw localStorage key, so there's no
      // second copy that can drift out of sync after a silent token refresh.
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
      }),
    }
  )
)
