import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { UserProfile } from '../../types'
import { usersApi } from '../../api/users'
import { useAuthStore } from '../../store/authStore'
import Avatar from './Avatar'

interface UserListItemProps {
  user: UserProfile
}

/** A single row in a followers/following/search-results list, with an inline follow toggle. */
export default function UserListItem({ user: initialUser }: UserListItemProps) {
  const navigate = useNavigate()
  const currentUser = useAuthStore(s => s.user)
  const [user, setUser] = useState(initialUser)
  const [busy, setBusy] = useState(false)
  const isSelf = currentUser?.username === user.username

  const toggleFollow = async () => {
    if (busy) return
    setBusy(true)
    try {
      const updated = user.followedByViewer
        ? await usersApi.unfollow(user.username)
        : await usersApi.follow(user.username)
      setUser(updated)
    } catch {
      try {
        setUser(await usersApi.getProfile(user.username))
      } catch { /* keep current state */ }
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex items-center gap-3 px-4 py-3 border-b border-stone-100 hover:bg-stone-50 transition-colors">
      <button onClick={() => navigate(`/profile/${user.username}`)} className="flex-shrink-0 cursor-pointer">
        <Avatar src={user.profilePictureUrl} alt={user.displayName ?? user.username} size="sm" />
      </button>

      <button
        onClick={() => navigate(`/profile/${user.username}`)}
        className="flex-1 min-w-0 text-left cursor-pointer"
      >
        <p className="font-semibold text-stone-900 text-sm truncate">
          {user.displayName ?? user.username}
        </p>
        <p className="text-stone-400 text-sm truncate">@{user.username}</p>
        {user.bio && <p className="text-stone-600 text-sm mt-0.5 line-clamp-2">{user.bio}</p>}
      </button>

      {!isSelf && (
        <button
          onClick={toggleFollow}
          disabled={busy}
          className={`flex-shrink-0 font-medium px-4 py-1.5 rounded-full text-sm transition cursor-pointer disabled:opacity-60 ${
            user.followedByViewer
              ? 'border border-stone-300 hover:border-red-300 hover:text-red-500 text-stone-700'
              : 'bg-stone-900 hover:bg-stone-700 text-white'
          }`}
        >
          {busy ? '…' : user.followedByViewer ? 'Following' : 'Follow'}
        </button>
      )}
    </div>
  )
}
