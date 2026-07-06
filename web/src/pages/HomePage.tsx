import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Post } from '../types'
import { timelinesApi } from '../api/timelines'
import { authApi } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import PostCard from '../components/posts/PostCard'
import PostComposer from '../components/posts/PostComposer'

type FeedTab = 'latest' | 'home'

export default function HomePage() {
  const navigate = useNavigate()
  const { user, refreshToken, logout } = useAuthStore()
  const [activeTab, setActiveTab] = useState<FeedTab>('latest')
  const [page, setPage] = useState<CursorPage<Post> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchFeed = async (tab: FeedTab) => {
    setLoading(true)
    setError(null)
    try {
      const result = tab === 'latest'
        ? await timelinesApi.getLatest()
        : await timelinesApi.getHome()
      setPage(result)
    } catch {
      setError('Failed to load feed. Please refresh.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchFeed(activeTab)
  }, [activeTab])

  const switchTab = (tab: FeedTab) => {
    if (tab === activeTab) return
    setActiveTab(tab)
    setPage(null)
  }

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = activeTab === 'latest'
        ? await timelinesApi.getLatest(page.nextCursor ?? undefined)
        : await timelinesApi.getHome(page.nextCursor ?? undefined)
      setPage(prev =>
        prev ? { ...more, items: [...prev.items, ...more.items] } : more
      )
    } finally {
      setLoadingMore(false)
    }
  }

  const handlePostCreated = (post: Post) => {
    setPage(prev =>
      prev ? { ...prev, items: [post, ...prev.items], count: prev.count + 1 } : null
    )
  }

  const handlePostDeleted = (postId: string) => {
    setPage(prev =>
      prev ? { ...prev, items: prev.items.filter(p => p.id !== postId), count: prev.count - 1 } : prev
    )
  }

  const handleLogout = async () => {
    try {
      if (refreshToken) await authApi.logout(refreshToken)
    } finally {
      logout()
      navigate('/login', { replace: true })
    }
  }

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-gray-100">
      {/* Top nav */}
      <header className="flex items-center justify-between px-4 py-3 border-b border-gray-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <span className="font-bold text-indigo-600 text-lg">Boondi</span>
        <div className="flex items-center gap-3">
          {user && (
            <button
              onClick={() => navigate(`/profile/${user.username}`)}
              className="text-sm text-gray-600 hover:text-gray-900 cursor-pointer"
            >
              @{user.username}
            </button>
          )}
          <button
            onClick={handleLogout}
            className="text-xs text-gray-400 hover:text-gray-600 cursor-pointer"
          >
            Sign out
          </button>
        </div>
      </header>

      {/* Feed tabs */}
      <div className="flex border-b border-gray-100">
        {(['latest', 'home'] as FeedTab[]).map(tab => (
          <button
            key={tab}
            onClick={() => switchTab(tab)}
            className={`flex-1 py-3 text-sm font-medium transition cursor-pointer ${
              activeTab === tab
                ? 'text-gray-900 border-b-2 border-indigo-600'
                : 'text-gray-400 hover:text-gray-600'
            }`}
          >
            {tab === 'latest' ? 'Latest' : 'Home'}
          </button>
        ))}
      </div>

      {/* Post composer */}
      <PostComposer onPosted={handlePostCreated} />

      {/* Feed content */}
      {loading && (
        <div className="flex justify-center py-12">
          <div className="animate-spin w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full" />
        </div>
      )}

      {error && (
        <div className="text-center py-12">
          <p className="text-gray-400 text-sm mb-3">{error}</p>
          <button
            onClick={() => fetchFeed(activeTab)}
            className="text-indigo-600 text-sm hover:underline cursor-pointer"
          >
            Retry
          </button>
        </div>
      )}

      {!loading && !error && page && (
        <>
          {page.items.length === 0 ? (
            <div className="text-center py-16 text-gray-400 text-sm">
              {activeTab === 'home'
                ? 'Follow some users to see their posts here.'
                : 'No posts yet. Be the first to post!'}
            </div>
          ) : (
            page.items.map(post => (
              <PostCard key={post.id} post={post} onDeleted={handlePostDeleted} />
            ))
          )}

          {page.hasMore && (
            <div className="flex justify-center py-4">
              <button
                onClick={loadMore}
                disabled={loadingMore}
                className="text-indigo-600 hover:text-indigo-800 text-sm font-medium cursor-pointer disabled:text-indigo-400"
              >
                {loadingMore ? 'Loading…' : 'Load more'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
