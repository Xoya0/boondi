# Boondi — Project Progress & Session Memory

> **Purpose:** Read this file FIRST in every new session before doing any work.
> **Last updated:** 2026-07-06 | Sprint 6 complete — next is Sprint 7

---

## Project Identity

- **Name:** Boondi
- **Type:** Private social networking platform (Twitter/X inspired)
- **Stack:** Java 21 + Spring Boot 3.3.4 backend · React + TypeScript web · Kotlin + Jetpack Compose Android
- **Database:** PostgreSQL 16 + Redis 7 + MinIO (object storage)
- **Architecture:** Clean Architecture (domain / application / infrastructure / presentation)
- **Root directory:** `C:\Users\dibya\Documents\Boondi\`

---

## Repository Structure

```
C:\Users\dibya\Documents\Boondi\
├── doc/                          ← All planning documents
├── backend/src/main/java/com/boondi/
│   ├── domain/
│   │   ├── entity/   User, Post, EmailVerification, PasswordResetToken, Follow, PostLike, PostRepost, PostBookmark, Notification, Hashtag, PostHashtag
│   │   ├── enums/    UserRole, NotificationType
│   │   └── repository/ UserRepository, PostRepository, EmailVerificationRepository, PasswordResetTokenRepository, FollowRepository, PostLikeRepository, PostRepostRepository, PostBookmarkRepository, NotificationRepository, HashtagRepository, PostHashtagRepository
│   ├── application/
│   │   ├── dto/request/   Register, Login, Logout, Refresh, ForgotPassword, ResetPassword, UpdateProfile, CreatePost, UpdatePost
│   │   ├── dto/response/  AuthResponse, UserResponse, MessageResponse, UploadResponse, PostResponse, CursorPage, NotificationResponse, HashtagResponse
│   │   ├── mapper/        UserMapper, PostMapper, NotificationMapper
│   │   └── service/       AuthService, EmailVerificationService, PasswordResetService, TokenService, UserService, PostService, TimelineService, InteractionService, FollowService, PostViewerStateService, TimelineCacheService, NotificationService, SearchService
│   ├── infrastructure/
│   │   ├── security/  JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   │   ├── config/    SecurityConfig, RedisConfig, CorsConfig, SwaggerConfig, StorageConfig, MailConfig
│   │   ├── service/   EmailService, StorageService
│   │   └── exception/ ErrorCode, BoondiException, GlobalExceptionHandler, ApiResponse
│   └── presentation/controller/
│       AuthController, HealthController, UserController, PostController, TimelineController, NotificationController, SearchController, HashtagController
├── backend/src/main/resources/db/migration/
│   V1__create_users_table.sql
│   V2__create_auth_token_tables.sql
│   V3__create_posts_table.sql
│   V4__create_follows_table.sql
│   V5__create_interaction_tables.sql
│   V6__create_notifications_and_search.sql
├── web/src/
│   ├── api/         client.ts, auth.ts, users.ts, posts.ts, timelines.ts
│   ├── store/       authStore.ts (Zustand + persist)
│   ├── router/      index.tsx (ProtectedRoute, PublicOnlyRoute)
│   ├── types/       index.ts (UserInfo, UserProfile, Post, PostAuthor, CursorPage, ApiResponse)
│   ├── utils/       time.ts (formatRelativeTime, formatFullDate)
│   ├── components/
│   │   ├── auth/    AuthLayout.tsx
│   │   ├── posts/   PostCard.tsx, PostComposer.tsx
│   │   └── profile/ EditProfileModal.tsx
│   └── pages/
│       LoginPage, RegisterPage, ForgotPasswordPage, ResetPasswordPage, HomePage, ProfilePage, PostDetailPage
├── docker-compose.yml
├── nginx/nginx.conf
└── .gitignore
```

---

## Sprint Plan Summary

| Sprint | Dates | Focus | Status |
|--------|-------|-------|--------|
| Sprint 1 | Jul 7–18, 2026 | Backend foundation + auth register/login | ✅ COMPLETE |
| Sprint 2 | Jul 21–Aug 1, 2026 | Auth complete + User profiles + S3 upload | ✅ COMPLETE |
| Sprint 3 | Aug 4–15, 2026 | Post CRUD APIs + Web project init + Web auth screens | ✅ COMPLETE |
| Sprint 4 | Aug 18–29, 2026 | Timeline APIs + Web profile + Post UI | ✅ COMPLETE |
| Sprint 5 | Sep 1–12, 2026 | Social interactions APIs + Web feed complete | ✅ COMPLETE |
| Sprint 6 | Sep 15–26, 2026 | Notifications + Search + Web social UI | ✅ COMPLETE |
| Sprint 7 | Sep 29–Oct 10, 2026 | Web feature complete + Android init + Android auth | ⏳ Pending |
| Sprint 8 | Oct 13–24, 2026 | Android core (feed, posts, profiles) | ⏳ Pending |
| Sprint 9 | Oct 27–Nov 7, 2026 | Android social + notifications + Admin panel | ⏳ Pending |
| Sprint 10 | Nov 10–20, 2026 | Polish, tests, security, production deploy | ⏳ Pending |

---

## Sprint 1 — COMPLETE ✅
Auth APIs (register/login), Spring Security + JWT, Flyway V1, Docker Compose, Swagger, CI.

## Sprint 2 — COMPLETE ✅
Full auth (refresh/logout/verify-email/forgot-reset-password), user profile CRUD, MinIO avatar/banner upload. v0.1.0 alpha tag.

## Sprint 3 — COMPLETE ✅
Post CRUD backend (E4-01→E4-05), React+Vite+Tailwind web init, Login/Register/ForgotPassword/ResetPassword screens, Zustand auth state, protected routes.

**Post API:** POST /posts, GET /posts/{id} (public), PUT /posts/{id} (30-min edit window), DELETE /posts/{id} (soft delete), POST /posts/images (upload).
**Flyway V3:** posts table.
**ErrorCodes added:** POST_NOT_FOUND, POST_ACCESS_DENIED, POST_EDIT_WINDOW_EXPIRED.

## Sprint 4 — COMPLETE ✅

**Sprint Goal:** Timeline APIs (cursor-paginated) + Web profile page + Post composer + Post card.

| ID | Story | Status |
|----|-------|--------|
| E5-01 | Backend: Home timeline — GET /api/v1/timelines/home (auth, follows-based) | ✅ |
| E5-02 | Backend: Latest timeline — GET /api/v1/timelines/latest (public) | ✅ |
| E5-03 | Backend: User timeline — GET /api/v1/users/{username}/posts (public) | ✅ |
| E3-04 | Web: Profile page (/profile/:username) | ✅ |
| E3-05 | Web: Edit profile modal (fields + avatar/banner upload with preview) | ✅ |
| E4-06 | Web: Post composer (textarea, char counter, image attach, submit) | ✅ |
| E4-07 | Web: Post card (avatar, name, handle, time, content, image, action bar) | ✅ |

### Key Technical Details (Sprint 4)

**Cursor-based pagination design:**
- Request: `?cursor=<ISO-8601>&limit=20` (max 50)
- Response: `CursorPage<T>` → `{ items, nextCursor, hasMore, count }`
- `nextCursor` = `createdAt` (ISO-8601) of the LAST item in the page. Pass as `?cursor=` on next request.
- Implemented by fetching `limit + 1` rows; if `size > limit` → hasMore=true, trim last item.
- All three timelines use the same `buildPage()` helper in `TimelineService`.

**Timeline endpoints:**
- `GET /timelines/latest` — public, all posts reverse-chron
- `GET /timelines/home` — auth required, posts from followed users (empty until Sprint 5 adds follow data)
- `GET /users/{username}/posts` — public, user's own posts

**Follow table (V4 migration):**
- Created now so home timeline JOIN works. `follows(follower_id, followee_id, created_at)` — composite PK.
- Follow/Unfollow API endpoints come in Sprint 5 (E6-05).
- `Follow.java` entity uses `@IdClass(Follow.FollowId.class)`.
- `FollowRepository` has `existsByFollowerIdAndFolloweeId` and `deleteByFollowerIdAndFolloweeId` — ready for Sprint 5.

**PostRepository JPQL queries (all use JOIN FETCH p.author to avoid N+1):**
- `findLatestTimeline(cursor, pageable)`
- `findHomeTimeline(userId, cursor, pageable)` — subquery on Follow
- `findUserTimeline(username, cursor, pageable)`

**SecurityConfig public endpoints added:**
- `GET /timelines/latest`
- `GET /users/{username}` (was already accessible but now explicit)
- `GET /users/{username}/posts`

**Web — HomePage (`/home`):**
- Top nav with brand + username link to profile + sign out
- Tab switcher: Latest / Home
- PostComposer at top (creates post → prepends to feed immediately)
- Feed of PostCards with "Load more" button
- Delete handled inline (removes from local state)

**Web — ProfilePage (`/profile/:username`):**
- Sticky back-nav with post count
- Banner + avatar (gradient fallback if no banner)
- Edit profile button if own profile, Follow button otherwise
- Joined date, bio, following/followers stats
- Posts tab + paginated PostCard list
- On username change after edit → redirects to new profile URL

**Web — PostCard:**
- Shows: avatar (or initial fallback), displayName, @username, relative time, content, image
- Action bar: reply/repost/like/bookmark counts (display only — interactions in Sprint 5)
- Delete button shown only to post owner (confirms before deleting)
- Links to `/profile/:username`

**Web — PostComposer:**
- Textarea with live char counter (amber at ≤50, red when over 500)
- Image attach: uploads to `/posts/images` first → gets URL → includes in createPost body
- Image preview with remove button
- Disabled submit when empty, over limit, or submitting

**Web — EditProfileModal:**
- Inline banner + avatar image pickers with preview
- Fields: displayName, username (@prefix), bio
- Single Save button uploads images then updates profile fields
- If username changes → ProfilePage redirects to new URL

**New web files (Sprint 4):**
- `src/types/index.ts` — UserInfo, UserProfile, Post, PostAuthor, CursorPage, ApiResponse
- `src/utils/time.ts` — formatRelativeTime, formatFullDate
- `src/api/users.ts`, `posts.ts`, `timelines.ts`
- `src/components/posts/PostCard.tsx`, `PostComposer.tsx`
- `src/components/profile/EditProfileModal.tsx`
- `src/pages/ProfilePage.tsx`

## Sprint 5 — COMPLETE ✅

**Sprint Goal:** Trending timeline + Redis home cache + all social interaction APIs + web interaction wiring + post detail page.

| ID | Story | Status |
|----|-------|--------|
| E5-04 | Backend: Trending timeline — GET /timelines/trending (public, 24h window) | ✅ |
| E5-05 | Backend: Redis home timeline cache (first page, 5-min TTL) | ✅ |
| E6-01 | Backend: Like/Unlike — POST/DELETE /posts/{id}/like | ✅ |
| E6-02 | Backend: Repost — POST/DELETE /posts/{id}/repost | ✅ |
| E6-03 | Backend: Quote post — POST /posts with quotedPostId | ✅ |
| E6-04 | Backend: Bookmark — POST/DELETE /posts/{id}/bookmark | ✅ |
| E6-05 | Backend: Follow/Unfollow — POST/DELETE /users/{username}/follow | ✅ |
| E6-06 | Backend: Followers list — GET /users/{username}/followers | ✅ |
| E6-07 | Backend: Following list — GET /users/{username}/following | ✅ |
| E6-08 | Backend: Reply API — POST /posts with parentPostId + GET /posts/{id}/replies | ✅ |
| E4-08 | Web: Post detail page (/post/:id) with replies + reply composer | ✅ |
| — | Web: Wired like/repost/bookmark toggles in PostCard; Follow button on ProfilePage | ✅ |

### Key Technical Details (Sprint 5)

**Flyway V5:** `post_likes`, `post_reposts`, `post_bookmarks` — each `(user_id, post_id)` composite PK + index on post_id. Entities `PostLike`, `PostRepost`, `PostBookmark` use `@IdClass` like Follow.

**Interaction design (InteractionService):**
- Like/repost/bookmark insert/delete a row and inc/dec the counter on Post (no separate counts table).
- Duplicate action → 409 CONFLICT (ALREADY_LIKED / NOT_LIKED etc. — 9 new ErrorCodes incl. CANNOT_FOLLOW_SELF).
- All interaction endpoints return the updated PostResponse so clients can sync counts + flags.

**Per-viewer flags:** `PostResponse` now has `likedByViewer` / `repostedByViewer` / `bookmarkedByViewer`, filled by `PostViewerStateService` via 3 batch queries (`findLikedPostIds(userId, postIds)` etc.). `UserResponse` has nullable `followedByViewer` (set in getProfile when authenticated, and by follow/unfollow responses). Public endpoints accept an optional Bearer token — `@AuthenticationPrincipal` is null for anonymous, flags default false/null.

**Reply/Quote (regular posts with FKs):**
- `CreatePostRequest` gained `parentPostId` (reply) and `quotedPostId` (quote).
- Reply increments parent `reply_count`; quote increments quoted `repost_count` (quotes count toward repost total — documented decision).
- Deleting a reply/quote decrements those counters (wrapped in try/catch: soft-deleted parent's lazy proxy throws EntityNotFoundException under @SQLRestriction).
- `PostResponse` includes `parentPostId` + shallow `quotedPost` embed (no nested quote chains). Timeline queries use `LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author` to avoid N+1.
- Replies list: GET /posts/{id}/replies — chronological (oldest first), cursor is `createdAt >` (opposite direction from timelines).

**Trending (E5-04):**
- `GET /timelines/trending` — public. Score = likeCount×1 + repostCount×2, window = last 24h.
- Cursor is a NUMERIC OFFSET (score order isn't a stable time cursor), capped at 200. nextCursor = offset+limit as string.

**Redis home cache (E5-05) — TimelineCacheService:**
- Key `timeline:home:{userId}`, TTL 5 min, JSON via Spring's ObjectMapper. Only caches the default first page (cursor=null, limit=20).
- CursorPage + PostResponse (+ nested) gained @NoArgsConstructor/@AllArgsConstructor for Jackson round-trip.
- Invalidation: post create/delete → evict all followers (`findFollowerIds`); follow/unfollow → evict the follower's own key.
- All Redis errors are caught + logged — degrades to DB query.

**Follow (FollowService):** counters `follower_count`/`following_count` maintained on User. Followers/following lists paginated by `follows.created_at` cursor (JPQL returns `[User, createdAt]` rows).

**SecurityConfig new public GETs:** `/timelines/trending`, `/posts/{postId}/replies`, `/users/{username}/followers`, `/users/{username}/following`.

**Web:**
- `PostCard`: like/repost/bookmark buttons toggle via API (server response replaces local post state; on error refetches the post). Filled/colored icons when active. Content + reply button navigate to `/post/:id`. Renders quoted-post embed (clickable).
- `PostDetailPage` (`/post/:postId`): post + chronological replies + reply composer (PostComposer gained `parentPostId` prop); "view parent" link when the post is itself a reply; deleting the post navigates home.
- `ProfilePage`: Follow/Following button wired via `followedByViewer`.
- Types: Post += parentPostId/quotedPost/viewer flags; UserProfile += followedByViewer; new QuotedPost type.
- API additions: posts (like/unlike/repost/unrepost/bookmark/unbookmark/getReplies), users (follow/unfollow/getFollowers/getFollowing), timelines (getTrending — UI tab comes in a later sprint).

**Fixed pre-existing bug:** 4 files (TokenService, EmailVerificationService, PasswordResetService, JwtAuthenticationFilter) were missing `import java.util.HexFormat` — backend didn't compile before this sprint's work.

**Service signature changes (callers beware):**
- `PostService.getPost(postId, viewerId)` · `UserService.getProfile(username, viewerId)`
- `TimelineService.getLatestTimeline(viewerId, cursor, limit)` / `getUserTimeline(viewerId, username, cursor, limit)` — viewerId first.

## Sprint 6 — COMPLETE ✅

**Sprint Goal (per Sprint-and-Release-Plan.md §6):** Notifications system + search APIs complete; web like/bookmark interactions polished with optimistic UI.

| ID | Story | Status |
|----|-------|--------|
| E7-01 | Backend: Notification creation service (fan-out on like/repost/reply/follow) | ✅ |
| E7-02 | Backend: Get notifications — GET /notifications (cursor-paginated) | ✅ |
| E7-03 | Backend: Mark notification as read — PUT /notifications/{id}/read | ✅ |
| E7-04 | Backend: Mark all read — PUT /notifications/read-all | ✅ |
| E8-01 | Backend: Search users — GET /search/users?q= (ILIKE username/display name) | ✅ |
| E8-02 | Backend: Search posts — GET /search/posts?q= (PostgreSQL tsvector full-text) | ✅ |
| E8-03 | Backend: Search hashtags — GET /search/hashtags?q= (prefix match) | ✅ |
| E8-04 | Backend: Hashtag extraction on post create (#word → hashtags + post_hashtags) | ✅ |
| E8-05 | Backend: Trending hashtags — GET /hashtags/trending (24h window, Redis-cached) | ✅ |
| E6-09 | Web: Like button with optimistic update + heart-fill animation | ✅ |
| E6-10 | Web: Bookmark button (toggle, persists via API) | ✅ (done in Sprint 5, verified here) |

### Key Technical Details (Sprint 6)

**Flyway V6:** `notifications` (recipient_id, actor_id, type, post_id nullable, is_read), `hashtags` (unique `tag`), `post_hashtags` (composite PK like the Sprint 5 interaction tables). Also alters `posts` to add a generated `search_vector tsvector` column (`GENERATED ALWAYS AS (to_tsvector('english', content)) STORED`) + GIN index — required before E8-02 could be written, per the sprint plan's own risk note.

**Notifications (NotificationService) — fan-out on write, not on read:**
- 4 types: `LIKE`, `REPOST`, `REPLY`, `FOLLOW`. No `MENTION` type — the plan's epic goal mentions "mentions" but no `@user` parsing story exists anywhere in the backlog, so it's out of scope; flagged here in case a future sprint adds it.
- Quote-post creation triggers a `REPOST` notification (not a separate type) — consistent with Sprint 5's decision that quotes count toward the repost total on the post itself.
- Self-notifications are always skipped (liking/replying to your own post, or the already-blocked self-follow).
- Triggered from: `InteractionService.like()`/`repost()`, `PostService.createPost()` (reply → parent author, quote → quoted author), `FollowService.follow()`.
- `NotificationResponse` embeds a shallow actor (id/username/displayName/avatar) + optional `postId`/`postContentPreview` (first 80 chars, null for FOLLOW). Soft-deleted related posts degrade to null preview rather than throwing (same `EntityNotFoundException` pattern as Sprint 5's quote/parent handling).
- No dedicated unread-count endpoint — out of scope for Sprint 6 (that's Web story E7-07 in Sprint 7); clients can page through `GET /notifications` and use the `isRead` flag per item.

**Search (SearchService) — three sources, one pagination style:**
- All three (`users`, `posts`, `hashtags`) paginate with a **numeric offset cursor**, same pattern as Sprint 5's trending timeline — relevance/prefix order isn't a stable time-based cursor. Capped at offset 500.
- Blank/whitespace `q` short-circuits to an empty page before hitting the DB.
- **Users:** native ILIKE query on `username` OR `display_name`, ordered alphabetically.
- **Posts:** native query against `search_vector @@ plainto_tsquery('english', :query)`, ordered by `ts_rank` then recency. Uses an explicit column list (not `SELECT *`) so Hibernate's entity-result mapping doesn't choke on the unmapped `search_vector` column. Known trade-off: author is lazy-loaded per result (N+1) rather than fetch-joined — not expressible cleanly in a native query without a `SqlResultSetMapping`; acceptable at page sizes ≤50.
- **Hashtags:** prefix match (`ILIKE 'query%'`), leading `#` stripped and lowercased before matching.

