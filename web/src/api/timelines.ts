import type { CursorPage, Post } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const timelinesApi = {
  getLatest: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>('/timelines/latest', { params: { cursor, limit } })
      .then(unwrap),

  getHome: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>('/timelines/home', { params: { cursor, limit } })
      .then(unwrap),

  // Trending uses a numeric-offset cursor (still passed via nextCursor)
  getTrending: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>('/timelines/trending', { params: { cursor, limit } })
      .then(unwrap),
}
