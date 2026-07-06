import type { CursorPage, Post, UserProfile } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const usersApi = {
  getProfile: (username: string) =>
    apiClient.get<{ data: UserProfile }>(`/users/${username}`).then(unwrap),

  updateProfile: (data: { displayName?: string; bio?: string; username?: string }) =>
    apiClient.put<{ data: UserProfile }>('/users/me', data).then(unwrap),

  uploadAvatar: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient
      .post<{ data: { url: string } }>('/users/me/avatar', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(unwrap)
  },

  uploadBanner: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient
      .post<{ data: { url: string } }>('/users/me/banner', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(unwrap)
  },

  getUserPosts: (username: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>(`/users/${username}/posts`, {
        params: { cursor, limit },
      })
      .then(unwrap),

  follow: (username: string) =>
    apiClient.post<{ data: UserProfile }>(`/users/${username}/follow`).then(unwrap),

  unfollow: (username: string) =>
    apiClient.delete<{ data: UserProfile }>(`/users/${username}/follow`).then(unwrap),

  getFollowers: (username: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<UserProfile> }>(`/users/${username}/followers`, {
        params: { cursor, limit },
      })
      .then(unwrap),

  getFollowing: (username: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<UserProfile> }>(`/users/${username}/following`, {
        params: { cursor, limit },
      })
      .then(unwrap),

  getMyBookmarks: (cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>('/users/me/bookmarks', {
        params: { cursor, limit },
      })
      .then(unwrap),
}
