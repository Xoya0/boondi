import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Post } from '../types'
import { timelinesApi } from '../api/timelines'
import { authApi } from '../api/auth'
import { notificationsApi } from '../api/notifications'
import { useAuthStore } from '../store/authStore'
import PostCard from '../components/posts/PostCard'
import PostComposer from '../components/posts/PostComposer'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'

type FeedTab = 'latest' | 'home' | 'trending'

const UNREAD_POLL_MS = 30_000

export default function HomePage() {
  const navigate = useNavigate()
  const { user, refreshToken, logout } = useAuthStore()
  const [activeTab, setActiveTab] = useState<FeedTab>('latest')
  const [page, setPage] = useState<CursorPage<Post> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [unreadCount, setUnreadCount] = useState(0)

  const fetchTab = (tab: FeedTab, cursor?: string) =>
    tab === 'latest' ? timelinesApi.getLatest(cursor)
    : tab === 'home' ? timelinesApi.getHome(cursor)
    : timelinesApi.getTrending(cursor)

  const fetchFeed = async (tab: FeedTab) => {
    setLoading(true)
    setError(null)
    try {
      const result = await fetchTab(tab)
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

  // Unread notification badge (E7-07): poll every 30s, and refetch when the tab regains focus
  useEffect(() => {
    const refreshUnreadCount = () => {
      notificationsApi.getUnreadCount().then(setUnreadCount).catch(() => {})
    }
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') refreshUnreadCount()
    }

    refreshUnreadCount()
    const interval = setInterval(refreshUnreadCount, UNREAD_POLL_MS)
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('focus', refreshUnreadCount)

    return () => {
      clearInterval(interval)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
      window.removeEventListener('focus', refreshUnreadCount)
    }
  }, [])

  const switchTab = (tab: FeedTab) => {
    if (tab === activeTab) return
    setActiveTab(tab)
    setPage(null)
  }

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await fetchTab(activeTab, page.nextCursor ?? undefined)
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
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/search')}
            className="text-gray-500 hover:text-gray-900 cursor-pointer"
            title="Search"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M17 10.5A6.5 6.5 0 114 10.5a6.5 6.5 0 0113 0z" />
            </svg>
          </button>
          <button
            onClick={() => navigate('/bookmarks')}
            className="text-gray-500 hover:text-gray-900 cursor-pointer"
            title="Bookmarks"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
            </svg>
          </button>
          <button
            onClick={() => navigate('/notifications')}
            className="relative text-gray-500 hover:text-gray-900 cursor-pointer"
            title="Notifications"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[16px] h-4 flex items-center justify-center px-1">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>
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
        {(['latest', 'home', 'trending'] as FeedTab[]).map(tab => (
          <button
            key={tab}
            onClick={() => switchTab(tab)}
            className={`flex-1 py-3 text-sm font-medium transition cursor-pointer ${
              activeTab === tab
                ? 'text-gray-900 border-b-2 border-indigo-600'
                : 'text-gray-400 hover:text-gray-600'
            }`}
          >
            {tab === 'latest' ? 'Latest' : tab === 'home' ? 'Home' : 'Trending'}
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
                : activeTab === 'trending'
                ? 'Nothing trending in the last 24h yet.'
                : 'No posts yet. Be the first to post!'}
            </div>
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
