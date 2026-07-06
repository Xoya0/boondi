import { apiClient } from './client'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: {
    id: string
    username: string
    email: string
    displayName: string | null
    profilePictureUrl: string | null
    role: string
  }
}

export interface UserInfo {
  id: string
  username: string
  email: string
  displayName: string | null
  profilePictureUrl: string | null
  role: string
}

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const authApi = {
  register: (data: { username: string; email: string; password: string; displayName?: string }) =>
    apiClient.post<{ data: AuthResponse }>('/auth/register', data).then(unwrap),

  login: (data: { email: string; password: string }) =>
    apiClient.post<{ data: AuthResponse }>('/auth/login', data).then(unwrap),

  logout: (refreshToken: string) =>
    apiClient.post('/auth/logout', { refreshToken }),

  forgotPassword: (email: string) =>
    apiClient.post('/auth/forgot-password', { email }),

  resetPassword: (data: { token: string; newPassword: string }) =>
    apiClient.post('/auth/reset-password', data),
}
