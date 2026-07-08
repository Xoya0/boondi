import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CursorPage, Hashtag, Post, UserProfile } from '../types'
import { searchApi } from '../api/search'
import PostCard from '../components/posts/PostCard'
import UserListItem from '../components/shared/UserListItem'
import InfiniteScrollSentinel from '../components/shared/InfiniteScrollSentinel'
import Spinner from '../components/shared/Spinner'
import EmptyState from '../components/shared/EmptyState'
import ErrorState from '../components/shared/ErrorState'

type SearchTab = 'users' | 'posts' | 'hashtags'

const DEBOUNCE_MS = 300

export default function SearchPage() {
  const navigate = useNavigate()
  const [inputValue, setInputValue] = useState('')
  const [query, setQuery] = useState('')
  const [activeTab, setActiveTab] = useState<SearchTab>('users')

  const [usersPage, setUsersPage] = useState<CursorPage<UserProfile> | null>(null)
  const [postsPage, setPostsPage] = useState<CursorPage<Post> | null>(null)
  const [hashtagsPage, setHashtagsPage] = useState<CursorPage<Hashtag> | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Debounce: only commit the query 300ms after typing stops (E8-07)
  useEffect(() => {
    const timer = setTimeout(() => setQuery(inputValue.trim()), DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [inputValue])

  const runSearch = () => {
    if (!query) {
      setUsersPage(null)
      setPostsPage(null)
      setHashtagsPage(null)
      return
    }
    setLoading(true)
    setError(null)
    const run =
      activeTab === 'users' ? searchApi.searchUsers(query).then(setUsersPage)
      : activeTab === 'posts' ? searchApi.searchPosts(query).then(setPostsPage)
      : searchApi.searchHashtags(query).then(setHashtagsPage)
    run
      .catch(() => setError('Search failed. Please try again.'))
      .finally(() => setLoading(false))
  }

  // Fetch the active tab's first page whenever the committed query or tab changes
  useEffect(runSearch, [query, activeTab])

  const loadMore = async () => {
    if (loadingMore || !query) return
    setLoadingMore(true)
    try {
      if (activeTab === 'users' && usersPage?.hasMore) {
        const more = await searchApi.searchUsers(query, usersPage.nextCursor ?? undefined)
        setUsersPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
      } else if (activeTab === 'posts' && postsPage?.hasMore) {
        const more = await searchApi.searchPosts(query, postsPage.nextCursor ?? undefined)
        setPostsPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
      } else if (activeTab === 'hashtags' && hashtagsPage?.hasMore) {
        const more = await searchApi.searchHashtags(query, hashtagsPage.nextCursor ?? undefined)
        setHashtagsPage(prev => (prev ? { ...more, items: [...prev.items, ...more.items] } : more))
      }
    } finally {
      setLoadingMore(false)
    }
  }

  const handleHashtagClick = (tag: string) => {
    setInputValue(tag)
    setQuery(tag)
    setActiveTab('posts')
  }

  const currentPage = activeTab === 'users' ? usersPage : activeTab === 'posts' ? postsPage : hashtagsPage

  return (
    <div className="min-h-screen bg-white max-w-xl mx-auto border-x border-stone-100">
      {/* Back nav + search input */}
      <div className="flex items-center gap-3 px-4 py-3 border-b border-stone-100 sticky top-0 bg-white/90 backdrop-blur z-10">
        <button
          onClick={() => navigate(-1)}
          className="text-stone-600 hover:text-stone-900 cursor-pointer flex-shrink-0"
          aria-label="Go back"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div className="relative flex-1">
          <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-stone-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M17 10.5A6.5 6.5 0 114 10.5a6.5 6.5 0 0113 0z" />
          </svg>
          <input
            type="text"
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            placeholder="Search Boondi"
            aria-label="Search Boondi"
            autoFocus
            className="w-full pl-9 pr-3 py-2 bg-stone-100 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-stone-100" role="tablist" aria-label="Search results">
        {(['users', 'posts', 'hashtags'] as SearchTab[]).map(tab => (
          <button
            key={tab}
            role="tab"
            aria-selected={activeTab === tab}
            onClick={() => setActiveTab(tab)}
            className={`flex-1 py-3 text-sm font-medium transition cursor-pointer ${
              activeTab === tab
                ? 'text-stone-900 border-b-2 border-brand-600'
                : 'text-stone-500 hover:text-stone-600'
            }`}
          >
            {tab === 'users' ? 'Users' : tab === 'posts' ? 'Posts' : 'Hashtags'}
          </button>
        ))}
      </div>

      {/* Results */}
      {!query && <EmptyState icon="🔍" message="Search for people, posts, or hashtags." />}

      {query && loading && (
        <div className="flex justify-center py-12">
          <Spinner />
        </div>
      )}

      {query && !loading && error && <ErrorState message={error} onRetry={runSearch} />}

      {query && !loading && !error && currentPage && (
        <>
          {currentPage.items.length === 0 ? (
            <EmptyState icon="🤷" message={`No results for "${query}".`} />
          ) : activeTab === 'users' ? (
            (currentPage.items as UserProfile[]).map(u => <UserListItem key={u.id} user={u} />)
          ) : activeTab === 'posts' ? (
            (currentPage.items as Post[]).map(p => <PostCard key={p.id} post={p} />)
          ) : (
            (currentPage.items as Hashtag[]).map(h => (
              <button
                key={h.tag}
                onClick={() => handleHashtagClick(h.tag)}
                className="w-full flex items-center justify-between px-4 py-3 border-b border-stone-100 hover:bg-stone-50 text-left cursor-pointer"
              >
                <span className="font-semibold text-stone-900 text-sm">#{h.tag}</span>
                <span className="text-stone-500 text-xs">{h.postCount.toLocaleString()} posts</span>
              </button>
            ))
          )}

          <InfiniteScrollSentinel hasMore={currentPage.hasMore} loading={loadingMore} onLoadMore={loadMore} />
        </>
      )}
    </div>
  )
}
