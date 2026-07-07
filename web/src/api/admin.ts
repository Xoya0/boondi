import type { CursorPage, Report, UserProfile } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const adminApi = {
  getUsers: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<UserProfile> }>('/admin/users', { params: { cursor, limit } })
      .then(unwrap),

  suspendUser: (userId: string) =>
    apiClient
      .put<{ data: UserProfile }>(`/admin/users/${userId}/suspend`)
      .then(unwrap),

  unsuspendUser: (userId: string) =>
    apiClient
      .put<{ data: UserProfile }>(`/admin/users/${userId}/unsuspend`)
      .then(unwrap),

  deletePost: (postId: string) =>
    apiClient.delete(`/admin/posts/${postId}`),

  getReports: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Report> }>('/admin/reports', { params: { cursor, limit } })
      .then(unwrap),
}
