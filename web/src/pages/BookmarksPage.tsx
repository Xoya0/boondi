import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Post } from '../types'
import { usersApi } from '../api/users'
import PostCard from '../components/posts/PostCard'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'
import { PostListSkeleton } from '../components/shared/PostCardSkeleton'
import EmptyState from '../components/shared/EmptyState'
import ErrorState from '../components/shared/ErrorState'

export default function BookmarksPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState<CursorPage<Post> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadBookmarks = () => {
    setLoading(true)
    setError(null)
    usersApi.getMyBookmarks()
      .then(setPage)
      .catch(() => setError('Failed to load bookmarks. Please refresh.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadBookmarks, [])

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await usersApi.getMyBookmarks(page.nextCursor ?? undefined)
      setPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  // Only a real post deletion removes the row here — unbookmarking (still the post's own
  // action, not this page's) leaves it in place until the next load, same as other feeds
  const handlePostDeleted = (postId: string) => {
    setPage(prev =>
      prev ? { ...prev, items: prev.items.filter(p => p.id !== postId), count: prev.count - 1 } : prev
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
        <p className="font-bold text-stone-900 text-sm">Bookmarks</p>
      </div>

      {loading && <PostListSkeleton />}

      {error && <ErrorState message={error} onRetry={loadBookmarks} />}

      {!loading && !error && page && (
        <>
          {page.items.length === 0 ? (
            <EmptyState icon="🔖" message="No bookmarks yet. Tap the bookmark icon on any post to save it here." />
          ) : (
            page.items.map(post => (
              <PostCard key={post.id} post={post} onDeleted={handlePostDeleted} />
            ))
          )}

          <InfiniteScrollSentinel hasMore={page.hasMore} loading={loadingMore} onLoadMore={loadMore} />
        </>
      )}
    </div>
  )
}
