# Boondi — Project Progress & Session Memory

> **Purpose:** Read this file FIRST in every new session before doing any work.
> **Last updated:** 2026-07-08 | Sprint 10 complete — all 10 sprints of the plan done

---

## ⚠️ Critical post-Sprint-9 fixes (found during first-ever live run)

The backend had **never actually been booted end-to-end** before 2026-07-07 (every prior sprint validated via `mvn compile` / code review only). The first real run against Docker + the Android app surfaced three real, pre-existing bugs — all now fixed:

1. **`application.yml` YAML indentation bug**: `spring.mail.*` and `spring.task.*` were mis-indented as children of `springdoc:` instead of `spring:`, so `spring.mail.host` was never actually set → Spring never created a `JavaMailSender` bean → **the app crash-looped on every startup**, so nothing was ever listening on port 8080. Fixed by re-indenting both blocks under `spring:`.
2. **`docker-compose.yml`'s `backend` service was missing `MAIL_HOST`/`STORAGE_ENDPOINT` env overrides.** Both defaulted to `localhost`, which inside the backend's own container means itself, not the `mailhog`/`minio` sibling containers — so even after fixing #1, verification emails and avatar/post-image uploads would silently fail. Fixed by pointing both at the Docker service names (`MAIL_HOST=mailhog`, `STORAGE_ENDPOINT=http://minio:9000`); `STORAGE_PUBLIC_URL` deliberately stays `localhost:9000` since that URL goes to browser/app clients on the host, not the backend itself.
3. **Systemic Postgres/pgjdbc bug in every cursor-paginated query**: the `(:cursor IS NULL OR x.field < :cursor)` JPQL pattern — used identically across **10 query methods in 5 repositories** (`PostRepository` ×5: latest/home/user timelines, replies, bookmarks; `FollowRepository` ×2: followers/following; `NotificationRepository`; `ReportRepository`; `UserRepository.findAllForAdmin`) — fails with `ERROR: could not determine data type of parameter $N` on Postgres whenever `cursor` is null (i.e. **every first-page load**, which is every feed/list screen on first open). This is why the Android app's Home feed showed a 500 and newly-created posts appeared to "vanish" — they were saved fine; the feed query just couldn't run. **Fixed by casting explicitly: `(cast(:cursor as timestamp) IS NULL OR x.field < :cursor)`** in all 10 places. Verified empirically against the live container (home timeline, user timeline, notifications, bookmarks, followers, following, admin users list all return 200 now). **If you add a new cursor-paginated query, use this cast pattern from the start — the bare `:cursor IS NULL` form will pass compilation and code review but fail at runtime on Postgres.**
4. **`SecurityConfig` never configured a custom `AuthenticationEntryPoint`**, so Spring Security's fallback for a stateless config with no `httpBasic()`/`formLogin()` is `Http403ForbiddenEntryPoint` — every missing/expired/invalid JWT got a **403**, not 401. Since the Android `TokenAuthenticator` (Sprint 8's E2-14 refresh flow) — and any correct OkHttp/Retrofit client — only ever triggers a refresh attempt on a **401**, this meant **the token-refresh feature could never actually fire**; an expired access token (15-min TTL) just failed forever with a generic "you don't have permission" error instead of silently refreshing. Fixed by adding an `AuthenticationEntryPoint` bean (in `SecurityConfig`) that returns a proper 401 + `ApiResponse` JSON body for unauthenticated access; the existing `@PreAuthorize`/`AccessDeniedException` → 403 path (admin RBAC) is untouched and still correctly returns 403. Verified empirically: no-token → 401, garbage-token → 401, valid-token-wrong-role → 403, valid-token-correct-role → 200.
5. **Image URLs are unreachable from the Android emulator** — `app.storage.public-url` defaults to `http://localhost:9000` (correct for the web app's browser, which runs on the host), but inside the emulator "localhost" means the emulator itself, not the host machine (same class of issue `10.0.2.2` solves for the API, but this is a *different* mechanism since the URL is embedded in API response bodies, not the API host itself). Workaround (not a code fix): `adb reverse tcp:9000 tcp:9000` forwards the emulator's own port 9000 to the host's MinIO. **This does not persist across emulator restarts — re-run it each session**, or wire it into a Gradle task if it becomes a recurring annoyance. As of this session, images still weren't confirmed rendering after this workaround (possibly needs a full app restart / Coil cache clear) — unresolved, deprioritized by the user.
6. **`@CreationTimestamp`/`@UpdateTimestamp` fields came back `null` in create-response bodies.** Hibernate populates these fields at *flush* time, not at `repository.save()` time — every "create X, then immediately map X to a response" flow (`PostService.createPost`, `PostService.updatePost`, `AuthService.register`, `ReportService.createReport`) was calling `.save()` and mapping the still-unflushed entity, so `createdAt`/`updatedAt` were `null` in the response the client got back (a follow-up GET would show the correct value, since by then the transaction had committed). Concretely: creating a reply showed `createdAt: null` in the immediate response, even though `GET /posts/{id}/replies` right after showed it correctly. **Fixed by switching those four call sites to `saveAndFlush(...)`.** Verified empirically: register, create post, and create report all now return real timestamps immediately. **If a new service creates an entity with `@CreationTimestamp`/`@UpdateTimestamp` and returns it in the same response, use `saveAndFlush`, not `save`.**

All six fixes are in already; `docker compose up -d --build backend` picks them up (fix 5 is a per-session `adb` command, not a code change). No Android/web code changes were needed for fixes 1, 2, 3, 4, 6 — all backend.

**Also verified working end-to-end this session** (via direct API calls against the live backend): like/unlike, repost/unrepost, bookmark/unbookmark (+ bookmarks list), reply creation (+ parent reply-count + replies list), search (users/posts/hashtags), and the full admin flow (file a report → admin views it → admin suspends/unsuspends a user). All correct.

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
│   │   ├── entity/   User, Post, EmailVerification, PasswordResetToken, Follow, PostLike, PostRepost, PostBookmark, Notification, Hashtag, PostHashtag, Report
│   │   ├── enums/    UserRole, NotificationType
│   │   └── repository/ UserRepository, PostRepository, EmailVerificationRepository, PasswordResetTokenRepository, FollowRepository, PostLikeRepository, PostRepostRepository, PostBookmarkRepository, NotificationRepository, HashtagRepository, PostHashtagRepository, ReportRepository
│   ├── application/
│   │   ├── dto/request/   Register, Login, Logout, Refresh, ForgotPassword, ResetPassword, UpdateProfile, CreatePost, UpdatePost, CreateReport
│   │   ├── dto/response/  AuthResponse, UserResponse, MessageResponse, UploadResponse, PostResponse, CursorPage, NotificationResponse, HashtagResponse, ReportResponse
│   │   ├── mapper/        UserMapper, PostMapper, NotificationMapper, ReportMapper
│   │   └── service/       AuthService, EmailVerificationService, PasswordResetService, TokenService, UserService, PostService, TimelineService, InteractionService, FollowService, PostViewerStateService, TimelineCacheService, NotificationService, SearchService, AdminService, ReportService
│   ├── infrastructure/
│   │   ├── security/  JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   │   ├── config/    SecurityConfig, RedisConfig, CorsConfig, SwaggerConfig, StorageConfig, MailConfig
│   │   ├── service/   EmailService, StorageService
│   │   └── exception/ ErrorCode, BoondiException, GlobalExceptionHandler (now handles AccessDeniedException too), ApiResponse
│   └── presentation/controller/
│       AuthController, HealthController, UserController, PostController, TimelineController, NotificationController, SearchController, HashtagController, AdminController, ReportController
├── backend/src/main/resources/db/migration/
│   V1__create_users_table.sql
│   V2__create_auth_token_tables.sql
│   V3__create_posts_table.sql
│   V4__create_follows_table.sql
│   V5__create_interaction_tables.sql
│   V6__create_notifications_and_search.sql
│   V7__create_reports_table.sql
├── web/src/
│   ├── api/         client.ts, auth.ts, users.ts, posts.ts, timelines.ts, notifications.ts, search.ts, admin.ts
│   ├── store/       authStore.ts (Zustand + persist)
│   ├── router/      index.tsx (ProtectedRoute, PublicOnlyRoute, AdminRoute)
│   ├── hooks/       useInfiniteScroll.ts
│   ├── types/       index.ts (UserInfo, UserProfile, Post, PostAuthor, QuotedPost, Notification, Hashtag, Report, CursorPage, ApiResponse)
│   ├── utils/       time.ts (formatRelativeTime, formatFullDate)
│   ├── components/
│   │   ├── auth/          AuthLayout.tsx
│   │   ├── posts/         PostCard.tsx, PostComposer.tsx, QuotedPostPreview.tsx, QuoteComposerModal.tsx
│   │   ├── profile/       EditProfileModal.tsx
│   │   ├── notifications/ NotificationItem.tsx
│   │   └── shared/        Avatar.tsx, UserListItem.tsx, InfiniteScrollSentinel.tsx
│   └── pages/
│       LoginPage, RegisterPage, ForgotPasswordPage, ResetPasswordPage, HomePage, ProfilePage,
│       PostDetailPage, BookmarksPage, FollowListPage, NotificationsPage, SearchPage, AdminPage
├── android/                      ← Kotlin + Compose + Hilt (Sprints 8–9 built; compiles, APK dexing pending host RAM)
│   ├── app/src/main/java/com/boondi/android/
│   │   ├── BoondiApplication.kt (@HiltAndroidApp), MainActivity.kt (@AndroidEntryPoint → BoondiApp), MainViewModel.kt
│   │   ├── navigation/BoondiNavHost.kt (BoondiApp + Routes; Routes.HOME renders ui/shell/HomeShell)
│   │   ├── data/remote/{dto,api,interceptor}/ · data/local/{TokenStorage,SessionManager} · data/repository/ · data/ApiResult.kt (safeApiCall + safeApiCallUnit)
│   │   ├── di/NetworkModule.kt (@PlainClient / @AuthClient)
│   │   ├── domain/model/Models.kt + PostInteractions.kt (User, Post, Author, QuotedPost, CursorPage, AuthSession, Notification, Hashtag; optimistic-toggle helpers)
│   │   └── ui/{common,auth,feed,post,profile,bookmarks,notifications,search,shell}/ · ui/theme/{Color,Type,Theme}.kt
│   ├── app/src/main/res/xml/network_security_config.xml (cleartext for 10.0.2.2 only)
│   ├── gradlew(.bat) + gradle/wrapper/ (8.14.3) · local.properties (sdk.dir, gitignored)
│   └── gradle/libs.versions.toml (AGP 8.12.3, Kotlin 2.0.21, compileSdk 36, Compose BOM 2024.09.03, Hilt 2.52, Retrofit/Moshi/OkHttp/Coil)
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
| Sprint 7 | Sep 29–Oct 10, 2026 | Web feature complete + Android init | ✅ COMPLETE |
| Sprint 8 | Oct 13–24, 2026 | Android core (feed, posts, profiles) | ✅ COMPLETE |
| Sprint 9 | Oct 27–Nov 7, 2026 | Android social + notifications + Admin panel | ✅ COMPLETE |
| Sprint 10 | Nov 10–20, 2026 | Polish, tests, security, production deploy | ✅ COMPLETE |

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

## Sprint 7 — COMPLETE ✅

**Sprint Goal (per Sprint-and-Release-Plan.md §6, Sprint 7):** Web app feature-complete for MVP + Android project initialized. Note: the plan's one-line phase summary in §6's intro table also says "+ Android auth," but Sprint 7's own detailed Committed Stories table only lists `E1-07` for Android — `E2-12/13/14` (Android login/registration/token storage) are in Sprint 8's table. Treated the detailed backlog as authoritative; Android auth work was not started this sprint.

| ID | Story | Status |
|----|-------|--------|
| E6-11 | Web: Follow/Unfollow button on profile page | ✅ (done Sprint 5, unchanged) |
| E6-12 | Web: Reply composer (threaded reply below post on detail page) | ✅ (done Sprint 5, unchanged) |
| E6-13 | Web: Repost/Quote UI — dropdown (Repost or Quote), quote opens composer with embedded post | ✅ |
| E6-14 | Web: Bookmarks page (`/bookmarks`) — paginated list of bookmarked posts | ✅ |
| E6-15 | Web: Followers/Following page (`/profile/:username/followers`, `/following`) | ✅ |
| E7-05 | Web: Notifications page (`/notifications`) | ✅ |
| E7-06 | Web: Notification item component | ✅ |
| E7-07 | Web: Unread count badge on bell icon in nav (polled 30s / on focus) | ✅ |
| E8-06 | Web: Search page (`/search`) with tabs: Users / Posts / Hashtags | ✅ |
| E8-07 | Web: Search input with 300ms debounce | ✅ |
| E5-06 | Web: Feed tabs (Home / Latest / Trending) | ✅ |
| E5-07 | Web: Infinite scroll (IntersectionObserver) on all feed/list pages | ✅ |
| E1-07 | Initialize Android project (Compose + Hilt + Navigation Compose) | ✅ (scaffolded; **build unverified** — see note below) |

### Key Technical Details (Sprint 7)

**Backend groundwork added to unblock two web stories (flagged as gaps in the Sprint 6 review, not new scope — required infrastructure for E6-14/E7-07):**
- `GET /users/me/bookmarks` — cursor-paginated bookmarked posts, sorted by *bookmark* time (not post creation time). New `PostRepository.findBookmarkedPosts` returns `[Post, bookmark.createdAt]` tuples (same tuple-row pattern as `FollowRepository.findFollowers/findFollowing`); `TimelineService.getBookmarkedTimeline` builds the page.
- `GET /notifications/unread-count` — new `NotificationRepository.countByRecipientIdAndReadFalse` (derived query; `n.read` is the entity property per the existing `markAllAsRead` JPQL) + `NotificationService.getUnreadCount` + `UnreadCountResponse` DTO.

**E6-13 — Repost/Quote dropdown:**
- `PostCard`'s repost button now opens a small popover (`Repost` / `Quote`) instead of directly toggling — clicking when already reposted still directly undoes it (no menu, since there's only one action available). Closes on outside click via a `mousedown` listener.
- `Quote` opens `QuoteComposerModal`, which wraps the existing `PostComposer` (now accepts an optional `quotedPost` prop) with the quoted-post preview and submits `quotedPostId`. On success, navigates to the new quote-post's detail page.
- Extracted `QuotedPostPreview` out of `PostCard` so the same embed markup renders both an existing quote (read-only, clickable) and the in-progress quote in the composer (read-only, non-clickable).
- Extracted `Avatar` into `components/shared/` (was a local function duplicated conceptually across pages) — `PostCard` and the new `UserListItem` both use it now.

