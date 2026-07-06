import { useRef, useState } from 'react'
import type { Post } from '../../types'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'

const MAX_CHARS = 500

interface PostComposerProps {
  onPosted?: (post: Post) => void
  placeholder?: string
  // When set, the composed post is a reply to this post
  parentPostId?: string
}

export default function PostComposer({
  onPosted,
  placeholder = "What's on your mind?",
  parentPostId,
}: PostComposerProps) {
  const user = useAuthStore(s => s.user)
  const [content, setContent] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)

  const remaining = MAX_CHARS - content.length
  const isOver = remaining < 0
  const isEmpty = content.trim().length === 0

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setImageFile(file)
    setImagePreview(URL.createObjectURL(file))
  }

  const removeImage = () => {
    setImageFile(null)
    setImagePreview(null)
    if (fileRef.current) fileRef.current.value = ''
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (isEmpty || isOver || submitting) return
    setError(null)
    setSubmitting(true)

    try {
      let imageUrl: string | undefined

      if (imageFile) {
        setUploading(true)
        const result = await postsApi.uploadImage(imageFile)
        imageUrl = result.url
        setUploading(false)
      }

      const post = await postsApi.createPost({ content: content.trim(), imageUrl, parentPostId })
      setContent('')
      removeImage()
      onPosted?.(post)
    } catch {
      setError('Failed to post. Please try again.')
    } finally {
      setSubmitting(false)
      setUploading(false)
    }
  }

  const initials = (user?.displayName ?? user?.username ?? '?').charAt(0).toUpperCase()

  return (
    <form onSubmit={handleSubmit} className="border-b border-gray-100 px-4 py-3">
      <div className="flex gap-3">
        {/* Avatar */}
        <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
          {user?.profilePictureUrl ? (
            <img
              src={user.profilePictureUrl}
              alt=""
              className="w-10 h-10 rounded-full object-cover"
            />
          ) : (
            <span className="text-indigo-600 font-semibold text-sm">{initials}</span>
          )}
        </div>

        <div className="flex-1">
          {/* Textarea */}
          <textarea
            value={content}
            onChange={e => setContent(e.target.value)}
            placeholder={placeholder}
            rows={3}
            className="w-full resize-none text-gray-800 text-sm placeholder-gray-400 border-none outline-none bg-transparent leading-relaxed"
          />

          {/* Image preview */}
          {imagePreview && (
            <div className="relative inline-block mt-2">
              <img
                src={imagePreview}
                alt="Preview"
                className="max-h-48 rounded-xl object-cover border border-gray-200"
              />
              <button
                type="button"
                onClick={removeImage}
                className="absolute top-1 right-1 bg-black/60 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs cursor-pointer hover:bg-black/80"
              >
                ✕
              </button>
            </div>
          )}

          {/* Error */}
          {error && <p className="text-red-500 text-xs mt-1">{error}</p>}

          {/* Footer */}
          <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-100">
            <div className="flex items-center gap-2">
              {/* Image attach button */}
              <button
                type="button"
                onClick={() => fileRef.current?.click()}
                className="text-indigo-400 hover:text-indigo-600 transition-colors cursor-pointer"
                title="Attach image"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </button>
              <input
                ref={fileRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="hidden"
                onChange={handleImageSelect}
              />
            </div>

            <div className="flex items-center gap-3">
              {/* Character counter */}
              <span className={`text-xs tabular-nums ${
                isOver ? 'text-red-500 font-medium' : remaining <= 50 ? 'text-amber-500' : 'text-gray-400'
              }`}>
                {remaining}
              </span>

              {/* Submit button */}
              <button
                type="submit"
                disabled={isEmpty || isOver || submitting}
                className="bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-300 text-white font-medium px-4 py-1.5 rounded-full text-sm transition cursor-pointer disabled:cursor-not-allowed"
              >
                {uploading ? 'Uploading…' : submitting ? 'Posting…' : 'Post'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </form>
  )
}
