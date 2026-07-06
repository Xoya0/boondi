import type { CursorPage, Post } from '../types'
import { apiClient } from './client'

function unwrap<T>(response: { data: { data: T } }): T {
  return response.data.data
}

export const postsApi = {
  createPost: (data: {
    content: string
    imageUrl?: string
    parentPostId?: string
    quotedPostId?: string
  }) => apiClient.post<{ data: Post }>('/posts', data).then(unwrap),

  getPost: (postId: string) =>
    apiClient.get<{ data: Post }>(`/posts/${postId}`).then(unwrap),

  getReplies: (postId: string, cursor?: string, limit = 20) =>
    apiClient
      .get<{ data: CursorPage<Post> }>(`/posts/${postId}/replies`, {
        params: { cursor, limit },
      })
      .then(unwrap),

  updatePost: (postId: string, data: { content: string; imageUrl?: string }) =>
    apiClient.put<{ data: Post }>(`/posts/${postId}`, data).then(unwrap),

  deletePost: (postId: string) =>
    apiClient.delete(`/posts/${postId}`),

  uploadImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient
      .post<{ data: { url: string } }>('/posts/images', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then(unwrap)
  },

  // ─── Interactions ─────────────────────────────────────────────────────────

  like: (postId: string) =>
    apiClient.post<{ data: Post }>(`/posts/${postId}/like`).then(unwrap),

  unlike: (postId: string) =>
    apiClient.delete<{ data: Post }>(`/posts/${postId}/like`).then(unwrap),

  repost: (postId: string) =>
    apiClient.post<{ data: Post }>(`/posts/${postId}/repost`).then(unwrap),

  unrepost: (postId: string) =>
    apiClient.delete<{ data: Post }>(`/posts/${postId}/repost`).then(unwrap),

  bookmark: (postId: string) =>
    apiClient.post<{ data: Post }>(`/posts/${postId}/bookmark`).then(unwrap),

  unbookmark: (postId: string) =>
    apiClient.delete<{ data: Post }>(`/posts/${postId}/bookmark`).then(unwrap),
}