**E6-14/E6-15 — new list pages, one shared pattern:**
- `BookmarksPage`, `FollowListPage` (shared for both `/followers` and `/following`, switching on `location.pathname`), `NotificationsPage`, and `SearchPage`'s per-tab results all follow the same shape: fetch first page in a `useEffect`, `loadMore()` appends via cursor, render via `InfiniteScrollSentinel`.
- New shared `UserListItem` (avatar, name, handle, bio snippet, inline follow toggle) — used by `FollowListPage` and `SearchPage`'s Users tab.
- `ProfilePage`'s Following/Followers `StatPill`s are now links to the new list pages.

**E7-05/06/07 — Notifications:**
- `NotificationItem` maps `NotificationType` → action text (`liked your post` / `reposted your post` / `replied to your post` / `followed you`), shows an unread dot + tinted background, marks itself read on click (optimistic local update, best-effort API call), then navigates to the post (or the actor's profile for `FOLLOW`, which has no `postId`).
- Unread badge lives on `HomePage`'s top nav (the only persistent nav bar in this codebase — there's no shared app-shell/header component across pages). Polls every 30s and refetches on `visibilitychange`/`window focus`, per the story. **Known limitation:** the badge only appears on `/home`, not on other authenticated pages, since introducing a global layout was judged out of scope for this story (would touch every page). Worth a real app-shell if this becomes a real user complaint.

