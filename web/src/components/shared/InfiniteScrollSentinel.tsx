import { useInfiniteScroll } from '../../hooks/useInfiniteScroll'
import Spinner from './Spinner'

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
      {loading && <Spinner size="sm" />}
    </div>
  )
}
