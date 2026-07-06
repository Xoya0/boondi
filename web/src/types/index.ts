export interface UserInfo {
  id: string
  username: string
  email: string
  displayName: string | null
  profilePictureUrl: string | null
  role: string
}

export interface UserProfile {
  id: string
  username: string
  email: string
  displayName: string | null
  bio: string | null
  profilePictureUrl: string | null
  bannerImageUrl: string | null
  role: string
  emailVerified: boolean
  followerCount: number
  followingCount: number
  postCount: number
  createdAt: string
  // Null/undefined for anonymous requests or own profile
  followedByViewer?: boolean | null
}

export interface PostAuthor {
  id: string
  username: string
  displayName: string | null
  profilePictureUrl: string | null
}

export interface QuotedPost {
  id: string
  content: string
  imageUrl: string | null
  author: PostAuthor
  createdAt: string
}

export interface Post {
  id: string
  content: string
  imageUrl: string | null
  author: PostAuthor
  likeCount: number
  repostCount: number
  replyCount: number
  bookmarkCount: number
  parentPostId: string | null
  quotedPost: QuotedPost | null
  edited: boolean
  editedAt: string | null
  createdAt: string
  updatedAt: string
  likedByViewer: boolean
  repostedByViewer: boolean
  bookmarkedByViewer: boolean
}

export type NotificationType = 'LIKE' | 'REPOST' | 'REPLY' | 'FOLLOW'

export interface Notification {
  id: string
  type: NotificationType
  actor: PostAuthor
  // Null for FOLLOW notifications
  postId: string | null
  postContentPreview: string | null
  read: boolean
  createdAt: string
}

export interface Hashtag {
  tag: string
  postCount: number
}

export interface CursorPage<T> {
  items: T[]
  nextCursor: string | null
  hasMore: boolean
  count: number
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  errorCode?: string
}