**E8-06/07 — Search:**
- Single `SearchPage` with local `inputValue` (immediate) and debounced `query` (300ms via `setTimeout`, no new dependency) that actually triggers fetches. Three independent `CursorPage` states (users/posts/hashtags), only the active tab's page is fetched/displayed; switching tabs re-fetches rather than caching all three (simpler, acceptable request volume for MVP).
- Clicking a hashtag result sets the query to that tag and switches to the Posts tab — no separate hashtag-detail route exists (none was in the backlog).

**E5-06/07 — Trending tab + infinite scroll:**
- `HomePage` gained a third tab wired to `timelinesApi.getTrending` (already existed unused since Sprint 5/6).
- New `useInfiniteScroll` hook (IntersectionObserver on a sentinel ref, guarded by `hasMore`/`loading`) + `InfiniteScrollSentinel` wrapper component replace every manual "Load more" button across `HomePage`, `ProfilePage`, `PostDetailPage` (replies), and all four new list pages — one consistent pagination UX everywhere, per the story's intent that this is the app-wide pagination pattern, not a single-page feature.

**Android (E1-07) — scaffolded, not verified:**
- `android/` created fresh: Gradle Kotlin DSL + version catalog (`libs.versions.toml`), AGP 8.5.2, Kotlin 2.0.21 with the Kotlin 2.0 Compose-compiler Gradle plugin (no `composeOptions.kotlinCompilerExtensionVersion` needed), Hilt 2.52 via KSP, Navigation Compose 2.8.2.
- `BoondiApplication` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`, edge-to-edge, sets Compose content), `BoondiNavHost` with a single placeholder `SplashScreen` route, and a `BoondiTheme` (Material3, indigo primary matching the web app's Tailwind indigo-600 brand, dynamic color opt-out by default).
- `minSdk 26` chosen so the adaptive launcher icon (`mipmap-anydpi-v26`) needs no legacy PNG fallback.
- **No Retrofit/OkHttp dependency added** — E1-07's story text only names Compose + Hilt + Navigation Compose; Retrofit is added when Sprint 8's `E2-14` (token storage + refresh interceptor) actually needs it, to avoid an unused dependency.
- **⚠️ Build is unverified.** This dev environment has no Android SDK and no network access to fetch a Gradle wrapper JAR, so `gradlew`/`gradlew.bat`/`gradle-wrapper.jar` were **not** created (a wrapper script pointing at a missing jar is worse than no wrapper). Opening `android/` in Android Studio will prompt to generate the wrapper automatically. Exact dependency versions (AGP/Compose BOM/Hilt/KSP patch numbers) are plausible-current but unverified against Maven Central — expect Android Studio to suggest updates on first sync. **Next session: open in Android Studio and fix whatever the first sync surfaces before writing any more Android code.**

---

## Sprint 8 — COMPLETE ✅

**Sprint Goal (per Sprint-and-Release-Plan.md §6, Sprint 8):** Android app shows a real feed, users can create posts, profile screens are functional — demo-able end-to-end (register → login → feed → post detail → profile; compose a post). No new backend features (Android consumes the stable APIs).

| ID | Story | Status |
|----|-------|--------|
| E2-12 | Android: Login screen (Compose + ViewModel + state hoisting) | ✅ |
| E2-13 | Android: Registration screen (Compose) | ✅ |
| E2-14 | Android: Token storage (EncryptedSharedPreferences) + OkHttp refresh interceptor (auto-refresh on 401) | ✅ |
| E3-06 | Android: Profile screen (header, stats row, posts list) | ✅ |
| E3-07 | Android: Edit profile screen (form fields + image picker for avatar/banner) | ✅ |
| E4-09 | Android: Post composer screen (text, char counter, image picker, post) | ✅ |
| E4-10 | Android: Post card composable (mirrors web card layout) | ✅ |
| E4-11 | Android: Post detail screen (post + reply thread, delete own post) | ✅ |
| E5-08 | Android: Home feed screen (LazyColumn + pull-to-refresh) | ✅ |
| E5-09 | Android: Tab switcher (Home / Latest / Trending) | ✅ |

### Build toolchain (resolved the Sprint 7 "build unverified" blocker)

This environment now has an Android SDK, so the scaffold was made buildable:
- **JDK 21 installed** (Temurin, via `winget`) — the pre-existing **JDK 25 is too new**: Gradle's embedded Kotlin (2.0.21) can't parse the `"25.0.3"` version string, so every `.gradle.kts` compile fails under it. **Always run Gradle with `JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"`.**
- **Gradle wrapper generated** (8.14.3, `--distribution-type all`). Note `.gitignore`'s `*.jar` currently excludes `gradle/wrapper/gradle-wrapper.jar` — add a `!android/gradle/wrapper/gradle-wrapper.jar` exception before committing the wrapper.
- **Modernized to match the installed SDK (all API 36):** AGP `8.5.2 → 8.12.3`, `compileSdk`/`targetSdk` `34 → 36`, pinned `buildToolsVersion = "36.1.0"`. `minSdk 26` unchanged.
- **`gradle.properties` memory-tuned** (`-Xmx1536m`, capped metaspace/workers, `kotlin.daemon.jvmargs`) because the host runs IDEs alongside the build.
- **`local.properties`** written with `sdk.dir` (gitignored).

