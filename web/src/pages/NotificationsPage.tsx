import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Notification } from '../types'
import { notificationsApi } from '../api/notifications'
import NotificationItem from '../components/notifications/NotificationItem'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'

export default function NotificationsPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState<CursorPage<Notification> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [markingAll, setMarkingAll] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError(null)
    notificationsApi.getNotifications()
      .then(setPage)
      .catch(() => setError('Failed to load notifications. Please refresh.'))
      .finally(() => setLoading(false))
  }, [])

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await notificationsApi.getNotifications(page.nextCursor ?? undefined)
      setPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  const handleRead = (id: string) => {
    setPage(prev =>
      prev ? { ...prev, items: prev.items.map(n => (n.id === id ? { ...n, read: true } : n)) } : prev
    )
  }

  const handleMarkAllRead = async () => {
    if (markingAll) return
    setMarkingAll(true)
    try {
      await notificationsApi.markAllAsRead()
      setPage(prev => (prev ? { ...prev, items: prev.items.map(n => ({ ...n, read: true })) } : prev))
    } finally {
      setMarkingAll(false)
    }
  }

  const hasUnread = page?.items.some(n => !n.read) ?? false

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-stone-100">
      {/* Back nav */}
      <div className="flex items-center justify-between gap-4 px-4 py-3 border-b border-stone-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate(-1)}
            className="text-stone-600 hover:text-stone-900 cursor-pointer"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <p className="font-bold text-stone-900 text-sm">Notifications</p>
        </div>
        {hasUnread && (
          <button
            onClick={handleMarkAllRead}
            disabled={markingAll}
            className="text-xs text-brand-600 hover:text-brand-800 font-medium cursor-pointer disabled:text-brand-400"
          >
            {markingAll ? 'Marking…' : 'Mark all as read'}
          </button>
        )}
      </div>

      {loading && (
        <div className="flex justify-center py-12">
          <div className="animate-spin w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full" />
        </div>
      )}

      {error && (
        <div className="text-center py-12">
          <p className="text-stone-400 text-sm">{error}</p>
        </div>
      )}

      {!loading && !error && page && (
        <>
          {page.items.length === 0 ? (
            <div className="text-center py-16 text-stone-400 text-sm">
              No notifications yet.
            </div>
          ) : (
            page.items.map(n => (
              <NotificationItem key={n.id} notification={n} onRead={handleRead} />
            ))
          )}

          <InfiniteScrollSentinel hasMore={page.hasMore} loading={loadingMore} onLoadMore={loadMore} />
        </>
      )}
    </div>
  )
}
