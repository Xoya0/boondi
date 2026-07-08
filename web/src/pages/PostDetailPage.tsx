import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import type { CursorPage, Post } from '../types'
import { postsApi } from '../api/posts'
import PostCard from '../components/posts/PostCard'
import PostComposer from '../components/posts/PostComposer'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'
import Spinner from '../components/shared/Spinner'
import EmptyState from '../components/shared/EmptyState'

export default function PostDetailPage() {
  const { postId } = useParams<{ postId: string }>()
  const navigate = useNavigate()

  const [post, setPost] = useState<Post | null>(null)
  const [repliesPage, setRepliesPage] = useState<CursorPage<Post> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadPost = () => {
    if (!postId) return
    setLoading(true)
    setError(null)
    setPost(null)
    setRepliesPage(null)
    Promise.all([postsApi.getPost(postId), postsApi.getReplies(postId)])
      .then(([p, replies]) => {
        setPost(p)
        setRepliesPage(replies)
      })
      .catch(() => setError('Post not found or failed to load.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadPost, [postId])

  const loadMoreReplies = async () => {
    if (!repliesPage?.hasMore || loadingMore || !postId) return
    setLoadingMore(true)
    try {
      const more = await postsApi.getReplies(postId, repliesPage.nextCursor ?? undefined)
      setRepliesPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  const handleReplyPosted = (reply: Post) => {
    // Replies are chronological (oldest first) — append
    setRepliesPage(prev =>
      prev ? { ...prev, items: [...prev.items, reply], count: prev.count + 1 } : prev
    )
    setPost(prev => (prev ? { ...prev, replyCount: prev.replyCount + 1 } : prev))
  }

  const handleReplyDeleted = (replyId: string) => {
    setRepliesPage(prev =>
      prev ? { ...prev, items: prev.items.filter(p => p.id !== replyId), count: prev.count - 1 } : prev
    )
    setPost(prev => (prev ? { ...prev, replyCount: Math.max(0, prev.replyCount - 1) } : prev))
  }

  const handlePostDeleted = () => {
    navigate('/home', { replace: true })
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner />
      </div>
    )
  }

  if (error || !post) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-stone-500 mb-4">{error ?? 'Post not found.'}</p>
          <div className="flex items-center justify-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="text-brand-600 text-sm hover:underline cursor-pointer"
            >
              ← Go back
            </button>
            <button
              onClick={loadPost}
              className="text-brand-600 text-sm hover:underline cursor-pointer"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-stone-100">
      {/* Back nav */}
      <div className="flex items-center gap-4 px-4 py-3 border-b border-stone-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <button
          onClick={() => navigate(-1)}
          className="text-stone-600 hover:text-stone-900 cursor-pointer"
          aria-label="Go back"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <p className="font-bold text-stone-900 text-sm">Post</p>
      </div>

      {/* If this post is itself a reply, offer a link to the parent */}
      {post.parentPostId && (
        <button
          onClick={() => navigate(`/post/${post.parentPostId}`)}
          className="w-full text-left px-4 py-2 text-xs text-brand-600 hover:bg-stone-50 border-b border-stone-100 cursor-pointer"
        >
          ← View the post this replies to
        </button>
      )}

      {/* The post itself */}
      <PostCard post={post} onDeleted={handlePostDeleted} linkToDetail={false} />

      {/* Reply composer */}
      <PostComposer
        parentPostId={post.id}
        placeholder="Post your reply"
        onPosted={handleReplyPosted}
      />

      {/* Replies */}
      <div className="border-b border-stone-100 px-4 py-2">
        <span className="font-medium text-stone-900 text-sm">
          Replies {post.replyCount > 0 && `(${post.replyCount})`}
        </span>
      </div>

      {repliesPage && repliesPage.items.length === 0 ? (
        <EmptyState icon="💬" message="No replies yet. Be the first to reply!" />
      ) : (
        <>
          {repliesPage?.items.map(reply => (
            <PostCard key={reply.id} post={reply} onDeleted={handleReplyDeleted} />
          ))}

          {repliesPage && (
            <InfiniteScrollSentinel
              hasMore={repliesPage.hasMore}
              loading={loadingMore}
              onLoadMore={loadMoreReplies}
            />
          )}
        </>
      )}
    </div>
  )
}
