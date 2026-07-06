import { useCallback, useRef } from 'react'

interface UseInfiniteScrollOptions {
  hasMore: boolean
  loading: boolean
  onLoadMore: () => void
}

/**
 * Returns a ref callback for a sentinel element. When the sentinel scrolls into
 * view, onLoadMore fires — guarded by hasMore/loading so it never double-fires
 * or fires past the end of the list (E5-07).
 */
export function useInfiniteScroll({ hasMore, loading, onLoadMore }: UseInfiniteScrollOptions) {
  const observerRef = useRef<IntersectionObserver | null>(null)

  const sentinelRef = useCallback(
    (node: Element | null) => {
      if (observerRef.current) {
        observerRef.current.disconnect()
        observerRef.current = null
      }
      if (!node || !hasMore) return

      observerRef.current = new IntersectionObserver(entries => {
        if (entries[0]?.isIntersecting && hasMore && !loading) {
          onLoadMore()
        }
      })
      observerRef.current.observe(node)
    },
    [hasMore, loading, onLoadMore]
  )

  return sentinelRef
}
