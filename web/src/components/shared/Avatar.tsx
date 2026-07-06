interface AvatarProps {
  src: string | null
  alt: string
  size?: 'sm' | 'md'
}

/** Shared avatar-or-initial fallback, factored out of PostCard for reuse in user lists. */
export default function Avatar({ src, alt, size = 'md' }: AvatarProps) {
  const sizeClasses = size === 'sm' ? 'w-10 h-10 text-sm' : 'w-12 h-12 text-base'

  if (src) {
    return (
      <img
        src={src}
        alt={alt}
        className={`${sizeClasses} rounded-full object-cover bg-gray-200 flex-shrink-0`}
      />
    )
  }
  return (
    <div className={`${sizeClasses} rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0`}>
      <span className="text-indigo-600 font-semibold">{alt.charAt(0).toUpperCase()}</span>
    </div>
  )
}
