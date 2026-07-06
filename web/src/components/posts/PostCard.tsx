import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { Post } from '../../types'
import { formatRelativeTime } from '../../utils/time'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'

interface PostCardProps {
  post: Post
  onDeleted?: (postId: string) => void
  // When false, clicking the card body does not navigate (used on the detail page itself)
  linkToDetail?: boolean
}

function Avatar({ src, alt }: { src: string | null; alt: string }) {
  if (src) {
    return (
      <img
        src={src}
        alt={alt}
        className="w-10 h-10 rounded-full object-cover bg-gray-200 flex-shrink-0"
      />
    )
  }
  return (
    <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
      <span className="text-indigo-600 font-semibold text-sm">
        {alt.charAt(0).toUpperCase()}
      </span>
    </div>
  )
}

export default function PostCard({ post: initialPost, onDeleted, linkToDetail = true }: PostCardProps) {
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)
  const [post, setPost] = useState(initialPost)
  const [deleting, setDeleting] = useState(false)
  const [busy, setBusy] = useState(false)
  const [justLiked, setJustLiked] = useState(false)
  const isOwner = user?.id === post.author.id

  // Resync when the parent renders a different post into this card slot
  useEffect(() => setPost(initialPost), [initialPost])

  const handleDelete = async () => {
    if (!confirm('Delete this post?')) return
    setDeleting(true)
    try {
      await postsApi.deletePost(post.id)
      onDeleted?.(post.id)
    } catch {
      alert('Failed to delete post. Please try again.')
      setDeleting(false)
    }
  }

  const toggle = async (action: () => Promise<Post>) => {
    if (busy) return
    setBusy(true)
    try {
      const updated = await action()
      setPost(updated)
    } catch {
      // Conflict (double-click) or network error — refetch the true state
      try {
        setPost(await postsApi.getPost(post.id))
      } catch { /* keep current state */ }
    } finally {
      setBusy(false)
    }
  }

  const toggleLike = async () => {
    if (busy) return
    const original = post
    const wasLiked = original.likedByViewer

    // Optimistic update — flip immediately so the heart fills without waiting on the network
    setPost({
      ...original,
      likedByViewer: !wasLiked,
      likeCount: original.likeCount + (wasLiked ? -1 : 1),
    })
    if (!wasLiked) {
      setJustLiked(true)
      setTimeout(() => setJustLiked(false), 300)
    }

    setBusy(true)
    try {
      const updated = wasLiked ? await postsApi.unlike(original.id) : await postsApi.like(original.id)
      setPost(updated)
    } catch {
      setPost(original)
    } finally {
      setBusy(false)
    }
  }

  const toggleRepost = () =>
    toggle(() => (post.repostedByViewer ? postsApi.unrepost(post.id) : postsApi.repost(post.id)))
  const toggleBookmark = () =>
    toggle(() => (post.bookmarkedByViewer ? postsApi.unbookmark(post.id) : postsApi.bookmark(post.id)))

  const openDetail = () => {
    if (linkToDetail) navigate(`/post/${post.id}`)
  }

  return (
    <article className="flex gap-3 px-4 py-3 border-b border-gray-100 hover:bg-gray-50 transition-colors">
      {/* Avatar */}
      <Link to={`/profile/${post.author.username}`} className="flex-shrink-0">
        <Avatar
          src={post.author.profilePictureUrl}
          alt={post.author.displayName ?? post.author.username}
        />
      </Link>

      {/* Body */}
      <div className="flex-1 min-w-0">
        {/* Header row */}
        <div className="flex items-center gap-2 flex-wrap">
          <Link
            to={`/profile/${post.author.username}`}
            className="font-semibold text-gray-900 text-sm hover:underline"
          >
            {post.author.displayName ?? post.author.username}
          </Link>
          <Link
            to={`/profile/${post.author.username}`}
            className="text-gray-400 text-sm"
          >
            @{post.author.username}
          </Link>
          <span className="text-gray-300 text-xs">·</span>
          <span className="text-gray-400 text-xs" title={new Date(post.createdAt).toLocaleString()}>
            {formatRelativeTime(post.createdAt)}
          </span>
          {post.edited && (
            <span className="text-gray-300 text-xs">(edited)</span>
          )}
        </div>

        {/* Content */}
        <p
          onClick={openDetail}
          className={`text-gray-800 text-sm mt-1 whitespace-pre-wrap break-words leading-relaxed ${
            linkToDetail ? 'cursor-pointer' : ''
          }`}
        >
          {post.content}
        </p>

        {/* Image */}
        {post.imageUrl && (
          <img
            src={post.imageUrl}
            alt="Post image"
            className="mt-2 rounded-xl max-h-80 object-cover border border-gray-100"
          />
        )}

        {/* Quoted post embed */}
        {post.quotedPost && (
          <div
            onClick={e => {
              e.stopPropagation()
              navigate(`/post/${post.quotedPost!.id}`)
            }}
            className="mt-2 border border-gray-200 rounded-xl px-3 py-2 hover:bg-gray-100 transition-colors cursor-pointer"
          >
            <div className="flex items-center gap-1.5 text-xs">
              <span className="font-semibold text-gray-900">
                {post.quotedPost.author.displayName ?? post.quotedPost.author.username}
              </span>
              <span className="text-gray-400">@{post.quotedPost.author.username}</span>
              <span className="text-gray-300">·</span>
              <span className="text-gray-400">{formatRelativeTime(post.quotedPost.createdAt)}</span>
            </div>
            <p className="text-gray-700 text-sm mt-0.5 whitespace-pre-wrap break-words line-clamp-3">
              {post.quotedPost.content}
            </p>
            {post.quotedPost.imageUrl && (
              <img
                src={post.quotedPost.imageUrl}
                alt=""
                className="mt-1.5 rounded-lg max-h-40 object-cover border border-gray-100"
              />
            )}
          </div>
        )}

        {/* Action bar */}
        <div className="flex items-center gap-5 mt-2">
          {/* Reply — opens the post detail page */}
          <button
            onClick={openDetail}
            className="flex items-center gap-1.5 text-gray-400 hover:text-indigo-500 text-xs transition-colors cursor-pointer"
            title="Reply"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <span>{post.replyCount}</span>
          </button>

          {/* Repost */}
          <button
            onClick={toggleRepost}
            disabled={busy}
            className={`flex items-center gap-1.5 text-xs transition-colors cursor-pointer disabled:opacity-60 ${
              post.repostedByViewer ? 'text-green-600' : 'text-gray-400 hover:text-green-500'
            }`}
            title={post.repostedByViewer ? 'Undo repost' : 'Repost'}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>{post.repostCount}</span>
          </button>

          {/* Like */}
          <button
            onClick={toggleLike}
            disabled={busy}
            className={`flex items-center gap-1.5 text-xs transition-colors cursor-pointer disabled:opacity-60 ${
              post.likedByViewer ? 'text-red-500' : 'text-gray-400 hover:text-red-500'
            }`}
            title={post.likedByViewer ? 'Unlike' : 'Like'}
          >
            <svg
              className={`w-4 h-4 transition-transform duration-300 ${justLiked ? 'scale-125' : 'scale-100'}`}
              fill={post.likedByViewer ? 'currentColor' : 'none'}
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span>{post.likeCount}</span>
          </button>

          {/* Bookmark */}
          <button
            onClick={toggleBookmark}
            disabled={busy}
            className={`flex items-center gap-1.5 text-xs transition-colors cursor-pointer disabled:opacity-60 ${
              post.bookmarkedByViewer ? 'text-indigo-600' : 'text-gray-400 hover:text-indigo-500'
            }`}
            title={post.bookmarkedByViewer ? 'Remove bookmark' : 'Bookmark'}
          >
            <svg
              className="w-4 h-4"
              fill={post.bookmarkedByViewer ? 'currentColor' : 'none'}
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
            </svg>
            <span>{post.bookmarkCount}</span>
          </button>

          {isOwner && (
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="ml-auto text-gray-300 hover:text-red-400 text-xs transition-colors cursor-pointer disabled:opacity-50"
              title="Delete post"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          )}
        </div>
      </div>
    </article>
  )
}