**⚠️ Build verification status:** `./gradlew :app:compileDebugKotlin` **succeeds** — all Kotlin compiles and `kspDebugKotlin` passes, so the **entire Hilt dependency graph resolves and type-checks**. The final **dex/APK packaging step could NOT be run to completion in this session** because the host RAM is exhausted (commit ~27 GB against a ~31.5 GB limit with a fixed 8 GB pagefile; two IDEs + browsers resident) → `d8` hit a native `malloc` OOM and crashed the daemon. **This is purely environmental, not a code/config issue.** To produce an installable APK: free memory (close IntelliJ/Opera/Edge) or build in Android Studio, then `JAVA_HOME=<jdk21> ./gradlew :app:assembleDebug`.

### Architecture (new `android/app/src/main/java/com/boondi/android/`)

Clean-ish layering, Hilt DI throughout, MVVM (StateFlow-based `UiState` per screen):
- **`data/remote/dto/`** — Moshi DTOs matching backend JSON exactly (`ApiEnvelope<T>`, `CursorPageDto<T>`, Auth/User/Post/Upload/Message DTOs) + `Mappers.kt` (DTO→domain) using the reflective `KotlinJsonAdapterFactory` (no codegen).
- **`data/remote/api/`** — Retrofit interfaces: `AuthApi`, `UserApi`, `PostApi`, `TimelineApi`, `NotificationApi`, `SearchApi` (suspend fns, paths relative to `BASE_URL` = `http://10.0.2.2:8080/api/v1/`).
- **`data/remote/interceptor/`** — `AuthInterceptor` (adds `Bearer`) + `TokenAuthenticator` (401 → refresh via the *plain* client → retry; gives up → `SessionManager.onSessionExpired`).
- **`data/local/`** — `TokenStorage` (EncryptedSharedPreferences, AES-256) + `SessionManager` (single source of auth truth, `StateFlow<AuthState>`).
- **`data/repository/`** — `AuthRepository`, `UserRepository`, `PostRepository`, `TimelineRepository`, `NotificationRepository`, `SearchRepository` returning `ApiResult<T>` (sealed Success/Error via `safeApiCall`/`safeApiCallUnit` — see Sprint 9 notes on when to use which).
- **`di/NetworkModule`** — two OkHttp/Retrofit stacks via `@PlainClient` (public auth, no token/authenticator → breaks the refresh DI cycle) and `@AuthClient` (Bearer + auto-refresh).
- **`domain/model/`** — UI-facing models mirroring web `types/index.ts` (`User`, `Post`, `Author`, `QuotedPost`, `CursorPage`, `AuthSession`, `Notification`, `Hashtag`) + `PostInteractions.kt` (optimistic-toggle helpers, Sprint 9).
- **`ui/`** — `common/` (Avatar w/ Coil + initial fallback, `TimeFormat` relative-time, `InfiniteListHandler`, Loading/Error/Empty), `auth/`, `feed/` (`PostCard`, `HomeScreen`, `FeedViewModel`), `post/` (compose/reply + detail), `profile/` (profile + edit), `bookmarks/`, `notifications/`, `search/`, `shell/` (`HomeShell` bottom-nav, Sprint 9).
- **`navigation/BoondiNavHost`** — `BoondiApp()` + `Routes`; start destination and global login/logout navigation driven by `MainViewModel.authState` (observes `SessionManager`). `Routes.HOME` renders `HomeShell` (Sprint 9), not a bare screen.

### Key decisions & notes

- **No `GET /users/me` endpoint exists** — the app persists the login `user` in `TokenStorage` and resolves "own profile" via that username (`GET /users/{username}`).
- **Registration auto-logs-in** (backend returns tokens on `POST /auth/register`), so register → home directly. If a future backend change stops returning tokens on register, `AuthRepository.register` would need to route to login instead.
- **Interaction endpoints** (like/repost/bookmark) are wired into the UI with optimistic updates as of Sprint 9 (E6-16) — see that section below for the pattern (`domain/model/PostInteractions.kt`).
- **Reply composition** shipped in Sprint 9 (E6-18) by reusing `ComposePostScreen`'s `parentPostId` param.
- **Networking deps added** (`app/build.gradle.kts`): Retrofit 2.11.0 + converter-moshi, Moshi 1.15.1 (+ `moshi-kotlin`), OkHttp 4.12.0 (+ logging), `androidx.security:security-crypto` 1.1.0-alpha06, Coil 2.7.0, lifecycle-*-compose, material-icons-extended. `BuildConfig.BASE_URL` added (10.0.2.2 for emulator→host).
- **Cleartext HTTP** to the dev backend is allowed only for `10.0.2.2`/`localhost`/`127.0.0.1` via `res/xml/network_security_config.xml` (production stays HTTPS-only).

