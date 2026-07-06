import type { CursorPage, Hashtag, Post, UserProfile } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const searchApi = {
  searchUsers: (q: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<UserProfile> }>('/search/users', { params: { q, cursor, limit } })
      .then(unwrap),

  searchPosts: (q: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>('/search/posts', { params: { q, cursor, limit } })
      .then(unwrap),

  searchHashtags: (q: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Hashtag> }>('/search/hashtags', { params: { q, cursor, limit } })
      .then(unwrap),
}
