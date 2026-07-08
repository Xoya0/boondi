import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { Post } from '../../types'
import { formatRelativeTime } from '../../utils/time'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import Avatar from '../shared/Avatar'
import QuotedPostPreview from './QuotedPostPreview'
import QuoteComposerModal from './QuoteComposerModal'

interface PostCardProps {
  post: Post
  onDeleted?: (postId: string) => void
  // When false, clicking the card body does not navigate (used on the detail page itself)
  linkToDetail?: boolean
}

export default function PostCard({ post: initialPost, onDeleted, linkToDetail = true }: PostCardProps) {
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)
  const [post, setPost] = useState(initialPost)
  const [deleting, setDeleting] = useState(false)
  const [busy, setBusy] = useState(false)
  const [justLiked, setJustLiked] = useState(false)
  const [repostMenuOpen, setRepostMenuOpen] = useState(false)
  const [showQuoteComposer, setShowQuoteComposer] = useState(false)
  const repostMenuRef = useRef<HTMLDivElement>(null)
  const isOwner = user?.id === post.author.id

  // Resync when the parent renders a different post into this card slot
  useEffect(() => setPost(initialPost), [initialPost])

  // Close the repost/quote menu on outside click
  useEffect(() => {
    if (!repostMenuOpen) return
    const handleClickOutside = (e: MouseEvent) => {
      if (repostMenuRef.current && !repostMenuRef.current.contains(e.target as Node)) {
        setRepostMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [repostMenuOpen])

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

  const handleRepostButtonClick = () => {
    if (post.repostedByViewer) {
      // Already reposted — clicking again undoes it directly, no menu needed
      toggle(() => postsApi.unrepost(post.id))
      return
    }
    setRepostMenuOpen(open => !open)
  }

  const handleRepost = () => {
    setRepostMenuOpen(false)
    toggle(() => postsApi.repost(post.id))
  }

  const handleQuote = () => {
    setRepostMenuOpen(false)
    setShowQuoteComposer(true)
  }

  const toggleBookmark = () =>
    toggle(() => (post.bookmarkedByViewer ? postsApi.unbookmark(post.id) : postsApi.bookmark(post.id)))

  const openDetail = () => {
    if (linkToDetail) navigate(`/post/${post.id}`)
  }

  return (
    <article className="flex gap-3 px-4 py-3 border-b border-stone-100 hover:bg-stone-50 transition-colors">
      {/* Avatar */}
      <Link to={`/profile/${post.author.username}`} className="flex-shrink-0">
        <Avatar
          src={post.author.profilePictureUrl}
          alt={post.author.displayName ?? post.author.username}
          size="sm"
        />
      </Link>

      {/* Body */}
      <div className="flex-1 min-w-0">
        {/* Header row */}
        <div className="flex items-center gap-2 flex-wrap">
          <Link
            to={`/profile/${post.author.username}`}
            className="font-semibold text-stone-900 text-sm hover:underline"
          >
            {post.author.displayName ?? post.author.username}
          </Link>
          <Link
            to={`/profile/${post.author.username}`}
            className="text-stone-400 text-sm"
          >
            @{post.author.username}
          </Link>
          <span className="text-stone-300 text-xs">·</span>
          <span className="text-stone-400 text-xs" title={new Date(post.createdAt).toLocaleString()}>
            {formatRelativeTime(post.createdAt)}
          </span>
          {post.edited && (
            <span className="text-stone-300 text-xs">(edited)</span>
          )}
        </div>

        {/* Content */}
        <p
          onClick={openDetail}
          className={`text-stone-800 text-sm mt-1 whitespace-pre-wrap break-words leading-relaxed ${
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
            className="mt-2 rounded-xl max-h-80 object-cover border border-stone-100"
          />
        )}

        {/* Quoted post embed */}
        {post.quotedPost && (
          <div className="mt-2">
            <QuotedPostPreview
              quotedPost={post.quotedPost}
              onClick={e => {
                e.stopPropagation()
                navigate(`/post/${post.quotedPost!.id}`)
              }}
            />
          </div>
        )}

        {/* Action bar */}
        <div className="flex items-center gap-5 mt-2">
          {/* Reply — opens the post detail page */}
          <button
            onClick={openDetail}
            className="flex items-center gap-1.5 text-stone-400 hover:text-brand-500 text-xs transition-colors cursor-pointer"
            title="Reply"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <span>{post.replyCount}</span>
          </button>

          {/* Repost / Quote dropdown (E6-13) */}
          <div className="relative" ref={repostMenuRef}>
            <button
              onClick={handleRepostButtonClick}
              disabled={busy}
              className={`flex items-center gap-1.5 text-xs transition-colors cursor-pointer disabled:opacity-60 ${
                post.repostedByViewer ? 'text-green-600' : 'text-stone-400 hover:text-green-500'
              }`}
              title={post.repostedByViewer ? 'Undo repost' : 'Repost or quote'}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span>{post.repostCount}</span>
            </button>

            {repostMenuOpen && (
              <div className="absolute bottom-full left-0 mb-1 w-36 bg-white border border-stone-200 rounded-xl shadow-lg py-1 z-20">
                <button
                  onClick={handleRepost}
                  className="w-full text-left px-4 py-2 text-sm text-stone-700 hover:bg-stone-50 cursor-pointer"
                >
                  Repost
                </button>
                <button
                  onClick={handleQuote}
                  className="w-full text-left px-4 py-2 text-sm text-stone-700 hover:bg-stone-50 cursor-pointer"
                >
                  Quote
                </button>
              </div>
            )}
          </div>

          {/* Like */}
          <button
            onClick={toggleLike}
            disabled={busy}
            className={`flex items-center gap-1.5 text-xs transition-colors cursor-pointer disabled:opacity-60 ${
              post.likedByViewer ? 'text-rose-500' : 'text-stone-400 hover:text-rose-500'
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
              post.bookmarkedByViewer ? 'text-brand-600' : 'text-stone-400 hover:text-brand-500'
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
              className="ml-auto text-stone-300 hover:text-red-400 text-xs transition-colors cursor-pointer disabled:opacity-50"
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

      {showQuoteComposer && (
        <QuoteComposerModal post={post} onClose={() => setShowQuoteComposer(false)} />
      )}
    </article>
  )
}