## Backend API Summary (Sprints 1–9)

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
| GET | /users/me/bookmarks | Required | Bookmarked posts, most recently bookmarked first (cursor) |
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
| GET | /notifications/unread-count | Required | Unread notification count |
| GET | /search/users?q= | Public | Search users (offset cursor) |
| GET | /search/posts?q= | Public | Full-text search posts (offset cursor) |
| GET | /search/hashtags?q= | Public | Search hashtags by prefix (offset cursor) |
| GET | /hashtags/trending | Public | Top 10 hashtags, last 24h (Redis-cached) |
| POST | /reports | Required | Report a post or user (exactly one target) |
| GET | /admin/users | Admin | List users, newest first (cursor) |
| PUT | /admin/users/{userId}/suspend | Admin | Suspend a user |
| PUT | /admin/users/{userId}/unsuspend | Admin | Unsuspend a user |
| DELETE | /admin/posts/{postId} | Admin | Delete any post (moderation) |
| GET | /admin/reports | Admin | List reports, newest first (cursor) |
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
| Epic 1 | Foundation & DevOps | 23 | 21 | 2 (Android init done; remaining is pre-existing point-count drift, not tracked work) |
| Epic 2 | Authentication | 39 | 28 | 11 (Android auth done Sprint 8; remainder is pre-existing point drift) |
| Epic 3 | User Profiles | 20 | 20 | 0 (Android E3-06/07 done Sprint 8) |
| Epic 4 | Posts | 29 | 29 | 0 (Android E4-09/10/11 done Sprint 8) |
| Epic 5 | Timeline & Feed | 31 | 29 | 2 (Android E5-08/09 done Sprint 8; remainder is pre-existing point drift) |
| Epic 6 | Social Interactions | 41 | 41 | 0 (Android E6-16/17/18/19 done Sprint 9) |
| Epic 7 | Notifications | 20 | 20 | 0 (Android E7-08/09 done Sprint 9) |
| Epic 8 | Search | 23 | 23 | 0 (Android E8-08 done Sprint 9) |
| Epic 9 | Admin | 16 | 16 | 0 (done Sprint 9) |
| Epic 10 | Polish/Testing/Launch | 50 | 50 | 0 |

---

## Sprint 9 — COMPLETE ✅

**Sprint Goal (per Sprint-and-Release-Plan.md §6, Sprint 9):** Android social interactions complete + Admin panel live on web/backend. Android private-beta ready (`v0.3.0` milestone — tag not cut this session; see note below).

