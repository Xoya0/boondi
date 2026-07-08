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
import { PostListSkeleton } from '../components/shared/PostCardSkeleton'
import { currentTheme, toggleTheme } from '../theme'

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
  const [theme, setTheme] = useState(currentTheme)

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
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-stone-100">
      {/* Top nav */}
      <header className="flex items-center justify-between px-4 py-3 border-b border-stone-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <span className="font-bold text-brand-600 text-lg">Boondi</span>
        <div className="flex items-center gap-4">
          <button
            onClick={() => setTheme(toggleTheme())}
            className="text-stone-500 hover:text-stone-900 cursor-pointer"
            title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            {theme === 'dark' ? (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            ) : (
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            )}
          </button>
          <button
            onClick={() => navigate('/search')}
            className="text-stone-500 hover:text-stone-900 cursor-pointer"
            title="Search"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M17 10.5A6.5 6.5 0 114 10.5a6.5 6.5 0 0113 0z" />
            </svg>
          </button>
          <button
            onClick={() => navigate('/bookmarks')}
            className="text-stone-500 hover:text-stone-900 cursor-pointer"
            title="Bookmarks"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
            </svg>
          </button>
          <button
            onClick={() => navigate('/notifications')}
            className="relative text-stone-500 hover:text-stone-900 cursor-pointer"
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
          {user?.role === 'ADMIN' && (
            <button
              onClick={() => navigate('/admin')}
              className="text-stone-500 hover:text-stone-900 cursor-pointer"
              title="Admin panel"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </button>
          )}
          {user && (
            <button
              onClick={() => navigate(`/profile/${user.username}`)}
              className="text-sm text-stone-600 hover:text-stone-900 cursor-pointer"
            >
              @{user.username}
            </button>
          )}
          <button
            onClick={handleLogout}
            className="text-xs text-stone-400 hover:text-stone-600 cursor-pointer"
          >
            Sign out
          </button>
        </div>
      </header>

      {/* Feed tabs */}
      <div className="flex border-b border-stone-100">
        {(['latest', 'home', 'trending'] as FeedTab[]).map(tab => (
          <button
            key={tab}
            onClick={() => switchTab(tab)}
            className={`flex-1 py-3 text-sm font-medium transition cursor-pointer ${
              activeTab === tab
                ? 'text-stone-900 border-b-2 border-brand-600'
                : 'text-stone-400 hover:text-stone-600'
            }`}
          >
            {tab === 'latest' ? 'Latest' : tab === 'home' ? 'Home' : 'Trending'}
          </button>
        ))}
      </div>

      {/* Post composer */}
      <PostComposer onPosted={handlePostCreated} />

      {/* Feed content */}
      {loading && <PostListSkeleton />}

      {error && (
        <div className="text-center py-12">
          <p className="text-stone-400 text-sm mb-3">{error}</p>
          <button
            onClick={() => fetchFeed(activeTab)}
            className="text-brand-600 text-sm hover:underline cursor-pointer"
          >
            Retry
          </button>
        </div>
      )}

      {!loading && !error && page && (
        <>
          {page.items.length === 0 ? (
            <div className="text-center py-16 text-stone-400 text-sm">
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
