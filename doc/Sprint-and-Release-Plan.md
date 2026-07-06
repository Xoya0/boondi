# Sprint & Release Plan — Boondi

> **Project:** Boondi — Private Social Networking Platform
> **Document Version:** 1.0
> **Created:** 2026-07-02
> **Author:** Engineering Team
> **Status:** Active

---

## Table of Contents

1. [Overview & Planning Philosophy](#1-overview--planning-philosophy)
2. [Team & Roles](#2-team--roles)
3. [Definition of Done](#3-definition-of-done)
4. [Release Strategy](#4-release-strategy)
5. [Epics & User Story Backlog](#5-epics--user-story-backlog)
6. [Sprint Plan (Sprints 1–10)](#6-sprint-plan-sprints-110)
7. [Milestone & Release Map](#7-milestone--release-map)
8. [Risk Register](#8-risk-register)
9. [Velocity & Estimation Notes](#9-velocity--estimation-notes)
10. [Post-MVP Roadmap](#10-post-mvp-roadmap)

---

## 1. Overview & Planning Philosophy

### 1.1 Project Goal

Boondi is a private social networking platform inspired by Twitter/X. The goal of this plan is to ship a working, production-grade MVP suitable for private beta — a platform where users can register, post, follow others, interact through likes/replies/reposts, receive notifications, and be discoverable through search. The MVP targets all three surfaces: a Spring Boot backend, a React/TypeScript web app, and a Kotlin/Jetpack Compose Android app.

The MVP definition is deliberately constrained. Features such as direct messaging, stories, polls, video streaming, and AI integrations are excluded to maintain focus and reduce scope risk. The ambition is a polished, reliable core product — not a feature-complete platform — by the end of Sprint 10.

### 1.2 Planning Approach

This project follows **Scrum with pragmatic adaptations** for a small team. Core Scrum values apply: transparency, inspection, adaptation. However, given the team size (1–3 developers), ceremonies are streamlined to avoid process overhead overtaking actual delivery.

Key principles:
- **Backend-first delivery.** APIs are built before UI so web and Android teams always have real endpoints to integrate against. No frontend development starts without a corresponding, documented API.
- **Iterative slicing.** Each sprint delivers demonstrable, integrated functionality — not just backend work in isolation.
- **Definition of Done is non-negotiable.** A story is not done unless it meets every DoD criterion. Partial work is tracked as in-progress and does not count toward sprint velocity.
- **Scope is fixed per sprint; quality is not sacrificed.** If capacity is exceeded, stories are moved to the next sprint. Technical debt is tracked explicitly.
- **Stories are estimated collaboratively** using Fibonacci points (1, 2, 3, 5, 8, 13). Estimation is based on complexity and effort, not calendar time.

### 1.3 Sprint Cadence

| Parameter | Value |
|---|---|
| Sprint Length | 2 weeks (10 working days) |
| Sprint 1 Start | 2026-07-07 (Tuesday) |
| Sprint 10 End | 2026-11-20 (Friday) |
| Total Duration | ~20 weeks (~5 months) |
| Total Sprints | 10 |

Sprints run Tuesday to Friday of the second week, giving Monday of week 1 as a preparation/planning day. Each sprint begins with Sprint Planning and ends with a Review + Retrospective on the final Friday.

### 1.4 Ceremonies

| Ceremony | Frequency | Duration | When |
|---|---|---|---|
| Sprint Planning | Once per sprint | 2 hours | Sprint start (Tuesday, Week 1) |
| Daily Standup | Daily | 15 minutes | Every working day, 09:30 |
| Sprint Review | Once per sprint | 1 hour | Sprint end (Friday, Week 2) |
| Retrospective | Once per sprint | 45 minutes | Immediately after Review |
| Backlog Refinement | Weekly | 30 minutes | Wednesday, Week 1 of each sprint |

**Daily Standup format (async-friendly for solo/remote):**
- What did I complete yesterday?
- What am I working on today?
- Any blockers?

For a solo developer, standups can be maintained as a brief daily log in a Notion journal or GitHub issue comment to preserve accountability and decision trail.

### 1.5 Tools

| Category | Recommended Tool | Purpose |
|---|---|---|
| Project Management | GitHub Projects (kanban) | Sprint board, issue tracking |
| Issue Tracking | GitHub Issues | User stories, bugs, tasks |
| Documentation | Notion or GitHub Wiki | Architecture, ADRs, this plan |
| API Documentation | Swagger/OpenAPI (SpringDoc) | Live API docs, auto-generated |
| CI/CD | GitHub Actions | Build, test, deploy pipeline |
| Communication | Slack or Discord | Team comms, standup channel |
| Design | Figma (free tier) | Wireframes, component mockups |
| Monitoring | Grafana + Prometheus (post-MVP) | Metrics, alerting |
| Version Control | Git + GitHub | Source control, PRs, code review |

**GitHub Projects setup:** Create one board per sprint. Columns: `Backlog | In Progress | In Review | Done`. Each sprint's committed stories are moved from the product backlog into the active sprint board at Planning.

---

## 2. Team & Roles

### 2.1 Assumed Team Composition

This plan is written for a team of **1–3 developers**. The sections below describe responsibilities for each role. On a solo team, all responsibilities fall to one person — the sequencing guidance in section 2.3 applies in that case.

---

#### Full-Stack Developer (Backend + Web)

**Primary responsibilities:**
- Design and implement the Spring Boot backend using Clean Architecture (Controller → Use Case → Repository)
- Write and maintain Flyway migration scripts for PostgreSQL schema changes
- Implement JWT authentication, Spring Security filter chain, Redis caching
- Build REST APIs documented via Swagger/OpenAPI
- Implement the React/TypeScript web application (Vite + Tailwind CSS)
- Manage auth state, API integration, protected routes on the web client
- Write backend unit tests (JUnit 5 + Mockito) and integration tests (TestContainers)
- Manage CI/CD pipeline, Docker Compose configuration, Nginx configuration
- Deploy to staging and production environments

**Secondary responsibilities:**
- Code reviews for Android PRs (API contract adherence)
- Environment configuration and secrets management

---

#### Android Developer

**Primary responsibilities:**
- Initialize and maintain the Android project (Kotlin, Jetpack Compose, Hilt DI)
- Implement all Android screens using Compose UI components
- Integrate with backend APIs using Retrofit + OkHttp
- Implement secure token storage (EncryptedSharedPreferences) and refresh interceptor
- Handle offline states, loading states, and error states gracefully
- Manage navigation using Jetpack Navigation Compose
- Implement background work (WorkManager for sync tasks if needed)
- Publish beta APK via GitHub Releases or Firebase App Distribution

**Secondary responsibilities:**
- Report API issues or contract mismatches to backend developer
- Write UI tests (Compose Test, Espresso where needed)

---

#### UI/UX Designer (Optional)

If a designer is available, their involvement is highest in Sprints 1–3 (delivering wireframes and component design tokens before UI development begins) and Sprint 10 (polish and accessibility review).

**Responsibilities:**
- Deliver Figma wireframes for all screens before the sprint that implements them
- Define color palette, typography, spacing tokens (used in both Tailwind config and Compose theme)
- Review implemented UIs against designs and file discrepancy issues
- Produce app icon, splash screen assets, and marketing screenshots for beta

---

#### QA Engineer (Optional)

If a QA engineer is available, they integrate into the team from Sprint 5 onward when features stabilize.

**Responsibilities:**
- Write and maintain manual test cases in a test case management tool (or GitHub Issues)
- Execute regression test suite before each release
- File bugs with reproduction steps, screenshots, and severity ratings
- Validate Definition of Done for stories where testing is ambiguous
- Coordinate UAT (User Acceptance Testing) during private beta

---

### 2.2 RACI Matrix

| Activity | Full-Stack Dev | Android Dev | Designer | QA |
|---|---|---|---|---|
| Backend API development | **R/A** | I | — | C |
| Web frontend development | **R/A** | I | C | C |
| Android development | I | **R/A** | C | C |
| Database schema design | **R/A** | I | — | — |
| CI/CD pipeline | **R/A** | C | — | I |
| UI design | I | I | **R/A** | — |
| Test planning & QA | C | C | — | **R/A** |
| Release deployment | **R/A** | I | — | C |
| Sprint ceremonies | **R/A** | R | I | I |

*R = Responsible, A = Accountable, C = Consulted, I = Informed*

---

### 2.3 Solo Developer Sequencing

For a **solo developer** managing backend, web, and Android, the following sequencing prevents context-switching waste and ensures every layer has a stable foundation before the next begins.

```
Phase 1 (Sprints 1–2):   Backend ONLY
                          Focus: All backend APIs for auth + profiles.
                          No UI work. Build API first, test with Postman/Swagger.

Phase 2 (Sprints 3–6):   Backend continues + Web App built on top
                          Focus: Backend completes posts/timeline/social/notifications/search.
                          Web app builds incrementally on completed backend APIs.

Phase 3 (Sprints 7–9):   Android App
                          Focus: Backend is complete. Android consumes stable APIs.
                          No new backend features during Android sprints (bugs only).

Phase 4 (Sprint 10):      Cross-cutting: testing, polish, production deploy
                          Touches all layers. No new features.
```

This sequencing avoids the "frontend waiting on backend" problem and prevents the cognitive overhead of switching between Java, TypeScript, and Kotlin in the same day.

---

## 3. Definition of Done

The Definition of Done (DoD) is the team's quality contract. Work that does not meet the DoD is **not counted** toward sprint velocity and is not considered complete. The DoD applies at three levels.

### 3.1 Story Done

A user story is Done when **all** of the following are true:

- [ ] Code is written and compiles without errors or warnings
- [ ] All acceptance criteria defined in the story are met
- [ ] Unit tests are written and pass (minimum coverage: critical business logic)
- [ ] No regression in existing tests (CI passes)
- [ ] Code has been reviewed (or self-reviewed with a 24-hour gap for solo developers)
- [ ] PR is merged to the `develop` branch (never directly to `main`)
- [ ] API changes are reflected in Swagger documentation
- [ ] No known bugs introduced by this story
- [ ] New database columns/tables have a Flyway migration file
- [ ] Sensitive data (passwords, tokens) is never logged or exposed in responses
- [ ] Feature is accessible in the staging environment

### 3.2 Sprint Done

A sprint is Done when **all** of the following are true:

- [ ] All committed stories meet the Story Done criteria above
- [ ] The sprint board shows all stories in the `Done` column
- [ ] A Sprint Review demo has been conducted showing all deliverables
- [ ] The `develop` branch is deployed to the staging environment
- [ ] No P0 (critical/blocking) bugs exist in staging
- [ ] Sprint velocity is recorded (actual story points completed)
- [ ] Sprint Retrospective is completed and action items are logged
- [ ] Unfinished stories are returned to the product backlog with updated estimates

### 3.3 Release Done

A release is Done when **all** of the following are true:

- [ ] All stories in scope for the release meet the Story Done criteria
- [ ] Full regression test pass on staging (manual or automated)
- [ ] Security checklist reviewed for the release (OWASP Top 10 items relevant to scope)
- [ ] Performance validated under expected load (response times within SLO)
- [ ] `develop` is merged to `main` via a release PR
- [ ] Git tag is created matching the semantic version (`v0.1.0`, `v1.0.0`, etc.)
- [ ] Deployment to production is complete and verified
- [ ] Health checks pass in production (API, database, Redis)
- [ ] Release notes are written and published (GitHub Releases)
- [ ] Rollback procedure is documented and tested
- [ ] For Android: APK is signed and distributed via Firebase App Distribution or GitHub Releases

---

## 4. Release Strategy

### 4.1 Environments

| Environment | Purpose | Who Accesses | Deploy Trigger |
|---|---|---|---|
| **Local** | Development, debugging | Developer only | Manual (`docker compose up`) |
| **Staging** | Integration testing, sprint demos | Team + internal testers | Merge to `develop` (CI/CD) |
| **Production** | Live private beta | Invited beta users | Merge to `main` + manual approval |

**Staging** mirrors production configuration as closely as possible (same Docker images, same PostgreSQL version, same Redis version) but uses separate database and object storage buckets. Environment variables differ; no staging credentials ever reach production.

### 4.2 Branching Strategy

```
main                   ← Production. Only release merges go here.
  └── develop          ← Integration branch. All feature PRs merge here.
        ├── feature/epic2-auth-register
        ├── feature/epic4-post-create
        ├── feature/epic5-home-timeline
        └── ...

hotfix/fix-jwt-expiry  ← Branches from main. Merges to both main AND develop.
release/v0.2.0         ← Optional: staging stabilization branch before major release.
```

**Rules:**
- No direct commits to `main` or `develop`. All work happens in `feature/*` branches.
- Feature branches are named: `feature/<epic-id>-<short-description>` (e.g., `feature/epic2-jwt-refresh`)
- PRs require at least one reviewer approval before merge (or a 24-hour self-review delay for solo)
- `main` always represents production-deployable code
- Hotfixes are the only path to bypass the `develop` → staging → `main` flow, and only for P0 production bugs
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `chore:`, `test:`, `docs:`

### 4.3 Semantic Versioning

```
v{MAJOR}.{MINOR}.{PATCH}

MAJOR  — Breaking change or architectural overhaul (0 during MVP development)
MINOR  — New feature milestone (alpha, beta milestones)
PATCH  — Bug fixes, hotfixes, minor improvements
```

| Version | Phase | Description |
|---|---|---|
| `v0.1.0` | Alpha | Backend API complete and testable |
| `v0.1.x` | Alpha patches | Bug fixes to API during web development |
| `v0.2.0` | Beta — Web | Web app functional, private beta for web users |
| `v0.2.x` | Beta patches | Bug fixes during Android development |
| `v0.3.0` | Beta — Android | Android app functional, private beta expanded |
| `v0.3.x` | Beta patches | Bug fixes from beta feedback |
| `v1.0.0` | **MVP Launch** | Full platform: backend + web + Android, production-ready |

### 4.4 Release Cadence

Formal releases (tags on `main`) occur at the end of milestone sprints:

| Release | Sprint | Target Date |
|---|---|---|
| `v0.1.0` | End of Sprint 2 | 2026-08-01 |
| `v0.2.0` | End of Sprint 6 | 2026-09-25 |
| `v0.3.0` | End of Sprint 9 | 2026-11-06 |
| `v1.0.0` | End of Sprint 10 | 2026-11-20 |

Between milestone releases, `develop` is deployed to staging at the end of every sprint. Only milestone releases are promoted to production.

### 4.5 Rollback Strategy

**Backend:**
- Database rollbacks: Flyway supports undo migrations (or manual SQL rollback scripts are authored alongside every migration)
- Application rollback: Redeploy the previous Docker image tag. Every production deploy tags the image as `boondi-api:v{version}` and keeps the previous two versions available
- Procedure: If a production deploy causes P0 issues within 30 minutes, rollback is initiated without a post-mortem. If after 30 minutes, rollback requires incident review

**Web app:**
- Static files served via Nginx or CDN. Rollback = redeploy previous build artifact
- Previous build artifacts are retained in CI/CD for 30 days

**Android:**
- APK rollback via Firebase App Distribution or Google Play internal testing track staged rollout
- Halt rollout if crash-free rate drops below 99% within 24 hours

**Rollback decision criteria:**
- P0 bug (data loss, security vulnerability, authentication broken, app crashes on launch)
- Error rate > 5% on any critical API endpoint in production

### 4.6 Feature Flags

Feature flags are **optional** for the MVP but recommended for Sprint 10 production launch. Use a simple environment-variable-based flag system (no third-party required):

```java
// application.yml
features:
  trending-timeline: true
  admin-panel: false  # enable only for admin users
```

Feature flags allow:
- Deploying backend code before the UI is ready (safe dark launch)
- Disabling a broken feature without a rollback
- Enabling admin features only for specific user roles
- Gradual rollout to a percentage of beta users (future enhancement)

---

## 5. Epics & User Story Backlog

Story points use the **Fibonacci scale**: 1 (trivial), 2 (small), 3 (medium), 5 (large), 8 (very large), 13 (epic — should be broken down if possible).

---

### EPIC 1: Project Foundation & DevOps Setup

**Epic Goal:** Every developer can clone the repo and run the full stack locally with a single command. CI is running. All three projects (backend, web, Android) are bootstrapped with correct architecture.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E1-01 | Initialize Spring Boot project with Clean Architecture package structure (`domain`, `application`, `infrastructure`, `presentation` layers) | 3 | 1 |
| E1-02 | Set up PostgreSQL + Flyway migrations (initial schema: users table, baseline migration) | 3 | 1 |
| E1-03 | Set up Redis connection and configuration (Spring Data Redis, health check endpoint) | 2 | 1 |
| E1-04 | Configure Docker Compose for local dev (PostgreSQL, Redis, backend, Nginx) | 3 | 1 |
| E1-05 | Set up Nginx reverse proxy config (routes `/api/*` to backend, serves web static files) | 2 | 1 |
| E1-06 | Initialize React + Vite + TypeScript + Tailwind CSS project with folder structure | 2 | 3 |
| E1-07 | Initialize Android project with Jetpack Compose + Hilt + Navigation Compose | 3 | 7 |
| E1-08 | Set up GitHub repository + branching rules + GitHub Actions CI (build + test on PR) | 3 | 1 |
| E1-09 | Swagger/OpenAPI integration (SpringDoc, accessible at `/swagger-ui.html`) | 2 | 1 |

**Epic Total: 23 points**

---

### EPIC 2: Authentication

**Epic Goal:** Users can register, verify email, log in, refresh their session, reset a forgotten password, and log out. JWTs are issued and validated correctly. All three platforms handle auth flows end-to-end.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E2-01 | Backend: User registration API (`POST /api/v1/auth/register`) with validation | 3 | 1 |
| E2-02 | Backend: Login API (`POST /api/v1/auth/login`) + JWT access + refresh token generation | 3 | 1 |
| E2-03 | Backend: Refresh token API (`POST /api/v1/auth/refresh`) with rotation | 3 | 2 |
| E2-04 | Backend: Logout + refresh token revocation (`POST /api/v1/auth/logout`) | 2 | 2 |
| E2-05 | Backend: Email verification flow (send verification link, `GET /api/v1/auth/verify-email`) | 3 | 2 |
| E2-06 | Backend: Forgot password (`POST /api/v1/auth/forgot-password`) + reset password (`POST /api/v1/auth/reset-password`) | 3 | 2 |
| E2-07 | Backend: Spring Security config + JWT filter chain (stateless, extract claims, set SecurityContext) | 5 | 1 |
| E2-08 | Web: Login screen (email/password form, error states, redirect on success) | 2 | 3 |
| E2-09 | Web: Registration screen (username, email, password, confirm password, validation) | 2 | 3 |
| E2-10 | Web: Auth state management (Zustand/Context) + protected routes (redirect to login if unauthenticated) | 3 | 3 |
| E2-11 | Web: Forgot password screen + Reset password screen (token from email link) | 2 | 3 |
| E2-12 | Android: Login screen (Compose) with ViewModel + state hoisting | 3 | 7 |
| E2-13 | Android: Registration screen (Compose) | 2 | 7 |
| E2-14 | Android: Token storage (EncryptedSharedPreferences) + OkHttp refresh interceptor (auto-refresh on 401) | 3 | 7 |

**Epic Total: 39 points**

---

### EPIC 3: User Profiles

**Epic Goal:** Users can view their own and others' profiles, edit display name/bio/handle, and upload an avatar and banner image. Follower and following counts are displayed.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E3-01 | Backend: Get user profile API (`GET /api/v1/users/{username}`) — returns profile + follower/following counts | 2 | 2 |
| E3-02 | Backend: Update profile API (`PUT /api/v1/users/me`) — display name, bio, location, website | 3 | 2 |
| E3-03 | Backend: Upload avatar + banner to object storage (S3-compatible) (`POST /api/v1/users/me/avatar`, `/banner`) | 5 | 2 |
| E3-04 | Web: View profile page (avatar, banner, bio, post count, follower/following counts, user's posts) | 3 | 4 |
| E3-05 | Web: Edit profile modal/page (update fields, upload avatar/banner with preview) | 2 | 4 |
| E3-06 | Android: Profile screen (Compose) — header with avatar/banner, stats row, posts list | 3 | 8 |
| E3-07 | Android: Edit profile screen — form fields + image picker for avatar/banner | 2 | 8 |

**Epic Total: 20 points**

---

### EPIC 4: Posts

**Epic Goal:** Authenticated users can create a text post (up to 500 characters), optionally attach an image, view a single post, edit their post, and delete it.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E4-01 | Backend: Create post API (`POST /api/v1/posts`) — text, optional image URL, UUID PK | 3 | 3 |
| E4-02 | Backend: Edit post API (`PUT /api/v1/posts/{postId}`) — only by author, within 30 minutes of creation | 2 | 3 |
| E4-03 | Backend: Delete post (soft delete) API (`DELETE /api/v1/posts/{postId}`) | 2 | 3 |
| E4-04 | Backend: Get post by ID API (`GET /api/v1/posts/{postId}`) | 1 | 3 |
| E4-05 | Backend: Image upload for posts (presigned S3 URL or direct upload endpoint) | 5 | 3 |
| E4-06 | Web: Post composer component (textarea with character counter, image attach, submit) | 3 | 4 |
| E4-07 | Web: Post card component (avatar, display name, handle, time, content, action bar: like/reply/repost/bookmark) | 3 | 4 |
| E4-08 | Web: Post detail page (`/post/:id`) with post + reply thread | 2 | 5 |
| E4-09 | Android: Post composer screen/bottom sheet (text input, char counter, image picker, post button) | 3 | 8 |
| E4-10 | Android: Post card composable (mirrors web card layout in Compose) | 3 | 8 |
| E4-11 | Android: Post detail screen | 2 | 8 |

**Epic Total: 29 points**

---

### EPIC 5: Timeline & Feed

**Epic Goal:** Authenticated users see a personalized home feed (posts from followed users), a latest feed (all posts chronological), a trending feed, and a user's profile timeline. Feeds are paginated and cached.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E5-01 | Backend: Home timeline API (`GET /api/v1/timelines/home`) — posts from followed users, cursor-based pagination | 5 | 4 |
| E5-02 | Backend: Latest timeline API (`GET /api/v1/timelines/latest`) — all posts, reverse chronological | 3 | 4 |
| E5-03 | Backend: User timeline API (`GET /api/v1/users/{username}/posts`) — posts by a specific user | 2 | 4 |
| E5-04 | Backend: Trending timeline API (`GET /api/v1/timelines/trending`) — most liked/reposted in last 24h | 5 | 5 |
| E5-05 | Backend: Redis caching for home timeline (cache per user, invalidate on new post by followee) | 5 | 5 |
| E5-06 | Web: Home feed page with tab switcher (Home / Latest / Trending tabs) | 3 | 5 |
| E5-07 | Web: Infinite scroll / cursor-based pagination (IntersectionObserver, load next page on scroll end) | 3 | 5 |
| E5-08 | Android: Home feed screen with LazyColumn + pull-to-refresh (SwipeRefresh) | 3 | 8 |
| E5-09 | Android: Tab switcher (Home / Latest / Trending) with Pager/Tab layout | 2 | 8 |

**Epic Total: 31 points**

---

### EPIC 6: Social Interactions

**Epic Goal:** Users can like/unlike posts, repost, quote-post, bookmark, follow/unfollow other users, and reply to posts. Follower/following lists are viewable.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E6-01 | Backend: Like / Unlike API (`POST /api/v1/posts/{postId}/like`, `DELETE`) | 2 | 5 |
| E6-02 | Backend: Repost API (`POST /api/v1/posts/{postId}/repost`) | 2 | 5 |
| E6-03 | Backend: Quote post API (`POST /api/v1/posts`) with `quotedPostId` field | 3 | 5 |
| E6-04 | Backend: Bookmark / Unbookmark API (`POST /api/v1/posts/{postId}/bookmark`, `DELETE`) | 2 | 5 |
| E6-05 | Backend: Follow / Unfollow API (`POST /api/v1/users/{username}/follow`, `DELETE`) | 2 | 5 |
| E6-06 | Backend: Followers list API (`GET /api/v1/users/{username}/followers`) paginated | 2 | 5 |
| E6-07 | Backend: Following list API (`GET /api/v1/users/{username}/following`) paginated | 2 | 5 |
| E6-08 | Backend: Reply (create comment) API — post with `parentPostId`, appears in replies thread | 3 | 5 |
| E6-09 | Web: Like button with optimistic update + animation (heart fill on click) | 2 | 6 |
| E6-10 | Web: Bookmark button (toggle, persists via API) | 1 | 6 |
| E6-11 | Web: Follow/Unfollow button on profile page (optimistic update) | 2 | 6 |
| E6-12 | Web: Reply composer (threaded reply below post on post detail page) | 2 | 6 |
| E6-13 | Web: Repost / Quote post UI (dropdown: Repost or Quote, quote opens composer with embedded post) | 3 | 6 |
| E6-14 | Web: Bookmarks page (`/bookmarks`) — paginated list of bookmarked posts | 2 | 6 |
| E6-15 | Web: Followers/Following page (`/profile/:username/followers`, `/following`) | 2 | 6 |
| E6-16 | Android: Like, Bookmark, Repost interactions on post card (tap handlers, optimistic state) | 3 | 9 |
| E6-17 | Android: Follow/Unfollow button on profile screen | 2 | 9 |
| E6-18 | Android: Reply screen (navigate to reply composer with parent post preview) | 2 | 9 |
| E6-19 | Android: Bookmarks screen | 2 | 9 |

**Epic Total: 41 points**

---

### EPIC 7: Notifications

**Epic Goal:** Users receive in-app notifications when someone likes their post, replies, follows them, mentions them, or reposts them. Unread count is displayed. Notifications can be marked as read.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E7-01 | Backend: Notification creation service (triggered on like/reply/follow/mention/repost events) | 5 | 6 |
| E7-02 | Backend: Get notifications API (`GET /api/v1/notifications`) — paginated, with `isRead` flag | 2 | 6 |
| E7-03 | Backend: Mark notification as read API (`PUT /api/v1/notifications/{id}/read`) | 1 | 6 |
| E7-04 | Backend: Mark all notifications as read API (`PUT /api/v1/notifications/read-all`) | 1 | 6 |
| E7-05 | Web: Notifications page (`/notifications`) — list of notification items | 3 | 7 |
| E7-06 | Web: Notification item component (avatar, action text, time, post preview link) | 2 | 7 |
| E7-07 | Web: Unread count badge on bell icon in nav (polled every 30s or on focus) | 2 | 7 |
| E7-08 | Android: Notifications screen (LazyColumn of notification items) | 3 | 9 |
| E7-09 | Android: Unread badge on bottom navigation bar notifications tab | 1 | 9 |

**Epic Total: 20 points**

---

### EPIC 8: Search

**Epic Goal:** Users can search for other users by name/handle, search posts by keyword using full-text search, and discover trending hashtags. Hashtags are extracted on post creation.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E8-01 | Backend: Search users API (`GET /api/v1/search/users?q=`) — ILIKE on username/display name | 3 | 6 |
| E8-02 | Backend: Search posts API (`GET /api/v1/search/posts?q=`) — PostgreSQL `tsvector` full-text search | 5 | 6 |
| E8-03 | Backend: Search hashtags API (`GET /api/v1/search/hashtags?q=`) | 2 | 6 |
| E8-04 | Backend: Hashtag extraction on post create (parse `#word` patterns, persist to `hashtags` + `post_hashtags` tables) | 2 | 6 |
| E8-05 | Backend: Trending hashtags API (`GET /api/v1/hashtags/trending`) — most used in last 24h, cached in Redis | 3 | 6 |
| E8-06 | Web: Search page (`/search`) with tabs: Users / Posts / Hashtags | 3 | 7 |
| E8-07 | Web: Search input with 300ms debounce (avoid API spam), results update on type | 2 | 7 |
| E8-08 | Android: Search screen (search bar + tabbed results: Users/Posts/Hashtags) | 3 | 9 |

**Epic Total: 23 points**

---

### EPIC 9: Admin

**Epic Goal:** Admin users can suspend or unsuspend accounts, delete any post, view user reports, and receive reports from users. Role-based access control prevents non-admins from accessing admin endpoints.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E9-01 | Backend: Admin role definition + RBAC enforcement (`@PreAuthorize("hasRole('ADMIN')")` on admin endpoints) | 3 | 9 |
| E9-02 | Backend: Suspend user API (`PUT /api/v1/admin/users/{userId}/suspend`) + unsuspend | 2 | 9 |
| E9-03 | Backend: Admin delete post API (`DELETE /api/v1/admin/posts/{postId}`) | 2 | 9 |
| E9-04 | Backend: Get reports API (`GET /api/v1/admin/reports`) — paginated | 2 | 9 |
| E9-05 | Backend: Create report API (`POST /api/v1/reports`) — user-facing, report a post or user | 2 | 9 |
| E9-06 | Web: Admin panel (`/admin`) — user list with suspend/unsuspend action, reported posts list | 5 | 9 |

**Epic Total: 16 points**

---

### EPIC 10: Polish, Testing & Launch

**Epic Goal:** The platform is production-hardened — error handling is consistent, critical paths have test coverage, performance is validated, security is reviewed, and the production environment is deployed with SSL.

| ID | Story | Points | Sprint |
|---|---|---|---|
| E10-01 | Backend: Global exception handler (`@ControllerAdvice`) — consistent error response format | 3 | 10 |
| E10-02 | Backend: Input validation (`@Valid`, Bean Validation annotations on all DTOs) | 2 | 10 |
| E10-03 | Backend: Rate limiting (Bucket4j or Spring Gateway) — limit per IP and per user | 3 | 10 |
| E10-04 | Backend: Unit tests — service layer (JUnit 5 + Mockito, critical paths: auth, posts, timeline) | 5 | 10 |
| E10-05 | Backend: Integration tests — API endpoints (TestContainers with real PostgreSQL + Redis) | 5 | 10 |
| E10-06 | Web: Error boundaries (React ErrorBoundary on route level) + empty states for feeds | 3 | 10 |
| E10-07 | Web: Loading skeletons for feed, profile, notifications (skeleton placeholders) | 2 | 10 |
| E10-08 | Web: Dark mode (Tailwind `dark:` classes + system preference detection) | 3 | 10 |
| E10-09 | Android: Offline handling + error states (no internet banner, retry buttons) | 3 | 10 |
| E10-10 | Android: App icon + splash screen (Compose SplashScreen API) | 2 | 10 |
| E10-11 | Performance audit + Redis cache tuning (identify slow queries with EXPLAIN ANALYZE, tune cache TTLs) | 3 | 10 |
| E10-12 | Security review (OWASP Top 10 checklist: SQL injection, XSS, IDOR, broken auth, sensitive data exposure) | 3 | 10 |
| E10-13 | Production deployment + Nginx SSL (Let's Encrypt TLS certificate, HTTPS redirect) | 3 | 10 |
| E10-14 | Swagger documentation completion (ensure all endpoints have descriptions, request/response examples) | 2 | 10 |
| E10-15 | Beta testing + bug fixes (dedicated buffer for bugs surfaced in private beta) | 8 | 10 |

**Epic Total: 50 points**

---

### Backlog Summary

| Epic | Name | Story Points |
|---|---|---|
| Epic 1 | Project Foundation & DevOps | 23 |
| Epic 2 | Authentication | 39 |
| Epic 3 | User Profiles | 20 |
| Epic 4 | Posts | 29 |
| Epic 5 | Timeline & Feed | 31 |
| Epic 6 | Social Interactions | 41 |
| Epic 7 | Notifications | 20 |
| Epic 8 | Search | 23 |
| Epic 9 | Admin | 16 |
| Epic 10 | Polish, Testing & Launch | 50 |
| **TOTAL** | | **292 points** |

---

## 6. Sprint Plan (Sprints 1–10)

> **Sprint Velocity Assumption:**
> - Solo developer: 28–32 points/sprint (achievable with focused work, no major blockers)
> - 2-person team: adjust committed points upward by ~50%
> - All sprints include a ~20% buffer — committed points are below theoretical max velocity

---

### Sprint 1 — 2026-07-07 to 2026-07-18

**Sprint Goal:** The backend project is initialized with clean architecture, connected to PostgreSQL and Redis, and the core authentication APIs (register, login, JWT) are working and testable via Swagger.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E1-01 | Initialize Spring Boot + Clean Architecture structure | 3 |
| E1-02 | PostgreSQL + Flyway initial schema (users table) | 3 |
| E1-03 | Redis connection + health check | 2 |
| E1-04 | Docker Compose (PG + Redis + backend + Nginx) | 3 |
| E1-05 | Nginx reverse proxy config | 2 |
| E1-08 | GitHub repo + CI pipeline (GitHub Actions) | 3 |
| E1-09 | Swagger/OpenAPI integration | 2 |
| E2-07 | Spring Security config + JWT filter chain | 5 |
| E2-01 | User registration API | 3 |
| E2-02 | Login API + JWT generation | 3 |

**Sprint Total: 29 points**

**Deliverables:**
- `docker compose up` brings up the full backend stack locally
- `POST /api/v1/auth/register` and `POST /api/v1/auth/login` work and return JWTs
- Swagger UI accessible at `http://localhost:8080/swagger-ui.html`
- CI pipeline runs on every PR and reports pass/fail
- PostgreSQL schema v1 (users table) is managed by Flyway

**Dependencies / Risks:**
- Docker Desktop must be installed and working on developer machines
- Object storage (S3) is not required yet — defer to Sprint 2/3
- Spring Security + JWT filter is the highest-risk story this sprint (5 pts). If it slips, defer E2-01/E2-02 to Sprint 2 and pull from Sprint 2's backlog

---

### Sprint 2 — 2026-07-21 to 2026-08-01

**Sprint Goal:** Authentication is fully complete (email verification, password reset, refresh/logout). User profile APIs are live. The backend v0.1.0 alpha is releasable.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E2-03 | Refresh token API with rotation | 3 |
| E2-04 | Logout + token revocation | 2 |
| E2-05 | Email verification flow (send link, verify endpoint) | 3 |
| E2-06 | Forgot password + reset password APIs | 3 |
| E3-01 | Get user profile API | 2 |
| E3-02 | Update profile API | 3 |
| E3-03 | Upload avatar + banner (S3 object storage) | 5 |
| E1-02 *(carry-over budget)* | Flyway migrations for profiles table | — |

**Sprint Total: 21 points** *(lighter sprint — Sprint 1 was front-loaded; use remaining capacity for refinement and Swagger docs cleanup)*

**Deliverables:**
- Complete authentication API: register → verify email → login → refresh → logout → forgot/reset password
- `GET /api/v1/users/{username}` returns full profile
- Avatar/banner upload to object storage works
- **Release: `v0.1.0` tag on `main`** — backend API alpha complete
- All endpoints documented in Swagger with request/response examples

**Dependencies / Risks:**
- Email delivery requires a configured SMTP provider (e.g., SendGrid, Mailgun, or AWS SES). This must be provisioned in Sprint 1 or early Sprint 2. Use a development sandbox (Mailtrap) to unblock the story without a real email account
- Object storage (S3 or Cloudflare R2 or MinIO for local) must be provisioned. Use MinIO via Docker Compose for local dev
- If S3/email setup delays E3-03 or E2-05, those stories move to Sprint 3 and Sprint 3 stories shift accordingly

---

### Sprint 3 — 2026-08-04 to 2026-08-15

**Sprint Goal:** Post CRUD APIs with image upload are complete. The React web project is initialized with login and registration screens connected to the backend.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E4-01 | Create post API | 3 |
| E4-02 | Edit post API | 2 |
| E4-03 | Delete post (soft delete) API | 2 |
| E4-04 | Get post by ID API | 1 |
| E4-05 | Image upload for posts (S3 presigned URLs) | 5 |
| E1-06 | Initialize React + Vite + Tailwind project | 2 |
| E2-08 | Web: Login screen | 2 |
| E2-09 | Web: Registration screen | 2 |
| E2-10 | Web: Auth state + protected routes | 3 |
| E2-11 | Web: Forgot/Reset password screens | 2 |

**Sprint Total: 24 points**

*(Note: Sprint 3 is slightly lighter to accommodate web project bootstrapping cost and React/TypeScript + API integration setup time.)*

**Deliverables:**
- Backend: Create, edit, delete, and view posts via API (Postman/Swagger testable)
- Image upload to object storage works for posts
- Web app runs locally: login and registration screens are functional, calling the real backend API
- Protected routes redirect unauthenticated users to `/login`
- Forgot password email flow works end-to-end in dev environment

**Dependencies / Risks:**
- Image upload presigned URL flow requires object storage to be fully configured (resolved in Sprint 2)
- Web authentication integration requires CORS to be correctly configured on the Spring Boot backend (add to Sprint 2 as a task if not already done)

---

### Sprint 4 — 2026-08-18 to 2026-08-29

**Sprint Goal:** Timeline APIs are complete. The web app shows profile pages and a real feed from the API with post cards and a working composer.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E5-01 | Backend: Home timeline API (paginated) | 5 |
| E5-02 | Backend: Latest timeline API | 3 |
| E5-03 | Backend: User timeline API | 2 |
| E3-04 | Web: View profile page | 3 |
| E3-05 | Web: Edit profile modal/page | 2 |
| E4-06 | Web: Post composer component | 3 |
| E4-07 | Web: Post card component | 3 |

**Sprint Total: 21 points**

*(Lighter point count — Timeline API is architecturally complex; buffer is intentional. Use remaining capacity for tech debt or backlog refinement.)*

**Deliverables:**
- Home timeline and latest timeline APIs return real posts (test with seeded data)
- Web app: profile page renders with avatar, bio, stats
- Web app: post composer creates posts successfully (text + image)
- Web app: post card renders correctly in a list
- The web app has a navigable structure: `/login` → `/home` → `/profile/:username`

**Dependencies / Risks:**
- Home timeline performance depends on query optimization. Ensure a database index on `follows(follower_id)` and `posts(author_id, created_at)` before Sprint 4 ends
- Cursor-based pagination must be designed in Sprint 4 as the pattern used by ALL timelines — define the `PageRequest` structure and response envelope early

---

### Sprint 5 — 2026-09-01 to 2026-09-12

**Sprint Goal:** All social interaction APIs are complete (likes, follows, replies, reposts, bookmarks, trending). The web feed is fully interactive with real-time-feeling optimistic UI.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E5-04 | Backend: Trending timeline API | 5 |
| E5-05 | Backend: Redis caching for home timeline | 5 |
| E6-01 | Backend: Like / Unlike API | 2 |
| E6-02 | Backend: Repost API | 2 |
| E6-03 | Backend: Quote post API | 3 |
| E6-04 | Backend: Bookmark / Unbookmark API | 2 |
| E6-05 | Backend: Follow / Unfollow API | 2 |
| E6-06 | Backend: Followers list API | 2 |
| E6-07 | Backend: Following list API | 2 |
| E6-08 | Backend: Reply API | 3 |
| E4-08 | Web: Post detail page | 2 |

**Sprint Total: 30 points**

**Deliverables:**
- All social APIs are live and testable in Swagger
- Redis caches home timeline — repeat requests are significantly faster
- Trending timeline returns top posts from last 24h
- Web: post detail page shows the post with its reply thread
- `docker compose up` includes all services including MinIO (object storage) and MailHog (email)

**Dependencies / Risks:**
- Redis cache invalidation (E5-05) is the highest-risk story. Define the invalidation strategy before implementing: on new post by a followee → delete that user's followers' cached timelines. A fan-out-on-write approach should be documented and agreed upon before coding starts
- Trending algorithm must be defined: score = (likes * 1) + (reposts * 2) in last 24h. Document the formula in code comments

---

### Sprint 6 — 2026-09-15 to 2026-09-26

**Sprint Goal:** Notifications system and search APIs are complete. Web social interaction UI (likes, follows, replies, bookmarks, repost) is polished. The web app is functionally complete for MVP.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E7-01 | Backend: Notification creation service | 5 |
| E7-02 | Backend: Get notifications API | 2 |
| E7-03 | Backend: Mark notification as read | 1 |
| E7-04 | Backend: Mark all read | 1 |
| E8-01 | Backend: Search users API | 3 |
| E8-02 | Backend: Search posts API (tsvector) | 5 |
| E8-03 | Backend: Search hashtags API | 2 |
| E8-04 | Backend: Hashtag extraction on post create | 2 |
| E8-05 | Backend: Trending hashtags API | 3 |
| E6-09 | Web: Like button with animation | 2 |
| E6-10 | Web: Bookmark button | 1 |

**Sprint Total: 27 points**

*(Sprint 6 is strategically lighter to accommodate integration complexity. The remaining E6 web stories roll into Sprint 7.)*

**Deliverables:**
- Notifications are created when a user likes, replies, follows, mentions, or reposts
- Full-text search works for posts (PostgreSQL tsvector) and users
- Hashtag extraction on post creation is live
- Trending hashtags API returns top hashtags (Redis-cached)
- Web: like and bookmark interactions work with optimistic updates
- **Release: `v0.2.0` tag on `main`** — Web app private beta candidate

**Dependencies / Risks:**
- PostgreSQL `tsvector` full-text search requires a Flyway migration adding a generated column and GIN index to the `posts` table. This migration must be authored and tested before E8-02 development begins
- Notification creation is event-driven (application events within Spring). Ensure thread safety and that events are not lost (consider persisting before publishing)

---

### Sprint 7 — 2026-09-29 to 2026-10-10

**Sprint Goal:** Web app is feature-complete for MVP (notifications, search, all social UIs). Android project is initialized and auth screens are working.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E6-11 | Web: Follow/Unfollow button | 2 |
| E6-12 | Web: Reply composer | 2 |
| E6-13 | Web: Repost / Quote UI | 3 |
| E6-14 | Web: Bookmarks page | 2 |
| E6-15 | Web: Followers/Following page | 2 |
| E7-05 | Web: Notifications page | 3 |
| E7-06 | Web: Notification item component | 2 |
| E7-07 | Web: Unread count badge | 2 |
| E8-06 | Web: Search page with tabs | 3 |
| E8-07 | Web: Search input with debounce | 2 |
| E5-06 | Web: Feed tabs (Home/Latest/Trending) | 3 |
| E5-07 | Web: Infinite scroll / pagination | 3 |
| E1-07 | Initialize Android project (Compose + Hilt) | 3 |

**Sprint Total: 32 points**

**Deliverables:**
- Web app: all MVP features are functional (feed, posts, likes, follows, replies, reposts, bookmarks, notifications, search)
- Web app infinite scroll works on all feed pages
- Android project runs on an emulator showing a splash screen
- Android Hilt DI, Navigation Compose, and Retrofit are configured
- Web app is deployable to staging for internal testing

**Dependencies / Risks:**
- Web app feature completeness is a gate for web-only private beta. QA/review of the web app should happen at Sprint 7 end
- Android project initialization (E1-07) should happen on Day 1 of Sprint 7 to unblock Android auth stories that follow

---

### Sprint 8 — 2026-10-13 to 2026-10-24

**Sprint Goal:** Android app shows a real feed, users can create posts, and profile screens are functional. The Android app is demo-able end-to-end.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E2-12 | Android: Login screen (Compose) | 3 |
| E2-13 | Android: Registration screen | 2 |
| E2-14 | Android: Token storage + refresh interceptor | 3 |
| E3-06 | Android: Profile screen | 3 |
| E3-07 | Android: Edit profile screen | 2 |
| E4-09 | Android: Post composer screen | 3 |
| E4-10 | Android: Post card composable | 3 |
| E4-11 | Android: Post detail screen | 2 |
| E5-08 | Android: Home feed screen (LazyColumn + pull refresh) | 3 |
| E5-09 | Android: Tab switcher (Home/Latest/Trending) | 2 |

**Sprint Total: 26 points**

*(Slightly lighter — Android development involves significant boilerplate, Hilt module wiring, and Retrofit setup not captured in individual stories. The buffer absorbs this.)*

**Deliverables:**
- Android app: register → login → view feed → tap a post detail → view profile
- Android app: compose and submit a text post
- Token refresh works automatically (expired access token triggers refresh, original request retried)
- Profile screen shows avatar, bio, post count, followers/following
- APK installable on a physical Android device

**Dependencies / Risks:**
- Android development assumes the backend API is stable (it is, since Sprint 5). If API changes are needed during Android sprints, they are treated as bugs/patches and scheduled in the `hotfix/*` flow
- Jetpack Compose learning curve: if this sprint falls behind, deprioritize E3-07 and E4-11 (edit profile, post detail) as they are lower risk

---

### Sprint 9 — 2026-10-27 to 2026-11-07

**Sprint Goal:** Android social interactions are complete. Admin panel is live on the web. The Android app is private beta ready.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E6-16 | Android: Like, Bookmark, Repost interactions | 3 |
| E6-17 | Android: Follow/Unfollow on profile | 2 |
| E6-18 | Android: Reply screen | 2 |
| E6-19 | Android: Bookmarks screen | 2 |
| E7-08 | Android: Notifications screen | 3 |
| E7-09 | Android: Unread badge on bottom nav | 1 |
| E8-08 | Android: Search screen | 3 |
| E9-01 | Backend: Admin role + RBAC | 3 |
| E9-02 | Backend: Suspend/unsuspend user API | 2 |
| E9-03 | Backend: Admin delete post API | 2 |
| E9-04 | Backend: Get reports API | 2 |
| E9-05 | Backend: Create report API | 2 |
| E9-06 | Web: Admin panel | 5 |

**Sprint Total: 32 points**

**Deliverables:**
- Android: full social feature set (like, repost, bookmark, reply, follow, search, notifications)
- Admin web panel: admins can view user list, suspend accounts, delete flagged posts
- Users can submit reports via the API
- **Release: `v0.3.0` tag on `main`** — Android private beta

**Dependencies / Risks:**
- Admin panel (E9-06 — 5 pts) is the single largest story in this sprint. If Sprint 9 is overloaded, deprioritize E9-06 to Sprint 10 and replace with extra polish/testing
- Ensure admin role is seeded in the database (Flyway data migration or application startup seed)
- Android APK signing setup must be done before distributing the private beta

---

### Sprint 10 — 2026-11-10 to 2026-11-20

**Sprint Goal:** The platform is production-hardened. Tests cover critical paths. Security is reviewed. Production is deployed with SSL. Beta launch happens at sprint end.

**Committed Stories:**

| Story ID | Description | Points |
|---|---|---|
| E10-01 | Backend: Global exception handler | 3 |
| E10-02 | Backend: Input validation | 2 |
| E10-03 | Backend: Rate limiting | 3 |
| E10-04 | Backend: Unit tests — service layer | 5 |
| E10-05 | Backend: Integration tests (TestContainers) | 5 |
| E10-06 | Web: Error boundaries + empty states | 3 |
| E10-07 | Web: Loading skeletons | 2 |
| E10-08 | Web: Dark mode | 3 |
| E10-09 | Android: Offline handling + error states | 3 |
| E10-10 | Android: App icon + splash screen | 2 |
| E10-11 | Performance audit + Redis cache tuning | 3 |
| E10-12 | Security review (OWASP checklist) | 3 |
| E10-13 | Production deployment + Nginx SSL | 3 |
| E10-14 | Swagger documentation completion | 2 |
| E10-15 | Beta testing + bug fixes | 8 |

**Sprint Total: 50 points**

> **Note:** Sprint 10 is the heaviest sprint intentionally. By this point the team has established velocity, there are no new architectural unknowns, and the work is largely additive (tests, polish) rather than net-new feature development. If velocity cannot support 50 points, deprioritize E10-08 (dark mode, 3pts) and E10-07 (loading skeletons, 2pts) as nice-to-haves.

**Deliverables:**
- Production environment is live with SSL/TLS
- All critical paths have unit and integration test coverage
- Security review completed, findings addressed or documented
- Dark mode works on web app
- Android app has correct icon and splash screen
- Rate limiting prevents API abuse
- **Release: `v1.0.0` tag on `main`** — MVP Launch
- Release notes published on GitHub Releases
- Beta users receive invitation to join Boondi

**Dependencies / Risks:**
- Production server must be provisioned before Sprint 10 (VPS, domain, DNS) — do this in Sprint 9
- SSL certificate (Let's Encrypt) requires a public domain to be configured
- E10-15 (8 pts — beta testing/bug fixes) is a time-boxed buffer. Any P0/P1 bugs found during the sprint consume this allocation first

---

## 7. Milestone & Release Map

### Milestone Table

| Milestone | Sprint | Target Date | Version | Description |
|---|---|---|---|---|
| Alpha — Backend API | End Sprint 2 | 2026-08-01 | `v0.1.0` | All core backend APIs working and documented via Swagger. Testable with Postman. |
| Beta — Web App | End Sprint 6 | 2026-09-25 | `v0.2.0` | Web app feature-complete for MVP. Deploy to staging for internal web-only private beta. |
| Beta — Android App | End Sprint 9 | 2026-11-07 | `v0.3.0` | Android app feature-complete. APK distributed via Firebase App Distribution for Android beta testers. |
| MVP Launch | End Sprint 10 | 2026-11-20 | `v1.0.0` | Full platform launch on production. Web + Android + Backend. Private beta opens to all invited users. |

---

### Visual Timeline (ASCII Gantt Chart)

```
BOONDI — SPRINT & MILESTONE TIMELINE
2026-07-07 to 2026-11-20

WEEK      W1    W2    W3    W4    W5    W6    W7    W8    W9    W10   W11   W12   W13   W14   W15   W16   W17   W18   W19   W20
DATE      07/07 07/14 07/21 07/28 08/04 08/11 08/18 08/25 09/01 09/08 09/15 09/22 09/29 10/06 10/13 10/20 10/27 11/03 11/10 11/17

SPRINT    [==S1==]    [==S2==]    [==S3==]    [==S4==]    [==S5==]    [==S6==]    [==S7==]    [==S8==]    [==S9==]    [=S10=]

Epic 1    [XXXXXX]   [X      ]                                                    [X     ]
Epic 2    [XXXXXX]   [XXXXXXX]   [XXXXXXX]                                        [XXXXXXX]
Epic 3               [XXXXXXX]               [XXXXXXX]                            [       ]   [XXXXXXX]
Epic 4                           [XXXXXXX]   [XXXXXXX]   [X     ]                             [XXXXXXX]
Epic 5                                       [XXXXXXX]   [XXXXXXX]   [XXXXX]                  [XXXXXXX]   [X     ]
Epic 6                                                   [XXXXXXX]   [XXXXXXX]   [XXXXXXX]               [XXXXXXX]   [X     ]
Epic 7                                                               [XXXXXXX]   [XXXXXXX]               [       ]   [XXXXXXX]
Epic 8                                                               [XXXXXXX]   [XXXXXXX]                           [XXXXXXX]
Epic 9                                                                                                    [XXXXXXX]   [XXXXX ]
Epic 10                                                                                                               [XXXXXXX]

MILESTONES                        ^v0.1.0                             ^v0.2.0                 ^v0.3.0                  ^v1.0.0
                                  Alpha                               Beta-Web                Beta-Android             MVP
                                  2026-08-01                          2026-09-25              2026-11-07               2026-11-20
```

**Legend:** `[XXXXX]` = Active development in sprint | `^` = Release milestone

---

## 8. Risk Register

| ID | Risk | Probability | Impact | Mitigation Strategy |
|---|---|---|---|---|
| R01 | **Object storage integration complexity** — S3/MinIO setup, presigned URLs, CORS config takes longer than estimated | Medium | High | Provision MinIO via Docker Compose in Sprint 1 (same sprint as setup). Use Testcontainers for S3 in tests. Spike object storage integration on Day 1 of Sprint 2 before committing to avatar upload story. |
| R02 | **Email service setup delays** — SMTP provider account approval, SendGrid API keys, or DNS records (SPF/DKIM) blocking email delivery | Medium | Medium | Use Mailtrap or MailHog for development to fully unblock email stories. Provision production email service (SendGrid/SES) at Sprint 3 start — do not wait until production launch. |
| R03 | **Android Compose learning curve** — Jetpack Compose + Hilt + Navigation + Retrofit wiring is unfamiliar, causing Sprint 7/8 underdelivery | Medium | High | Spike Android project setup in Sprint 6 (1 day timebox, not a committed story). Review Compose/Hilt sample apps before Sprint 7. Accept that Android sprints (7–9) may run 10–15% slower and account for this in the plan. |
| R04 | **Timeline performance at scale** — Home timeline query joining `posts` + `follows` tables degrades beyond 1,000 follows | Low | High | Add database indexes in Sprint 4 before timeline goes to staging. Use Redis fan-out cache (E5-05) to avoid DB queries on hot paths. Use `EXPLAIN ANALYZE` to validate query plans before Sprint 5 ends. |
| R05 | **JWT refresh race condition** — Mobile/web client sends multiple requests simultaneously when the access token expires, all triggering concurrent refresh calls | Medium | Medium | Implement mutex/lock in the Android refresh interceptor (OkHttp Authenticator with `synchronized`). On the backend, make refresh idempotent: same refresh token returns the same access token within a short window (5 seconds) before rotating. |
| R06 | **Scope creep** — Stakeholders or the developer themselves add features (DMs, polls, video) during MVP development | High | High | The MVP scope is locked in this document. Any new feature request goes to a written backlog (Post-MVP Roadmap, Section 10). No unplanned feature starts mid-sprint without removing an equivalent story. |
| R07 | **Solo developer burnout** — Five months of sustained development across three platforms with no vacation buffer | Medium | High | Build in "light" sprints (Sprints 2, 4, 6 are intentionally under 30 points). Take one day off per sprint for rest. Declare scope reduction rather than working nights. Sprint 10 bugs are acceptable to carry to v1.0.1. |
| R08 | **Security vulnerabilities discovered late** — IDOR, JWT leakage, or injection bugs found in Sprint 10 security review require architectural fixes | Low | Critical | Do not defer all security to Sprint 10. In every sprint: validate UUIDs (not sequential IDs) are used, user actions are authorized (not just authenticated), and no sensitive data leaks in API responses. Sprint 10 security review is a final check, not the first check. |
| R09 | **Redis cache invalidation bugs** — Stale home timeline data shown after a followee posts; cache eviction key mismatch | Medium | Medium | Write integration tests for cache invalidation (E10-05). Define cache key naming convention in Sprint 5 (`timeline:home:{userId}`). Log cache hit/miss rates in staging to verify behavior before v0.2.0 release. |
| R10 | **Sprint velocity underestimation** — Actual velocity proves to be 20–24 pts/sprint (not 28–32), putting the Nov 2026 launch at risk | Medium | High | Monitor actuals after Sprint 1 and Sprint 2. By end of Sprint 3, if running behind by >10%, scope-reduce Epic 10 (defer dark mode, loading skeletons, admin panel) to v1.1.0. Never sacrifice testing (E10-04, E10-05) or security (E10-12). |
| R11 | **PostgreSQL full-text search performance** — tsvector search on `posts` is slow without correct index | Low | Medium | Add GIN index on `posts.search_vector` in the Flyway migration when E8-02 is implemented. Validate with `EXPLAIN ANALYZE` before marking story Done. |
| R12 | **Android APK signing not set up before beta** — Release build fails or is unsigned when beta testers need the APK | Low | Medium | Set up Android keystore and signing config in Sprint 7 (same sprint as Android project init). Store keystore in GitHub Secrets, never in the repository. |

---

## 9. Velocity & Estimation Notes

### 9.1 Assumed Velocity

| Team Composition | Target Velocity | Range |
|---|---|---|
| Solo developer | 28 pts/sprint | 22–32 pts |
| 2 developers (1 backend/web + 1 Android) | 45 pts/sprint | 38–52 pts |
| 3 developers | 60 pts/sprint | 52–68 pts |

**Total backlog: 292 points**
**Solo developer:** 292 / 28 avg = ~10.4 sprints (fits within 10 sprints with sprint-level optimization)
**2-person team:** 292 / 45 avg = ~6.5 sprints (could complete in 7 sprints, 2 months early)

### 9.2 Story Point Reference Guide

Use this calibration table to keep estimates consistent across sprints. If in doubt, estimate against these concrete benchmarks.

| Points | Estimated Effort | Example Story |
|---|---|---|
| **1** | 1–2 hours | Add a new field to an existing API response; mark notifications as read endpoint |
| **2** | half day (3–4 hours) | Simple CRUD endpoint with validation; a basic web screen with static data |
| **3** | 1 day | New API with business logic; a web screen integrating a real API; an Android screen with ViewModel |
| **5** | 1.5–2 days | Complex feature with multiple components (JWT filter chain; S3 upload with presigned URLs; tsvector search with GIN index) |
| **8** | 3–4 days | A full feature that spans multiple layers and has uncertainty (beta testing + bug fix buffer) |
| **13** | 1 week | Indicates the story is too large — break it down. No story should be 13 points at sprint start |

> **Rule:** If a story cannot be described in one sentence and completed within a sprint, decompose it. 13-point stories signal missing decomposition, not high effort.

### 9.3 Buffer Allocation

Each sprint maintains an implicit **20% buffer** (roughly 5–7 points of unscheduled capacity per sprint):

- **Bug fixes from previous sprint:** Production bugs or staging issues discovered after the sprint closes
- **Integration friction:** Time lost to environment issues, merge conflicts, dependency debugging
- **Refinement:** Updating Flyway migrations, refactoring for code quality without functional change
- **Unplanned meetings / admin:** Code reviews on archived PRs, dependency updates (security patches), documentation

**Never fully commit to theoretical maximum velocity.** A sprint with 32-point capacity should commit ~27–28 points. The buffer is consumed by real work — it is not free time.

### 9.4 Schedule Adjustment Protocol

If the team is running behind, apply scope reduction in this priority order:

1. **Defer polish stories first:** Dark mode (3 pts), loading skeletons (2 pts), app icon improvements
2. **Defer admin panel complexity:** Reduce admin panel to a simple list view, defer advanced moderation features to v1.1
3. **Defer Android search:** Release Android without search (web app has search). Add in v1.0.1
4. **Reduce test coverage target:** Focus unit tests on auth + post + timeline services only; defer broader coverage to post-MVP
5. **Never defer:** Authentication security, JWT handling, production SSL, input validation, core CRUD for posts/feed

> **Hard rule:** If Sprint 7 ends and the web app is not functional (feature-complete), stop new development and spend Sprint 8 completing and stabilizing the web app before starting Android. A working web product ships on time. An incomplete web + incomplete Android ships nothing.

### 9.5 Technical Debt Tracking

Technical debt is tracked explicitly rather than ignored. Use a `tech-debt` label in GitHub Issues. Each sprint, a **maximum of 1 story slot (3–5 points)** may be allocated to debt reduction without formal approval.

Categories of debt to track:
- **Missing tests:** Stories marked Done without tests (label: `needs-tests`)
- **Hardcoded values:** Magic strings/numbers that should be configuration
- **Inconsistent error handling:** Endpoints that return 500 instead of structured error responses
- **Missing indexes:** Database queries that run without an index (found via slow query log)
- **TODO comments in code:** Each TODO is a GitHub issue within one sprint of creation

At the Sprint Retrospective, review the debt backlog. If more than 10 open debt items accumulate, dedicate a half-sprint to debt before the next major feature epic begins.

---

## 10. Post-MVP Roadmap

After `v1.0.0` ships and private beta users are onboarded, the following roadmap guides the next three versions. All dates are estimates based on continued 2-week sprint cadence.

---

### v1.1.0 — Performance & Stability

**Target:** Sprints 11–12 (2026-12-01 to 2026-12-25)
**Theme:** Make the platform fast, observable, and reliable before growing the user base.

| Feature | Description |
|---|---|
| Redis pipeline optimizations | Batch Redis commands using pipelines to reduce round-trips. Audit all cache usage from Sprint 5. |
| CDN integration | Serve static assets (web app) and user media (avatars, post images) through a CDN (Cloudflare or AWS CloudFront). Reduces latency and origin load. |
| Monitoring + alerting | Deploy Prometheus metrics endpoint on the Spring Boot backend. Set up Grafana dashboards for: API response times, error rates, database connection pool, Redis hit rate. Configure PagerDuty or email alerts on P99 > 2s. |
| Android push notifications | Integrate Firebase Cloud Messaging (FCM). Send push notifications for: new like, new follower, new reply, mention. Requires a notification dispatch service on the backend. |
| Bug fixes from beta feedback | Dedicated allocation (8–10 points) for bugs surfaced during the v1.0.0 private beta. Prioritized by severity and frequency. |
| Database connection pooling | Tune HikariCP pool settings based on observed load. Add read replicas if write/read ratio justifies it. |

**Target: ~55–65 points over 2 sprints**

---

### v1.2.0 — Social Enhancements

**Target:** Sprints 13–15 (2027-01-06 to 2027-02-13)
**Theme:** Deepen the social experience and content discovery capabilities.

| Feature | Description |
|---|---|
| Polls | Users can attach a poll (2–4 options, 1–7 day duration) to a post. Votes are stored and results shown. |
| Post scheduling | Users can write a post and schedule it to publish at a future time. Implemented via a `scheduled_at` column + a Spring `@Scheduled` job or Quartz scheduler. |
| Improved search relevance | Rank search results by recency + engagement (likes, reposts). Implement ts_rank for PostgreSQL full-text results. |
| Trending algorithm improvements | Replace simple count-based trending with a time-decayed score (similar to Hacker News ranking algorithm). |
| Lists / Collections | Users can create named lists of accounts and view a timeline filtered to those accounts. |
| Web: post analytics (own posts) | Show view count, like count, repost count, and reach for a user's own posts. |
| Android: image viewer | Full-screen image viewer with pinch-to-zoom when tapping post images. |

**Target: ~85–100 points over 3 sprints**

---

### v2.0.0 — Direct Messaging & Communities

**Target:** Sprints 16–22 (2027-02-17 to 2027-05-21)
**Theme:** Expand Boondi from a public social platform to a community and private communication platform.

This is the largest version increment and introduces real-time infrastructure.

| Feature | Description |
|---|---|
| Direct Messages (DM) | 1:1 private messaging between users using WebSocket (Spring WebSocket + STOMP). Messages are encrypted at rest. Message history is paginated. |
| Group DMs | Up to 10 participants per group DM. |
| Communities / Groups | Users can create named communities with a description, avatar, and moderation rules. Posts can be submitted to communities. Community-specific feed. |
| Community moderation | Community owners and moderators can remove posts, ban members, and set community rules. |
| Stories (24-hour posts) | Users can post Stories that disappear after 24 hours. Stored separately from posts. Stories are visible on the profile avatar ring UI. |
| Video upload support | Users can upload short videos (up to 60 seconds, max 100MB). Transcode via FFmpeg or a cloud transcoding service (AWS MediaConvert). Serve via CDN. |
| Verified accounts | Admin-granted verification badge (blue checkmark) for notable accounts. |
| Improved notifications | Real-time notifications via WebSocket (no polling). Group notification summaries (e.g., "5 people liked your post"). |

**Target: ~180–220 points over 7 sprints**

---

### Version Roadmap Summary

| Version | Sprints | Estimated Date | Theme |
|---|---|---|---|
| `v1.0.0` | 1–10 | 2026-11-20 | MVP Launch |
| `v1.1.0` | 11–12 | 2026-12-25 | Performance & Stability |
| `v1.2.0` | 13–15 | 2027-02-13 | Social Enhancements |
| `v2.0.0` | 16–22 | 2027-05-21 | DMs & Communities |

---

*This document is a living artifact. Update sprint plans at each retrospective. Update the risk register when new risks emerge. Update velocity benchmarks after Sprint 2 when real data is available.*

*Last updated: 2026-07-02*