| ID | Story | Status |
|----|-------|--------|
| E6-16 | Android: Like/Bookmark/Repost interactions on post card (optimistic) | ✅ |
| E6-17 | Android: Follow/Unfollow on profile | ✅ (shipped Sprint 8; confirmed it already met this story's criteria) |
| E6-18 | Android: Reply screen (parent post preview + composer) | ✅ |
| E6-19 | Android: Bookmarks screen | ✅ |
| E7-08 | Android: Notifications screen | ✅ |
| E7-09 | Android: Unread badge on bottom nav | ✅ |
| E8-08 | Android: Search screen (Users/Posts/Hashtags tabs) | ✅ |
| E9-01 | Backend: Admin role + RBAC enforcement | ✅ |
| E9-02 | Backend: Suspend/unsuspend user API | ✅ |
| E9-03 | Backend: Admin delete post API | ✅ |
| E9-04 | Backend: Get reports API (paginated) | ✅ |
| E9-05 | Backend: Create report API (user-facing) | ✅ |
| E9-06 | Web: Admin panel (`/admin`) | ✅ |

### Admin epic (E9-01→06) — backend + web

- **RBAC groundwork was already in place**: `UserRole.ADMIN`, `User.isSuspended`, `@EnableMethodSecurity`, and `CustomUserDetailsService` granting `ROLE_ADMIN`/`ROLE_USER` authorities all predate this sprint. E9-01 was mostly `@PreAuthorize("hasRole('ADMIN')")` on the new `AdminController` (class-level) plus one real gap: **`GlobalExceptionHandler` had no handler for Spring Security's `AccessDeniedException`**, so a non-admin hitting an admin route would have fallen through to the generic 500 handler instead of a 403. Added a dedicated handler mapping to `ACCESS_DENIED`.
- **Flyway V7** (`reports` table): `reporter_id` (FK), `reported_user_id` **or** `reported_post_id` (both nullable FKs, enforced exactly-one via a `CHECK` constraint), `reason`, `created_at`.
- **`ReportService`**: `createReport` validates exactly one target is set and rejects self-reports (`cannotReportSelf`); `getReports` paginates admin-side by `createdAt` cursor (same pattern as `NotificationService`).
- **`AdminService`**: `getUsers` (new `UserRepository.findAllForAdmin` cursor query — **not a separately-planned story**, but required infrastructure for E9-06's user list, same "flagged gap" pattern Sprint 7 used for bookmarks/unread-count), `suspendUser`/`unsuspendUser` (flip `User.isSuspended`), `deletePost` (delegates to a new `PostService.adminDeletePost`).
- **`PostService` refactor**: extracted the soft-delete + counter-rollback logic from `deletePost(postId, userId)` into a private `softDeletePost(post)` helper, reused by the new `adminDeletePost(postId)` (same effect, no ownership check).
- **`UserResponse` gained a `suspended` boolean** (mapped from `User.isSuspended`) — didn't exist before; the admin panel needs it to render Suspend vs. Unsuspend, and there was no other way to know a user's suspension state from the API.
- **Web**: `AdminPage` (`/admin`, admin-only via new `AdminRoute` guard checking `user.role === 'ADMIN'`) with Users tab (list + suspend/unsuspend) and Reports tab (list + view reported user/post + delete post). Nav entry point: a shield icon on `HomePage`'s top bar, shown only when `user?.role === 'ADMIN'`.

### Android social/search track (E6-16→E8-08)

- **E6-16 optimistic interactions**: `Post.withLikeToggled()/withRepostToggled()/withBookmarkToggled()` (new `domain/model/PostInteractions.kt`) flip local state immediately; each screen's ViewModel (`FeedViewModel`, `ProfileViewModel`, `PostDetailViewModel`, `BookmarksViewModel`, `SearchViewModel`) applies the optimistic `Post` to its list, fires the API call, and either replaces with the server's response or reverts on failure. `PostCard` gained `onReplyClick`/`onLikeClick`/`onRepostClick`/`onBookmarkClick` (bookmark icon didn't exist on the card before this sprint); `PostDetailScreen`'s `DetailPostHeader` got the same real action bar (previously read-only counts).
- **E6-18 reply screen**: reused `ComposePostScreen` rather than a new screen — `Routes.COMPOSE` became `"compose?parentPostId={parentPostId}"` (optional nav arg), and `ComposePostViewModel` now reads `parentPostId` from `SavedStateHandle`, fetches that parent post, and exposes it for a read-only "Replying to @username" preview header. Every `PostCard`'s reply icon and `DetailPostHeader`'s reply stat now navigate here.
- **E6-19 bookmarks**: `GET /users/me/bookmarks` (existed since Sprint 7) → new `UserApi.getMyBookmarks` + `UserRepository.getMyBookmarks` + `BookmarksViewModel`/`BookmarksScreen`. Entry point is a bookmark icon on the Home tab's top bar (bottom nav was reserved for Home/Search/Alerts/Profile — see below).
- **E7-08/09 notifications + badge**: `NotificationApi`/`NotificationRepository`/`NotificationsViewModel`/`NotificationsScreen`/`NotificationItem` (unread dot + tinted row, "Mark all read", type→action-text mapping, taps navigate to the post or — for `FOLLOW` — the actor's profile). The unread **badge** lives in a new `NavShellViewModel` (polls `GET /notifications/unread-count` every 30s while the shell is alive) and renders via `BadgedBox` on the bottom nav's Alerts tab.
- **E8-08 search**: `SearchApi`/`SearchRepository`/`SearchViewModel`/`SearchScreen`. 300ms-debounced query (`Flow.debounce`, mirrors the web app's `setTimeout` debounce), three independent tabs (Users/Posts/Hashtags) that only fetch the active tab and re-fetch on tab switch — same trade-off Sprint 7's web `SearchPage` made. Tapping a hashtag result searches that tag on the Posts tab (no hashtag-detail route, same as web).
- **Bottom-nav shell** (introduced to host Search/Alerts/Profile + the E7-09 badge): new `ui/shell/HomeShell.kt` wraps a **nested** `NavController`/`NavHost` (Home/Search/Notifications/Profile-self tabs) inside a `Scaffold(bottomBar = NavigationBar {...})`. The *outer* `BoondiNavHost`'s `Routes.HOME` now renders `HomeShell` instead of `HomeScreen` directly; all cross-cutting navigation (opening any post, another user's profile, compose, edit profile) still goes through the outer `NavController` passed in via callbacks, so those destinations correctly cover the bottom bar when pushed. `ProfileScreen.onBack` became nullable (`(() -> Unit)?`) — the shell's own-profile tab has no back stack to pop, so the arrow is hidden there rather than wired to a no-op.
- **Bug fix (pre-existing, found while building E7-08's mark-as-read)**: the backend returns `ApiResponse.success(null, "...")` — a genuinely null `data` — for `DELETE /posts/{id}`, `PUT /notifications/{id}/read`, and `PUT /notifications/read-all`. The original `safeApiCall` helper treated `success == true && data == null` as a **failure** (it requires non-null data), so `PostRepository.deletePost` had been silently broken since Sprint 8 — deleting a post always surfaced as an error to the UI even though the server had already deleted it. Added `safeApiCallUnit` (checks only `envelope.success`, ignores `data`) and switched `deletePost`/`markAsRead`/`markAllAsRead` to it.

### Build verification

- **Backend**: `./mvnw -q -o compile` — clean, including the new `Report`/`AdminController`/`ReportController`/`AdminService`/`ReportService`/`ReportMapper` and the `GlobalExceptionHandler`/`PostService`/`UserResponse`/`UserMapper` edits.
- **Web**: `npx tsc -b` — clean, including `AdminPage`, `AdminRoute`, `adminApi`, and the `Report`/`UserProfile.suspended` type additions.
- **Android**: `JAVA_HOME=<jdk21> ./gradlew :app:compileDebugKotlin` — **BUILD SUCCESSFUL**, run and re-verified clean after each feature slice this sprint (interactions, reply, bookmarks, notifications, search, shell wiring) — `kspDebugKotlin` passing each time means the full Hilt graph (5 new repositories/APIs, half a dozen new ViewModels) resolves and type-checks.
- **Android APK packaging (`assembleDebug`) was still not completed this session** — same root cause as Sprint 8: host RAM exhaustion during the `d8` dexing step (commit was 24.9 GB against a 27.7 GB limit, 0 free physical, at last check — tighter than Sprint 8's already-tight state). This is purely environmental; not attempted repeatedly since it wasn't going to newly succeed without freeing memory. **Next session (or whenever RAM is free): run `assembleDebug`/`installDebug` and smoke-test the full flow** (interactions, reply, bookmarks, notifications badge, search, admin panel) against a running backend.
- **`v0.3.0` tag**: not cut — the plan ties it to Android private-beta readiness after a verified, installed build; do that once `assembleDebug` succeeds and the manual smoke test passes.

---

## Sprint 10 — COMPLETE ✅

**Sprint Goal (per Sprint-and-Release-Plan.md §6, Sprint 10):** Cross-cutting hardening — testing, security, performance, production deploy. No new features. Final sprint before `v1.0.0`.

| ID | Story | Status |
|----|-------|--------|
| E10-01 | Backend: Global exception handler audit | ✅ |
| E10-02 | Backend: Input validation audit on request DTOs | ✅ |
| — | Backend: Admin role seeding mechanism (flagged gap from Sprint 9) | ✅ |
| E10-03 | Backend: Rate limiting (Bucket4j) | ✅ |
| E10-04 | Backend: Unit tests (JUnit5 + Mockito) | ✅ |
| E10-05 | Backend: Integration tests (TestContainers) | ✅ |
| E10-06 | Web: Error boundaries + empty states | ✅ |
| E10-07 | Web: Loading skeletons | ✅ |
| E10-08 | Web: Dark mode | ✅ |
| E10-09 | Android: Offline/error states | ✅ |
| E10-10 | Android: App icon + splash screen | ✅ |
| E10-11 | Performance audit (Redis/query/indices review) | ✅ |
| E10-12 | Security review, OWASP Top 10 | ✅ |
| E10-13 | Production deploy config + Nginx SSL | ✅ |
| E10-14 | Swagger completion | ✅ (already complete from prior sprints — every endpoint had `@Operation`, global bearer auth scheme was already configured; no changes needed) |
| E10-15 | Beta bug-fix buffer | ✅ (absorbed into the exception-handler/validation/upload-validation fixes below — no separate buffer work needed) |

### E10-01/02 — Exception handling + input validation

- `GlobalExceptionHandler` gained 7 handlers ahead of the generic 500 fallback: malformed JSON body (400), type-mismatch path/query params (400), missing required query params (400), `ConstraintViolationException` from `@Validated` controller-level params (400, with a per-field violations map), unsupported HTTP method (405), unmatched route (404), oversized upload (413 `FILE_TOO_LARGE`). Previously all of these fell through to a generic 500.
- New `ErrorCode`s: `NOT_FOUND`, `METHOD_NOT_ALLOWED`, `RATE_LIMITED`.
- `CreatePostRequest`/`UpdatePostRequest.imageUrl` capped at 1000 chars; `SearchController`'s three `q` params capped at 100 chars (class-level `@Validated` + `ConstraintViolationException` handler above).

### Admin seeding (flagged gap from Sprint 9)

- New `AdminSeedConfig` (`ApplicationRunner`): reads `app.admin.emails` (comma-separated, `ADMIN_EMAILS` env var), promotes each matching registered user to `ADMIN` at startup. Idempotent, logs a warning for unknown emails, deliberately does **not** auto-demote (so removing an email from the list doesn't silently strip an admin who was promoted a different way). This was the only way to reach Epic 9's admin panel/APIs before this sprint (previously required a manual `psql` `UPDATE`).

### E10-03 — Rate limiting (Bucket4j)

- New `RateLimitFilter` (`OncePerRequestFilter`, in-memory token buckets keyed by client IP): 10 req/min on `/auth/**`, 300 req/min on everything else. Reads the first entry of `X-Forwarded-For` when present (so it works correctly behind the production Nginx). Bucket map is capped at 50k tracked IPs with a hard `clear()` on overflow (simple bound, acceptable for MVP scale — a real deployment under sustained attack would want Redis-backed buckets instead of per-instance memory, noted as a future improvement, not done here since it's beyond MVP scope). Returns 429 + `ApiResponse` body with `RATE_LIMITED`.

### E10-04/05 — Test suites

- **59 backend tests, all passing**: 43 unit tests (`AuthServiceTest`, `PostServiceTest`, `InteractionServiceTest`, `ReportServiceTest`, `RateLimitFilterTest`) + 11 `TestContainers` integration tests (`BackendIntegrationTest`, real Postgres 16 + Redis 7 containers) + 5 pre-existing `AuthControllerTest` tests (fixed a shared-container test-isolation bug found in the process — two tests were registering the same username into one class-level container).
- **TestContainers 1.x is incompatible with Docker Engine 29+** (pins API version 1.32; the engine's minimum is 1.44) — fixed only in 2.0.2+. Upgraded to 2.0.5; artifact IDs changed (`postgresql`→`testcontainers-postgresql`, `junit-jupiter`→`testcontainers-junit-jupiter`), Java package names unchanged.
- Integration tests deliberately encode regressions found in the Sprint 9 first-ever-live-run session: the pgjdbc null-cursor cast bug, 401-vs-403 `AuthenticationEntryPoint` behavior, `saveAndFlush` timestamp population, soft-deleted-post interaction guard, auth-endpoint rate-limit burst.
- **Always run Maven with `JAVA_HOME` pointed at JDK 21** (same rule as Gradle) — Mockito/ByteBuddy fail to start under the system JDK 25.

### E10-06/07/08 — Web polish

- `ErrorBoundary` (class component) wraps the router; full-screen fallback + reload button.
- `PostCardSkeleton`/`PostListSkeleton` (`animate-pulse`) replace the loading spinner on `HomePage` and `BookmarksPage`.
- Dark mode: single `.dark` class on `<html>`, one CSS block in `index.css` mapping every light-palette utility class to a dark value (`theme.ts` handles system-preference detection + `localStorage` persistence + a toggle button) — chosen over per-component `dark:` variants to avoid touching every component.

### E10-09/10 — Android polish

- `ApiResult`'s network-failure path now surfaces a friendly `"You're offline — check your connection"` message (`NETWORK_ERROR_MESSAGE` constant); `ErrorState` renders a `CloudOff` icon specifically for that message instead of the generic error icon.
- Real app icon (indigo `#4F46E5` background + white droplet vector — "boondi" is a droplet-shaped sweet) replacing the default Compose placeholder; `androidx.core.splashscreen` 1.0.1 wired into `MainActivity` — `installSplashScreen().setKeepOnScreenCondition { mainViewModel.authState.value is AuthState.Loading }` holds the system splash until the stored session resolves, so the first real frame is already the correct screen instead of a loading spinner.
- Verified via `compileDebugKotlin`/`kspDebugKotlin` (BUILD SUCCESSFUL) — **`assembleDebug`/`installDebug` still not run this session** (deferred; see Android build note below).

### E10-11/12 — Performance + security audit (OWASP Top 10)

Findings, all fixed:

1. **Multipart size limits mismatched the app's own validation.** Spring Boot's defaults (1MB max-file-size / 10MB max-request-size) are *below* `UserService`'s 10MB banner cap — uploads between 1MB and the app's intended limit were silently rejected with a generic 413 before the app's own size check (and its friendlier error message) ever ran. Fixed by setting `spring.servlet.multipart.max-file-size`/`max-request-size` to 10MB in `application.yml`.
2. **Image upload validation only trusted the client-supplied `Content-Type` header.** A caller could label arbitrary bytes `image/jpeg` and have them stored and served back from MinIO under that trusted content type (the exact value is forwarded straight to S3's `PutObject`). Added `ImageContentValidator` (checks real magic bytes for JPEG/PNG/WEBP against the claimed type) and wired it into both `PostService.validatePostImage` and `UserService.validateImage`.
3. **JWT secret had no production safety net.** The dev placeholder (`...-change-in-production-must-be-at-least-256-bits-long`) would sign real tokens if `JWT_SECRET` were forgotten in a prod deploy. `JwtTokenProvider`'s constructor now fails fast (`IllegalStateException` at startup) if the `prod` profile is active and the secret still contains the `"change-in-production"` sentinel.
4. **`/actuator/health` leaked component internals publicly.** `show-details: always` + the endpoint being `permitAll()` (health checks can't require auth, and there's no separate management port) meant anyone could see DB/Redis connectivity and disk-space details. The app's own `HealthController` at `/health` is what Docker/Nginx actually probe — actuator's health only needs to exist for completeness. Changed to `show-details: never`.
5. **Backend Docker image ran as root.** Added a non-root `boondi` user in the runtime stage of `backend/Dockerfile` (`USER boondi` after the jar is copied in).
6. **CORS/JWT/RBAC/refresh-token design were already solid** — verified rather than changed: refresh-token rotation + Redis storage, access-token blacklisting on logout (`TokenService`/`JwtAuthenticationFilter`), password-reset tokens are SHA-256-hashed at rest with 1h expiry and anti-enumeration (`sendPasswordResetEmail` always returns success), `BCryptPasswordEncoder(12)`, no wildcard CORS origins, generic exception handler never leaks stack traces to the client, web app has no `dangerouslySetInnerHTML`/`eval` XSS vectors.
7. **Indices/query performance reviewed** — every hot foreign key (`posts.author_id`, both `follows` directions, `post_likes/reposts/bookmarks.post_id`, `notifications(recipient_id, created_at)`) already has a supporting index from earlier sprints; Redis caching (home timeline, trending hashtags) and the new rate limiter are the two additions since the schema was designed. No further changes needed at MVP scale.

### E10-13 — Production deploy config + Nginx SSL

- **The dev `nginx.conf` never actually served the web app** — no volume/build step ever populated `/usr/share/nginx/html`, so `location /` was always empty in every environment that used it (latent gap, not previously exercised since local dev uses the Vite dev server directly).
- New `nginx/Dockerfile.prod` (multi-stage: `node:20-alpine` builds `web/` → `nginx:alpine` serves the output) + `nginx/nginx.prod.conf`: HTTP→HTTPS redirect (with an ACME HTTP-01 challenge passthrough for certbot), TLS termination (cert paths bind-mounted from `./nginx/ssl`, TLS 1.2/1.3 only), HSTS/`X-Content-Type-Options`/`X-Frame-Options`/`Referrer-Policy` headers, gzip, long-cache-plus-immutable for hashed `/assets/`, no-cache for `index.html`.
- Web build defaults to a **relative** `VITE_API_URL=/api/v1` (build-time `ARG` in `Dockerfile.prod`) — since Nginx proxies `/api/` to the backend on the same origin, the browser's own API calls become same-origin and don't need CORS at all in production (CORS config is still needed for any other consumer, and stays configurable via `CORS_ORIGINS`).
- New `docker-compose.prod.yml`: Postgres/Redis/MinIO have **no host port mappings** (only reachable inside the compose network — dev's `docker-compose.yml` exposes them for local debugging, prod shouldn't); no `mailhog` (real SMTP required via env); every secret (`DB_PASSWORD`, `REDIS_PASSWORD`, `JWT_SECRET`, MinIO creds, `STORAGE_PUBLIC_URL`, `CORS_ORIGINS`, `APP_BASE_URL`) uses the `${VAR:?message}` interpolation form so the deploy fails immediately with a clear message instead of silently starting with a dev placeholder; Redis now requires a password (`--requirepass`) in prod, unlike dev.
- New `application-prod.yml`: disables `springdoc` Swagger UI/API docs by default in production (reduces public attack-surface discoverability of the full API schema, including admin/report endpoints — re-enable per-deployment via env vars if actually needed), quieter logging (`root: WARN`, `com.boondi: INFO`).
- **Verified**: `docker build` succeeds for both the updated `backend/Dockerfile` (non-root user) and the new `nginx/Dockerfile.prod` (web app builds and lands in the image with the expected `index.html`/`assets/`/favicon); `docker compose -f docker-compose.prod.yml config` resolves cleanly with dummy secrets (confirms YAML + required-variable interpolation is correct). **Not verified**: an actual end-to-end run of the prod stack (needs a real domain + TLS certs, which this dev environment doesn't have) — the `nginx -t` config-syntax check only fails on `backend` hostname resolution when run standalone outside the compose network, which is expected, not a bug.

### Build verification

- **Backend**: `JAVA_HOME=<jdk21> ./mvnw -q -o compile` clean; full test suite (`./mvnw -q -o test`) — **59/59 passing** (see E10-04/05 above).
- **Web**: unchanged this sprint from the E10-06/07/08 work — `npx tsc -b` and `npm run build` both clean (verified earlier in the sprint).
- **Android**: `JAVA_HOME=<jdk21> ./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL (verifies E10-09/10 changes + resource merging of the new drawable/theme XML). **`assembleDebug`/`installDebug` still not run** — same open item carried from Sprints 8/9.
- **Docker**: `docker build` verified for both `backend/Dockerfile` (non-root user change) and the new `nginx/Dockerfile.prod`; `docker compose -f docker-compose.prod.yml config` verified with dummy secrets.

### Carried-over open items (not blockers for this sprint, but flagged for whenever they're picked up)

- **Android `assembleDebug`/`installDebug` has never been run in this dev environment** (Sprints 8, 9, and 10 all verified Android changes via `compileDebugKotlin`/`kspDebugKotlin` only, due to host RAM constraints for the `d8` dexing step). The app *has* been run and smoke-tested via a separately-built APK per the "app is not opening" session earlier in this sprint's timeline (ANR fix + emulator gfxstream blank-screen issue, both resolved) — but that was a snapshot before E10-09/10's icon/splash/offline-state changes landed. Worth a fresh `installDebug` + smoke test (icon, splash, airplane-mode offline state) whenever RAM allows.
- **Nginx SSL is unexercised end-to-end** — no real domain/certs available in this dev environment. Whoever deploys this needs to: populate `./nginx/ssl/{fullchain.pem,privkey.pem}` (certbot or self-signed for testing), create a `.env` with the required secrets listed in `docker-compose.prod.yml`'s comments, then `docker compose -f docker-compose.prod.yml up -d --build`.
- **Rate limiting is in-memory, per-instance** — fine for a single-backend-container MVP; would need Redis-backed buckets if the backend is ever horizontally scaled.
- `v1.0.0` tag not cut yet — ties to this sprint's completion per the release plan, but cutting it is a user action, not something done automatically here.
- `v0.3.0` (Android private-beta) tag also still not cut — blocked on the `assembleDebug`/`installDebug` item above, carried since Sprint 9.

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
9. **Android build:** ALWAYS set `JAVA_HOME` to the Temurin **JDK 21** (`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`) — the system default JDK 25 breaks Gradle's embedded Kotlin. SDK is all API 36 (AGP 8.12.3, compileSdk 36, build-tools 36.1.0). If `assembleDebug` dies with a native `malloc`/daemon-disappeared error, it's host RAM exhaustion (close IDEs/browsers), not the code.
10. **Android patterns:** MVVM + Hilt. Screens observe a `StateFlow<…UiState>` from a `@HiltViewModel`; repositories return `ApiResult<T>` (`safeApiCall`) or `ApiResult<Unit>` (`safeApiCallUnit` — **use this one whenever the backend responds with `ApiResponse.success(null, "...")`**, e.g. delete/mark-read endpoints; `safeApiCall` misreads that null `data` as a failure). `SessionManager` is the auth source of truth; nav reacts to its `AuthState`. DTOs (Moshi, `data/remote/dto`) map to `domain/model` via `toDomain()`.

---

## v1.0.0 MVP Launch — Next Steps (not automated; user action required)

Sprint 10 is the last sprint in the plan. What's left before calling this MVP-launched:

1. **Cut `v1.0.0`** once satisfied with the state above.
2. **Get an Android APK actually installed and smoke-tested** (carried open item — see Sprint 10's "Carried-over open items" above), then cut `v0.3.0` for Android private-beta.
3. **Stand up the production stack** on a real host with a real domain: DNS → the host, `.env` with the secrets `docker-compose.prod.yml` requires, TLS certs in `./nginx/ssl/`, then `docker compose -f docker-compose.prod.yml up -d --build`.
4. Anything beyond this (new features, further epics) would be a new milestone, not part of the Sprint 1–10 plan this file has been tracking.

---

*Last updated: 2026-07-08 | Sprint 10 complete — all 10 sprints of the plan done*
