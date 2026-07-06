import type { CursorPage, Notification } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const notificationsApi = {
  getNotifications: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Notification> }>('/notifications', { params: { cursor, limit } })
      .then(unwrap),

  markAsRead: (notificationId: string) =>
    apiClient.put(`/notifications/${notificationId}/read`),

  markAllAsRead: () =>
    apiClient.put('/notifications/read-all'),

  getUnreadCount: () =>
    apiClient
      .get<{ data: { count: number } }>('/notifications/unread-count')
      .then(unwrap)
      .then(r => r.count),
}
