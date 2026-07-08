import { useInfiniteScroll } from '../../hooks/useInfiniteScroll'

interface InfiniteScrollSentinelProps {
  hasMore: boolean
  loading: boolean
  onLoadMore: () => void
}

/** Invisible trigger + spinner at the bottom of a list, used for infinite scroll (E5-07). */
export default function InfiniteScrollSentinel({ hasMore, loading, onLoadMore }: InfiniteScrollSentinelProps) {
  const sentinelRef = useInfiniteScroll({ hasMore, loading, onLoadMore })

  if (!hasMore) return null

  return (
    <div ref={sentinelRef} className="flex justify-center py-4">
      {loading && (
        <div className="animate-spin w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full" />
      )}
    </div>
  )
}
