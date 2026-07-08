import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import type { CursorPage, Post, UserProfile } from '../types'
import { usersApi } from '../api/users'
import { useAuthStore } from '../store/authStore'
import PostCard from '../components/posts/PostCard'
import EditProfileModal from '../components/profile/EditProfileModal'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'
import Spinner from '../components/shared/Spinner'
import EmptyState from '../components/shared/EmptyState'
import { formatFullDate } from '../utils/time'

function StatPill({ value, label, to }: { value: number; label: string; to: string }) {
  return (
    <Link to={to} className="text-sm hover:underline">
      <span className="font-bold text-stone-900">{value.toLocaleString()}</span>{' '}
      <span className="text-stone-500">{label}</span>
    </Link>
  )
}

export default function ProfilePage() {
  const { username } = useParams<{ username: string }>()
  const navigate = useNavigate()
  const currentUser = useAuthStore(s => s.user)
  const isOwnProfile = currentUser?.username === username

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [postsPage, setPostsPage] = useState<CursorPage<Post> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showEditModal, setShowEditModal] = useState(false)
  const [followBusy, setFollowBusy] = useState(false)

  const loadProfile = () => {
    if (!username) return
    setLoading(true)
    setError(null)
    Promise.all([
      usersApi.getProfile(username),
      usersApi.getUserPosts(username),
    ])
      .then(([prof, posts]) => {
        setProfile(prof)
        setPostsPage(posts)
      })
      .catch(() => setError('User not found or failed to load profile.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadProfile, [username])

  const loadMorePosts = async () => {
    if (!postsPage?.hasMore || loadingMore || !username) return
    setLoadingMore(true)
    try {
      const more = await usersApi.getUserPosts(username, postsPage.nextCursor ?? undefined)
      setPostsPage(prev => prev
        ? { ...more, items: [...prev.items, ...more.items] }
        : more
      )
    } finally {
      setLoadingMore(false)
    }
  }

  const handlePostDeleted = (postId: string) => {
    setPostsPage(prev =>
      prev ? { ...prev, items: prev.items.filter(p => p.id !== postId), count: prev.count - 1 } : prev
    )
    if (profile) setProfile({ ...profile, postCount: Math.max(0, profile.postCount - 1) })
  }

  const toggleFollow = async () => {
    if (!profile || followBusy) return
    setFollowBusy(true)
    try {
      const updated = profile.followedByViewer
        ? await usersApi.unfollow(profile.username)
        : await usersApi.follow(profile.username)
      setProfile(updated)
    } catch {
      // Conflict (double-click) or network error — refetch the true state
      try {
        setProfile(await usersApi.getProfile(profile.username))
      } catch { /* keep current state */ }
    } finally {
      setFollowBusy(false)
    }
  }

  const handleProfileSaved = (updated: UserProfile) => {
    setProfile(updated)
    setShowEditModal(false)
    // If username changed, redirect to new profile URL
    if (updated.username !== username) {
      navigate(`/profile/${updated.username}`, { replace: true })
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner />
      </div>
    )
  }

  if (error || !profile) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-stone-500 mb-4">{error ?? 'Profile not found.'}</p>
          <div className="flex items-center justify-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="text-brand-600 text-sm hover:underline cursor-pointer"
            >
              ← Go back
            </button>
            <button
              onClick={loadProfile}
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
        <div>
          <p className="font-bold text-stone-900 text-sm">
            {profile.displayName ?? profile.username}
          </p>
          <p className="text-stone-500 text-xs">{profile.postCount} posts</p>
        </div>
      </div>

      {/* Banner */}
      <div className="h-36 bg-gradient-to-r from-brand-100 to-purple-100">
        {profile.bannerImageUrl && (
          <img
            src={profile.bannerImageUrl}
            alt="Banner"
            className="w-full h-full object-cover"
          />
        )}
      </div>

      {/* Profile header */}
      <div className="px-4 pb-4">
        <div className="flex justify-between items-start -mt-10 mb-3">
          {/* Avatar */}
          <div className="w-20 h-20 rounded-full border-4 border-white bg-brand-100 flex items-center justify-center overflow-hidden">
            {profile.profilePictureUrl ? (
              <img src={profile.profilePictureUrl} alt="" className="w-full h-full object-cover" />
            ) : (
              <span className="text-brand-600 font-bold text-2xl">
                {(profile.displayName ?? profile.username).charAt(0).toUpperCase()}
              </span>
            )}
          </div>

          {/* Edit / Follow button */}
          {isOwnProfile ? (
            <button
              onClick={() => setShowEditModal(true)}
              className="mt-12 border border-stone-300 hover:border-stone-400 text-stone-700 font-medium px-4 py-1.5 rounded-full text-sm transition cursor-pointer"
            >
              Edit profile
            </button>
          ) : (
            <button
              onClick={toggleFollow}
              disabled={followBusy}
              className={`mt-12 font-medium px-4 py-1.5 rounded-full text-sm transition cursor-pointer disabled:opacity-60 ${
                profile.followedByViewer
                  ? 'border border-stone-300 hover:border-red-300 hover:text-red-500 text-stone-700'
                  : 'bg-stone-900 hover:bg-stone-700 text-white'
              }`}
            >
              {followBusy ? '…' : profile.followedByViewer ? 'Following' : 'Follow'}
            </button>
          )}
        </div>

        {/* Name & handle */}
        <div className="mb-2">
          <p className="font-bold text-stone-900 text-lg leading-tight">
            {profile.displayName ?? profile.username}
          </p>
          <p className="text-stone-500 text-sm">@{profile.username}</p>
        </div>

        {/* Bio */}
        {profile.bio && (
          <p className="text-stone-700 text-sm mb-2 leading-relaxed">{profile.bio}</p>
        )}

        {/* Joined date */}
        <p className="text-stone-500 text-xs mb-3">
          Joined {formatFullDate(profile.createdAt)}
        </p>

        {/* Stats */}
        <div className="flex gap-4">
          <StatPill value={profile.followingCount} label="Following" to={`/profile/${profile.username}/following`} />
          <StatPill value={profile.followerCount} label="Followers" to={`/profile/${profile.username}/followers`} />
        </div>
      </div>

      {/* Posts tab header */}
      <div className="border-b border-stone-100">
        <div className="px-4 py-3 border-b-2 border-brand-600 inline-block">
          <span className="font-medium text-stone-900 text-sm">Posts</span>
        </div>
      </div>

      {/* Posts list */}
      {postsPage && postsPage.items.length === 0 ? (
        <EmptyState icon="📝" message="No posts yet." />
      ) : (
        <>
          {postsPage?.items.map(post => (
            <PostCard key={post.id} post={post} onDeleted={handlePostDeleted} />
          ))}

          {postsPage && (
            <InfiniteScrollSentinel
              hasMore={postsPage.hasMore}
              loading={loadingMore}
              onLoadMore={loadMorePosts}
            />
          )}
        </>
      )}

      {/* Edit Profile Modal */}
      {showEditModal && (
        <EditProfileModal
          profile={profile}
          onClose={() => setShowEditModal(false)}
          onSaved={handleProfileSaved}
        />
      )}
    </div>
  )
}
