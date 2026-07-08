import type { QuotedPost } from '../../types'
import { formatRelativeTime } from '../../utils/time'

interface QuotedPostPreviewProps {
  quotedPost: QuotedPost
  onClick?: (e: React.MouseEvent) => void
}

/**
 * Embedded preview of a quoted post — used both when rendering an existing
 * quote-post (PostCard) and while composing a new one (QuoteComposerModal).
 */
export default function QuotedPostPreview({ quotedPost, onClick }: QuotedPostPreviewProps) {
  return (
    <div
      onClick={onClick}
      className={`border border-stone-200 rounded-xl px-3 py-2 ${
        onClick ? 'hover:bg-stone-100 transition-colors cursor-pointer' : ''
      }`}
    >
      <div className="flex items-center gap-1.5 text-xs">
        <span className="font-semibold text-stone-900">
          {quotedPost.author.displayName ?? quotedPost.author.username}
        </span>
        <span className="text-stone-400">@{quotedPost.author.username}</span>
        <span className="text-stone-300">·</span>
        <span className="text-stone-400">{formatRelativeTime(quotedPost.createdAt)}</span>
      </div>
      <p className="text-stone-700 text-sm mt-0.5 whitespace-pre-wrap break-words line-clamp-3">
        {quotedPost.content}
      </p>
      {quotedPost.imageUrl && (
        <img
          src={quotedPost.imageUrl}
          alt=""
          className="mt-1.5 rounded-xl max-h-40 object-cover border border-stone-100"
        />
      )}
    </div>
  )
}
