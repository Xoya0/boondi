/**
 * Loading skeletons (E10-07): shown while a post list's first page is in flight,
 * shaped like PostCard so the feed doesn't jump when content arrives.
 */
export default function PostCardSkeleton() {
  return (
    <div className="px-4 py-3 border-b border-stone-100 animate-pulse" aria-hidden="true">
      <div className="flex gap-3">
        <div className="w-10 h-10 rounded-full bg-stone-200 shrink-0" />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <div className="h-3 w-24 bg-stone-200 rounded" />
            <div className="h-3 w-16 bg-stone-100 rounded" />
          </div>
          <div className="space-y-2">
            <div className="h-3 w-full bg-stone-100 rounded" />
            <div className="h-3 w-4/5 bg-stone-100 rounded" />
          </div>
          <div className="flex gap-10 mt-3">
            <div className="h-3 w-8 bg-stone-100 rounded" />
            <div className="h-3 w-8 bg-stone-100 rounded" />
            <div className="h-3 w-8 bg-stone-100 rounded" />
          </div>
        </div>
      </div>
    </div>
  )
}

/** Convenience block of several skeleton cards for first-page loads. */
export function PostListSkeleton({ count = 4 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }, (_, i) => (
        <PostCardSkeleton key={i} />
      ))}
    </>
  )
}
