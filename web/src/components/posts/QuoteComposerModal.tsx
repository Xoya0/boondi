import { useNavigate } from 'react-router-dom'
import type { Post } from '../../types'
import PostComposer from './PostComposer'

interface QuoteComposerModalProps {
  post: Post
  onClose: () => void
}

/** Modal wrapping PostComposer with quotedPost set — E6-13's "Quote" entry point. */
export default function QuoteComposerModal({ post, onClose }: QuoteComposerModalProps) {
  const navigate = useNavigate()

  const handlePosted = (quotePost: Post) => {
    onClose()
    navigate(`/post/${quotePost.id}`)
  }

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}
    >
      <div className="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 sticky top-0 bg-white">
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 cursor-pointer">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          <h2 className="font-semibold text-gray-900 text-sm">Quote post</h2>
          <div className="w-5" />
        </div>

        <PostComposer
          placeholder="Add a comment"
          quotedPost={{
            id: post.id,
            content: post.content,
            imageUrl: post.imageUrl,
            author: post.author,
            createdAt: post.createdAt,
          }}
          onPosted={handlePosted}
        />
      </div>
    </div>
  )
}
