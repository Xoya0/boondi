import { useEffect, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import type { CursorPage, UserProfile } from '../types'
import { usersApi } from '../api/users'
import UserListItem from '../components/shared/UserListItem'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'

/** Renders /profile/:username/followers or /profile/:username/following (E6-15). */
export default function FollowListPage() {
  const { username } = useParams<{ username: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const mode: 'followers' | 'following' = location.pathname.endsWith('/following') ? 'following' : 'followers'

  const [page, setPage] = useState<CursorPage<UserProfile> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchPage = (cursor?: string) =>
    mode === 'followers'
      ? usersApi.getFollowers(username!, cursor)
      : usersApi.getFollowing(username!, cursor)

  useEffect(() => {
    if (!username) return
    setLoading(true)
    setError(null)
    setPage(null)
    fetchPage()
      .then(setPage)
      .catch(() => setError('Failed to load. Please refresh.'))
      .finally(() => setLoading(false))
  }, [username, mode])

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await fetchPage(page.nextCursor ?? undefined)
      setPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-stone-100">
      {/* Back nav */}
      <div className="flex items-center gap-4 px-4 py-3 border-b border-stone-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <button
          onClick={() => navigate(-1)}
          className="text-stone-600 hover:text-stone-900 cursor-pointer"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div>
          <p className="font-bold text-stone-900 text-sm">@{username}</p>
          <p className="text-stone-400 text-xs">{mode === 'followers' ? 'Followers' : 'Following'}</p>
        </div>
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
              {mode === 'followers' ? 'No followers yet.' : 'Not following anyone yet.'}
            </div>
          ) : (
            page.items.map(u => <UserListItem key={u.id} user={u} />)
          )}

          <InfiniteScrollSentinel hasMore={page.hasMore} loading={loadingMore} onLoadMore={loadMore} />
        </>
      )}
    </div>
  )
}
