interface SpinnerProps {
  size?: 'sm' | 'md'
}

/** Shared loading spinner — replaces several inline divs that had drifted to inconsistent
 *  sizes/colors across pages. */
export default function Spinner({ size = 'md' }: SpinnerProps) {
  const sizeClasses = size === 'sm' ? 'w-5 h-5 border-2' : 'w-8 h-8 border-[3px]'
  return (
    <div
      role="status"
      aria-label="Loading"
      className={`${sizeClasses} border-brand-500 border-t-transparent rounded-full animate-spin`}
    />
  )
}
