import { useNavigate } from 'react-router-dom'
import type { Notification } from '../../types'
import { formatRelativeTime } from '../../utils/time'
import { notificationsApi } from '../../api/notifications'
import Avatar from '../shared/Avatar'

interface NotificationItemProps {
  notification: Notification
  onRead: (id: string) => void
}

const ACTION_TEXT: Record<Notification['type'], string> = {
  LIKE: 'liked your post',
  REPOST: 'reposted your post',
  REPLY: 'replied to your post',
  FOLLOW: 'followed you',
}

export default function NotificationItem({ notification, onRead }: NotificationItemProps) {
  const navigate = useNavigate()

  const handleClick = () => {
    if (!notification.read) {
      onRead(notification.id)
      notificationsApi.markAsRead(notification.id).catch(() => {
        // Best-effort — the read state already flipped locally
      })
    }
    if (notification.postId) {
      navigate(`/post/${notification.postId}`)
    } else {
      navigate(`/profile/${notification.actor.username}`)
    }
  }

  return (
    <button
      onClick={handleClick}
      className={`w-full flex items-start gap-3 px-4 py-3 border-b border-stone-100 text-left cursor-pointer transition-colors ${
        notification.read ? 'hover:bg-stone-50' : 'bg-brand-50/50 hover:bg-brand-50'
      }`}
    >
      <Avatar
        src={notification.actor.profilePictureUrl}
        alt={notification.actor.displayName ?? notification.actor.username}
        size="sm"
      />

      <div className="flex-1 min-w-0">
        {!notification.read && <span className="sr-only">Unread: </span>}
        <p className="text-sm text-stone-800">
          <span className="font-semibold text-stone-900">
            {notification.actor.displayName ?? notification.actor.username}
          </span>{' '}
          {ACTION_TEXT[notification.type]}
        </p>
        {notification.postContentPreview && (
          <p className="text-stone-500 text-sm mt-0.5 truncate">{notification.postContentPreview}</p>
        )}
        <p className="text-stone-500 text-xs mt-1">{formatRelativeTime(notification.createdAt)}</p>
      </div>

      {!notification.read && (
        <span className="w-2 h-2 rounded-full bg-brand-600 flex-shrink-0 mt-1.5" aria-hidden="true" />
      )}
    </button>
  )
}
