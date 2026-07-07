import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Report, UserProfile } from '../types'
import { adminApi } from '../api/admin'
import Avatar from '../components/shared/Avatar'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'

type Tab = 'users' | 'reports'

export default function AdminPage() {
  const navigate = useNavigate()
  const [tab, setTab] = useState<Tab>('users')

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-gray-100">
      <div className="flex items-center gap-4 px-4 py-3 border-b border-gray-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <button
          onClick={() => navigate('/home')}
          className="text-gray-600 hover:text-gray-900 cursor-pointer"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <p className="font-bold text-gray-900 text-sm">Admin</p>
      </div>

      <div className="flex border-b border-gray-100">
        {(['users', 'reports'] as const).map(t => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`flex-1 py-3 text-sm font-medium capitalize cursor-pointer transition ${
              tab === t
                ? 'text-indigo-600 border-b-2 border-indigo-600'
                : 'text-gray-500 hover:text-gray-800'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'users' ? <UsersTab /> : <ReportsTab />}
    </div>
  )
}

function UsersTab() {
  const [page, setPage] = useState<CursorPage<UserProfile> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    adminApi.getUsers()
      .then(setPage)
      .catch(() => setError('Failed to load users.'))
      .finally(() => setLoading(false))
  }, [])

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await adminApi.getUsers(page.nextCursor ?? undefined)
      setPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  const toggleSuspend = async (user: UserProfile) => {
    if (busyId) return
    setBusyId(user.id)
    try {
      const updated = user.suspended
        ? await adminApi.unsuspendUser(user.id)
        : await adminApi.suspendUser(user.id)
      setPage(prev =>
        prev ? { ...prev, items: prev.items.map(u => (u.id === updated.id ? updated : u)) } : prev
      )
    } catch {
      // leave state as-is; user can retry
    } finally {
      setBusyId(null)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full" />
      </div>
    )
  }
  if (error) {
    return <div className="text-center py-12 text-gray-400 text-sm">{error}</div>
  }
  if (!page || page.items.length === 0) {
    return <div className="text-center py-16 text-gray-400 text-sm">No users found.</div>
  }

  return (
    <>
      {page.items.map(user => (
        <div key={user.id} className="flex items-center gap-3 px-4 py-3 border-b border-gray-100">
          <Avatar src={user.profilePictureUrl} alt={user.displayName ?? user.username} size="sm" />
          <div className="flex-1 min-w-0">
            <p className="font-semibold text-gray-900 text-sm truncate">
              {user.displayName ?? user.username}
              {user.role === 'ADMIN' && (
                <span className="ml-2 text-[10px] font-bold text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded">
                  ADMIN
                </span>
              )}
            </p>
            <p className="text-gray-400 text-sm truncate">@{user.username} · {user.email}</p>
          </div>
          {user.suspended && (
            <span className="text-[10px] font-bold text-red-600 bg-red-50 px-1.5 py-0.5 rounded flex-shrink-0">
              SUSPENDED
            </span>
          )}
          <button
            onClick={() => toggleSuspend(user)}
            disabled={busyId === user.id}
            className={`flex-shrink-0 font-medium px-3 py-1.5 rounded-full text-sm transition cursor-pointer disabled:opacity-60 ${
              user.suspended
                ? 'bg-gray-900 hover:bg-gray-700 text-white'
                : 'border border-gray-300 hover:border-red-300 hover:text-red-500 text-gray-700'
            }`}
          >
            {busyId === user.id ? '…' : user.suspended ? 'Unsuspend' : 'Suspend'}
          </button>
        </div>
      ))}
      <InfiniteScrollSentinel hasMore={page.hasMore} loading={loadingMore} onLoadMore={loadMore} />
    </>
  )
}

function ReportsTab() {
  const navigate = useNavigate()
  const [page, setPage] = useState<CursorPage<Report> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    adminApi.getReports()
      .then(setPage)
      .catch(() => setError('Failed to load reports.'))
      .finally(() => setLoading(false))
  }, [])

  const loadMore = async () => {
    if (!page?.hasMore || loadingMore) return
    setLoadingMore(true)
    try {
      const more = await adminApi.getReports(page.nextCursor ?? undefined)
      setPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
    } finally {
      setLoadingMore(false)
    }
  }

  const handleDeletePost = async (report: Report) => {
    if (!report.reportedPost || busyId) return
    if (!confirm('Delete this post? This cannot be undone.')) return
    setBusyId(report.id)
    try {
      await adminApi.deletePost(report.reportedPost.id)
      setPage(prev => (prev ? { ...prev, items: prev.items.filter(r => r.id !== report.id) } : prev))
    } catch {
      // leave the report visible; admin can retry
    } finally {
      setBusyId(null)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full" />
      </div>
    )
  }
  if (error) {
    return <div className="text-center py-12 text-gray-400 text-sm">{error}</div>
  }
  if (!page || page.items.length === 0) {
    return <div className="text-center py-16 text-gray-400 text-sm">No reports.</div>
  }

  return (
    <>
      {page.items.map(report => (
        <div key={report.id} className="px-4 py-3 border-b border-gray-100">
          <p className="text-xs text-gray-400">
            Reported by @{report.reporter.username}
          </p>
          <p className="text-sm text-gray-900 mt-1">{report.reason}</p>

          {report.reportedUser && (
            <button
              onClick={() => navigate(`/profile/${report.reportedUser!.username}`)}
              className="mt-2 text-sm text-indigo-600 hover:underline cursor-pointer block"
            >
              View reported user: @{report.reportedUser.username}
            </button>
          )}

          {report.reportedPost && (
            <div className="mt-2 border border-gray-200 rounded-lg p-3">
              <p className="text-xs text-gray-400 mb-1">@{report.reportedPost.authorUsername}</p>
              <p className="text-sm text-gray-700 line-clamp-3">{report.reportedPost.contentPreview}</p>
              <div className="flex gap-3 mt-2">
                <button
                  onClick={() => navigate(`/post/${report.reportedPost!.id}`)}
                  className="text-sm text-indigo-600 hover:underline cursor-pointer"
                >
                  View post
                </button>
                <button
                  onClick={() => handleDeletePost(report)}
                  disabled={busyId === report.id}
                  className="text-sm text-red-500 hover:underline cursor-pointer disabled:opacity-60"
                >
                  {busyId === report.id ? 'Deleting…' : 'Delete post'}
                </button>
              </div>
            </div>
          )}

          <p className="text-xs text-gray-300 mt-2">
            {new Date(report.createdAt).toLocaleString()}
          </p>
        </div>
      ))}
      <InfiniteScrollSentinel hasMore={page.hasMore} loading={loadingMore} onLoadMore={loadMore} />
    </>
  )
}