**Hashtag extraction (E8-04) — in `PostService.createPost`:**
- Regex `#(\w+)` over post content, tags deduped (case-insensitive, stored lowercase) via a `LinkedHashSet`, find-or-create each `Hashtag`, then insert a `PostHashtag` link row. No extraction on edit (out of scope — plan only specifies "on post create").

**Trending hashtags (E8-05):**
- `GET /hashtags/trending` — top 10 by usage count in the last 24h, computed via a JPQL query joining `post_hashtags`→`hashtags` with a subquery restricting to non-deleted posts (Post's `@SQLRestriction` applies automatically inside the subquery).
- Redis-cached at key `hashtags:trending`, TTL 10 minutes, same JSON-via-ObjectMapper + graceful-degrade-on-Redis-failure pattern as Sprint 5's `TimelineCacheService`.

**Web — PostCard like button (E6-09):**
- Now optimistic: flips `likedByViewer`/`likeCount` locally before the API call resolves, reverts on failure (previous Sprint 5 behavior waited for the server response first).
- Heart icon gets a `scale-125 → scale-100` transition (300ms) on like — pure Tailwind transition classes, no new dependency.
- Bookmark/repost intentionally left non-optimistic (only Like was called out for animation + optimistic update in the sprint plan).

**New ErrorCodes:** `NOTIFICATION_NOT_FOUND`, `NOTIFICATION_ACCESS_DENIED` (marking someone else's notification as read).

**SecurityConfig new public GETs:** `/search/users`, `/search/posts`, `/search/hashtags`, `/hashtags/trending`. `/notifications/**` has no public routes — auth required for all (falls through to `anyRequest().authenticated()`).

---

## Backend API Summary (Sprints 1–6)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /auth/register | Public | Register |
| POST | /auth/login | Public | Login |
| POST | /auth/refresh | Public | Refresh tokens |
| POST | /auth/logout | Required | Logout |
| GET | /auth/verify-email?token= | Public | Verify email |
| POST | /auth/forgot-password | Public | Send reset email |
| POST | /auth/reset-password | Public | Reset password |
| GET | /users/{username} | Public | Get profile |
| GET | /users/{username}/posts | Public | User's posts (cursor) |
| PUT | /users/me | Required | Update profile |
| POST | /users/me/avatar | Required | Upload avatar |
| POST | /users/me/banner | Required | Upload banner |
| POST | /posts | Required | Create post |
| GET | /posts/{id} | Public | Get post |
| PUT | /posts/{id} | Required | Edit post (30-min window) |
| DELETE | /posts/{id} | Required | Soft delete post |
| POST | /posts/images | Required | Upload post image |
| GET | /posts/{id}/replies | Public | Replies, oldest first (cursor) |
| POST | /posts/{id}/like | Required | Like post |
| DELETE | /posts/{id}/like | Required | Unlike post |
| POST | /posts/{id}/repost | Required | Repost |
| DELETE | /posts/{id}/repost | Required | Undo repost |
| POST | /posts/{id}/bookmark | Required | Bookmark |
| DELETE | /posts/{id}/bookmark | Required | Remove bookmark |
| POST | /users/{username}/follow | Required | Follow user |
| DELETE | /users/{username}/follow | Required | Unfollow user |
| GET | /users/{username}/followers | Public | Followers list (cursor) |
| GET | /users/{username}/following | Public | Following list (cursor) |
| GET | /timelines/latest | Public | Latest timeline (cursor) |
| GET | /timelines/trending | Public | Trending 24h (offset cursor) |
| GET | /timelines/home | Required | Home timeline (cursor, Redis-cached first page) |
| GET | /notifications | Required | Get notifications (cursor) |
| PUT | /notifications/{id}/read | Required | Mark notification read |
| PUT | /notifications/read-all | Required | Mark all notifications read |
| GET | /search/users?q= | Public | Search users (offset cursor) |
| GET | /search/posts?q= | Public | Full-text search posts (offset cursor) |
| GET | /search/hashtags?q= | Public | Search hashtags by prefix (offset cursor) |
| GET | /hashtags/trending | Public | Top 10 hashtags, last 24h (Redis-cached) |
| GET | /health | Public | Health check |

---

## Backend API Design Conventions

- **Base path:** `/api/v1`
- **All responses:** `ApiResponse<T>` — `{ success, data, message, errorCode, errors, path, timestamp }`
- **Auth:** `Authorization: Bearer {accessToken}`
- **Pagination:** cursor-based — `CursorPage<T>` — `{ items, nextCursor, hasMore, count }`
- **UUIDs:** everywhere as primary keys
- **Soft deletes:** `deleted_at` + `@SQLRestriction("deleted_at IS NULL")`

## Key Config Values (Dev)
```
DB:     jdbc:postgresql://localhost:5432/boondi (user: boondi / pass: boondi)
Redis:  localhost:6379 (no password)
MinIO:  http://localhost:9000 (minioadmin / minioadmin), console: localhost:9001
Mail:   localhost:1025 (MailHog), UI: localhost:8025
JWT:    access=15min, refresh=7days
Web:    http://localhost:5173 (Vite dev server)
```

---

## Epics Status Overview

| Epic | Name | Total Pts | Done | Remaining |
|------|------|-----------|------|-----------|
| Epic 1 | Foundation & DevOps | 23 | 18 | 5 (Android init Sprint 7) |
| Epic 2 | Authentication | 39 | 20 | 19 (Android Sprints 7-8) |
| Epic 3 | User Profiles | 20 | 17 | 3 (Android Sprint 8) |
| Epic 4 | Posts | 29 | 22 | 7 (Android Sprint 8) |
| Epic 5 | Timeline & Feed | 31 | 18 | 13 (Web trending tab Sprint 7, Android Sprint 8) |
| Epic 6 | Social Interactions | 41 | 28 | 13 (Web reply composer/quote UI/bookmarks page/followers-following pages Sprint 7, Android Sprint 9) |
| Epic 7 | Notifications | 20 | 9 | 11 (Web notifications UI Sprint 7, Android Sprint 9) |
| Epic 8 | Search | 23 | 15 | 8 (Web search UI Sprint 7, Android Sprint 9) |
| Epic 9 | Admin | 16 | 0 | 16 |
| Epic 10 | Polish/Testing/Launch | 50 | 0 | 50 |

---

## Sprint 7 Preview (next session)

**Focus (per Sprint-and-Release-Plan.md §6, Sprint 7):** Web app feature-complete for MVP + Android project init + Android auth screens.

**Committed stories per the plan:** E6-11 (Follow/Unfollow button — *already done in Sprint 5*, verify only), E6-12 (Reply composer — mostly done via PostDetailPage's `PostComposer parentPostId`, verify against story wording), E6-13 (Repost/Quote UI — dropdown for Repost vs Quote; quote composer entry point still missing), E6-14 (Bookmarks page `/bookmarks`), E6-15 (Followers/Following pages `/profile/:username/followers|following` — backend + API client already exist from Sprint 5), E7-05/06/07 (Notifications page + item component + unread badge), E8-06/07 (Search page with tabs + debounced input), E5-06/07 (Feed tabs Home/Latest/Trending + infinite scroll), E1-07 (Initialize Android project).

**Useful groundwork already in place from Sprints 5–6:**
- `usersApi.getFollowers/getFollowing` + backend list endpoints exist — web list pages just need UI.
- `timelinesApi.getTrending` exists — a Trending tab on HomePage is mostly wiring.
- Quote backend done (`quotedPostId` on createPost + embed rendering in PostCard) — only the "Repost vs Quote" dropdown + quote composer entry point is missing in the UI.
- `postsApi` has bookmark/unbookmark — a `/bookmarks` page needs a new backend list endpoint (`GET /users/me/bookmarks` or similar) since none exists yet — check the plan before assuming E6-14 is UI-only.
- Notification and search REST APIs are complete and cursor-paginated — E7-05/06/07 and E8-06/07 are pure UI work against existing endpoints.

---

## Notes for Claude in Future Sessions

1. **Always read this file first** before any code work.
2. **Check actual files** before assuming what exists — verify before writing.
3. **Sprint by sprint** — user works one sprint at a time.
4. **No scope creep** — only implement what's in the sprint plan.
5. **Update this file** at end of each sprint.
6. **Tailwind v4 note:** No `tailwind.config.js`. Uses `@tailwindcss/vite` plugin + `@import "tailwindcss"` in CSS.
7. **Web patterns:** All API calls use `apiClient` (axios) from `src/api/client.ts`. Responses unwrapped via `response.data.data`. Types from `src/types/index.ts`.
8. **Backend patterns:** Controllers inject services, return `ResponseEntity<ApiResponse<T>>`. Services throw `BoondiException` factory methods. `@SQLRestriction` on soft-deletable entities.

---

*Last updated: 2026-07-06 | Sprint 6 complete*
