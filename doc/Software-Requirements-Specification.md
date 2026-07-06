# Software Requirements Specification (SRS)


# Table of Contents

1. Introduction
2. Overall Description
3. Functional Requirements
4. External Interface Requirements
5. Non-Functional Requirements
6. Data Requirements
7. Business Rules
8. User Stories
9. Use Cases
10. Acceptance Criteria
11. Future Enhancements
12. Appendix

---

# 1. Introduction

## 1.1 Purpose

This Software Requirements Specification (SRS) defines the functional and non-functional requirements for **Boondi**, a private social networking platform consisting of a Web application, Android application, and RESTful backend services.

The purpose of this document is to provide a single source of truth for developers, designers, testers, and future contributors. It describes system behavior, interfaces, business rules, quality attributes, and constraints to ensure consistent implementation.

This document follows the principles of IEEE 29148 Software Requirements Specification.

---

## 1.2 Scope

Boondi is a modern social networking application inspired by platforms such as Twitter/X, designed initially for private communities and friend groups.

The system enables users to:

- Register and authenticate securely
- Create personal profiles
- Publish text and image posts
- Like, reply to, and repost content
- Follow other users
- View personalized timelines
- Receive notifications
- Search users and content
- Manage account settings

The platform consists of:

- Android Application
- Responsive Web Application
- REST API Backend
- PostgreSQL Database
- Object Storage for media
- Redis Cache
- Notification Service

Future versions may introduce:

- Direct Messaging
- Communities
- Stories
- Polls
- Video Upload
- AI-powered recommendations
- Live Streaming

---

## 1.3 Intended Audience

This document is intended for:

- Software Developers
- Backend Engineers
- Android Developers
- Frontend Developers
- QA Engineers
- UI/UX Designers
- DevOps Engineers
- Project Managers

---

## 1.4 Definitions

| Term | Definition |
|------|------------|
| API | Application Programming Interface |
| JWT | JSON Web Token |
| REST | Representational State Transfer |
| UUID | Universally Unique Identifier |
| DTO | Data Transfer Object |
| RBAC | Role-Based Access Control |
| CDN | Content Delivery Network |
| Timeline | Collection of posts shown to a user |
| Feed | Ordered list of posts displayed to users |

---

## 1.5 References

- IEEE 29148 Systems and Software Engineering Requirements
- RFC 7519 – JSON Web Token (JWT)
- REST Architectural Style
- OpenAPI Specification 3.1
- OWASP Application Security Verification Standard

---

## 1.6 Document Overview

This document is organized into sections covering:

- System overview
- Functional requirements
- External interfaces
- Performance requirements
- Security requirements
- Database requirements
- User interactions
- Acceptance criteria

---

# 2. Overall Description

## 2.1 Product Perspective

Boondi is a client-server application.

High-level architecture:

```
Android App
        │
        │
Web Application
        │
        ▼
REST API Gateway
        │
Spring Boot Backend
        │
 ┌───────────────┬──────────────┐
 │               │              │
PostgreSQL     Redis      Object Storage
```

The backend exposes REST APIs consumed by both Android and Web applications.

Media assets are stored separately from relational data.

Authentication is token-based using JWT.

---

## 2.2 Product Goals

The primary goals are:

- Fast user experience
- Secure authentication
- High availability
- Scalable architecture
- Responsive interface
- Cross-platform compatibility
- Maintainable codebase

---

## 2.3 Product Functions

The system shall provide the following capabilities:

### Authentication

- User registration
- Login
- Logout
- Password reset
- Email verification
- Session management

---

### User Profiles

Users shall be able to:

- Create profile
- Update profile
- Upload profile picture
- Upload banner image
- Edit biography
- View followers
- View following

---

### Content Management

Users shall be able to:

- Create posts
- Edit posts
- Delete posts
- View posts
- Quote posts
- Repost posts

Posts shall support:

- Plain text
- Images
- Multiple media attachments (future)

---

### Social Interaction

Users shall be able to:

- Follow users
- Unfollow users
- Like posts
- Remove likes
- Reply to posts
- Bookmark posts

---

### Timeline

The system shall generate:

- Home Timeline
- Following Timeline
- User Timeline
- Trending Timeline

---

### Notifications

The system shall notify users when:

- Someone follows them
- Someone likes their post
- Someone replies
- Someone mentions them
- Someone reposts their content

---

### Search

Users shall search:

- Users
- Posts
- Hashtags

Future versions may support semantic and AI-assisted search.

---

### Administration

Administrators shall:

- Suspend users
- Delete content
- Review reports
- Manage announcements
- Moderate the platform

---

## 2.4 User Classes

### Guest

Capabilities:

- View landing page
- Register
- Login

Restrictions:

- Cannot create content
- Cannot interact with posts

---

### Registered User

Capabilities:

- Full social interaction
- Profile management
- Notifications
- Search
- Posting

Restrictions:

- Cannot access administrative features

---

### Administrator

Capabilities:

- User management
- Content moderation
- Reports
- Announcements
- Platform maintenance

---

## 2.5 Operating Environment

Backend

- Java 21
- Spring Boot
- Spring Security
- PostgreSQL
- Redis
- Docker

Android

- Android 10+
- Kotlin
- Jetpack Compose

Web

- React
- TypeScript
- Vite
- Tailwind CSS

Infrastructure

- Linux Server
- Nginx
- HTTPS
- Docker Compose (development)
- Kubernetes (future)

---

## 2.6 Design Constraints

The system shall:

- Use REST architecture
- Use PostgreSQL as the primary database
- Use UUIDs as primary identifiers
- Support stateless authentication
- Store media separately from relational data
- Follow Clean Architecture principles
- Use Git for version control
- Expose OpenAPI documentation

---

## 2.7 Assumptions

It is assumed that:

- Users have internet connectivity.
- Email delivery services are available.
- Modern browsers support the web application.
- Android devices support Android 10 or later.
- HTTPS certificates are properly configured.

---

## 2.8 Dependencies

The application depends on:

- PostgreSQL
- Redis
- Object Storage
- Email Service
- DNS
- HTTPS Certificates
- Internet Connectivity

---

## 2.9 System Constraints

The MVP shall not include:

- Direct messaging
- Video uploads
- Stories
- Audio calls
- Live streaming
- AI-generated content
- Public APIs
- Third-party integrations

These features are planned for future releases.

---

# 3. Functional Requirements

This section specifies the functional requirements of the Boondi platform. Each requirement is assigned a unique identifier (FR-XXX) and describes a discrete, testable capability the system must provide.

---

## 3.1 Authentication & Authorization

| ID | Requirement |
|----|-------------|
| FR-001 | The system shall allow any visitor to register a new account by providing a unique username, a valid email address, and a password. |
| FR-002 | The system shall validate that the email address provided during registration is syntactically correct and not already associated with an existing account. |
| FR-003 | The system shall validate that the username is unique, between 3 and 30 characters, and contains only alphanumeric characters and underscores. |
| FR-004 | The system shall hash all passwords using BCrypt with a minimum cost factor of 12 before storing them in the database. |
| FR-005 | The system shall issue a short-lived JWT access token (15-minute expiry) and a long-lived refresh token (7-day expiry) upon successful login. |
| FR-006 | The system shall allow authenticated users to obtain a new access token by presenting a valid, unexpired refresh token to `POST /auth/refresh`. |
| FR-007 | The system shall invalidate the refresh token upon logout (`POST /auth/logout`), preventing further token renewal. |
| FR-008 | The system shall reject requests bearing expired or malformed JWT access tokens with an HTTP 401 Unauthorized response. |
| FR-009 | The system shall enforce Role-Based Access Control (RBAC) with the roles Guest, Registered User, and Administrator. |
| FR-010 | The system shall prevent Guest users from accessing any endpoint other than registration, login, and publicly viewable content. |
| FR-011 | The system shall prevent Registered Users from accessing Administrator-only endpoints, returning HTTP 403 Forbidden. |

---

## 3.2 User Profile Management

| ID | Requirement |
|----|-------------|
| FR-012 | The system shall automatically create a default profile for a user upon successful registration. |
| FR-013 | The system shall allow an authenticated user to retrieve their own profile via `GET /users/me` and any other user's public profile via `GET /users/{id}`. |
| FR-014 | The system shall allow an authenticated user to update their display name, biography (maximum 160 characters), website URL, and location via `PUT /users/me`. |
| FR-015 | The system shall allow an authenticated user to upload a profile picture; the system shall accept JPEG and PNG formats up to 5 MB in size. |
| FR-016 | The system shall allow an authenticated user to upload a profile banner image; the system shall accept JPEG and PNG formats up to 10 MB in size. |
| FR-017 | The system shall store uploaded profile images in object storage and serve them via a publicly accessible URL. |
| FR-018 | The system shall display, on a user's profile page, their total post count, follower count, and following count. |
| FR-019 | The system shall allow a user to view the list of users who follow them and the list of users they follow. |

---

## 3.3 Post Management

| ID | Requirement |
|----|-------------|
| FR-020 | The system shall allow an authenticated user to create a new post via `POST /posts` containing text, images, or both. |
| FR-021 | The system shall enforce a maximum post text length of 500 characters and reject posts that exceed this limit with an HTTP 400 response. |
| FR-022 | The system shall allow a post to contain up to 4 image attachments; each image shall be JPEG or PNG and shall not exceed 10 MB. |
| FR-023 | The system shall extract and index hashtags (tokens beginning with `#`) from post text upon creation. |
| FR-024 | The system shall extract and index user mentions (tokens beginning with `@`) from post text upon creation. |
| FR-025 | The system shall allow an authenticated user to edit their own post's text content via `PUT /posts/{id}` within 15 minutes of creation. |
| FR-026 | The system shall mark edited posts with an "edited" indicator and record the timestamp of the last edit. |
| FR-027 | The system shall allow an authenticated user to delete their own post via `DELETE /posts/{id}`; the system shall also delete associated media from object storage. |
| FR-028 | The system shall allow an Administrator to delete any post regardless of ownership. |
| FR-029 | The system shall allow an authenticated user to create a reply post linked to an existing post via `POST /posts/{id}/reply`. |
| FR-030 | The system shall allow an authenticated user to repost (repost without comment) an existing post, creating a reference entry in their timeline. |
| FR-031 | The system shall allow an authenticated user to create a quote post, which includes a reference to the original post and adds new text commentary. |
| FR-032 | The system shall return post data in paginated responses with a default page size of 20 and a maximum page size of 50. |

---

## 3.4 Social Interactions

| ID | Requirement |
|----|-------------|
| FR-033 | The system shall allow an authenticated user to like a post via `POST /posts/{id}/like`, incrementing the post's like count by 1. |
| FR-034 | The system shall prevent a user from liking the same post more than once; a duplicate like request shall return HTTP 409 Conflict. |
| FR-035 | The system shall allow an authenticated user to remove their like from a post via `DELETE /posts/{id}/like`, decrementing the like count by 1. |
| FR-036 | The system shall allow an authenticated user to follow another user via `POST /users/{id}/follow`. |
| FR-037 | The system shall prevent a user from following themselves; such a request shall return HTTP 400 Bad Request. |
| FR-038 | The system shall prevent duplicate follow relationships; a second follow request against the same target shall return HTTP 409 Conflict. |
| FR-039 | The system shall allow an authenticated user to unfollow a user they currently follow via `DELETE /users/{id}/follow`. |
| FR-040 | The system shall allow an authenticated user to bookmark a post via `POST /posts/{id}/bookmark`, saving it to their private bookmark collection. |
| FR-041 | The system shall allow an authenticated user to remove a bookmark via `DELETE /posts/{id}/bookmark`. |
| FR-042 | The system shall allow an authenticated user to retrieve their bookmarked posts, ordered by bookmark creation date descending, via `GET /users/me/bookmarks`. |
| FR-043 | Bookmarks shall be private and visible only to the owning user. |

---

## 3.5 Timeline & Feed

| ID | Requirement |
|----|-------------|
| FR-044 | The system shall provide a Home Timeline (`GET /timeline/home`) that aggregates posts from all users the authenticated user follows, ordered by creation timestamp descending. |
| FR-045 | The system shall provide a Latest Timeline (`GET /timeline/latest`) that returns all recent posts from all users, ordered by creation timestamp descending. |
| FR-046 | The system shall provide a User Timeline (`GET /users/{id}/posts`) that returns all posts created by a specific user, ordered by creation timestamp descending. |
| FR-047 | The system shall provide a Trending Timeline (`GET /timeline/trending`) that surfaces posts and hashtags ranked by engagement (likes + replies + reposts) within a configurable rolling time window (default: 24 hours). |
| FR-048 | The system shall support cursor-based pagination for all timeline endpoints to ensure consistent ordering under high write frequency. |
| FR-049 | The system shall cache timeline responses in Redis with a TTL of 60 seconds to reduce database load. |
| FR-050 | The system shall include reposts and quote posts in the Home Timeline of followers of the reposting user. |

---

## 3.6 Notifications

| ID | Requirement |
|----|-------------|
| FR-051 | The system shall generate a notification for a user when another user follows them. |
| FR-052 | The system shall generate a notification for a post author when another user likes their post. |
| FR-053 | The system shall generate a notification for a post author when another user replies to their post. |
| FR-054 | The system shall generate a notification for a user when another user mentions them (using `@username`) in a post or reply. |
| FR-055 | The system shall generate a notification for a post author when another user reposts or quote-posts their content. |
| FR-056 | The system shall allow an authenticated user to retrieve their notifications, ordered by creation timestamp descending, via `GET /notifications`. |
| FR-057 | The system shall support marking individual notifications as read and marking all notifications as read in bulk. |
| FR-058 | The system shall expose an unread notification count for display in client application UI badges. |
| FR-059 | The system shall not generate a notification when a user interacts with their own content. |

---

## 3.7 Search

| ID | Requirement |
|----|-------------|
| FR-060 | The system shall allow any authenticated user to search for other users by username or display name via `GET /search/users?q={query}`. |
| FR-061 | The system shall allow any authenticated user to search for posts containing specific text via `GET /search/posts?q={query}`. |
| FR-062 | The system shall allow any authenticated user to search by hashtag, returning all posts containing the specified hashtag. |
| FR-063 | The system shall return search results in paginated form with a default page size of 20. |
| FR-064 | The system shall perform case-insensitive matching for all search queries. |
| FR-065 | User search results shall include the user's display name, username, profile picture URL, and follower count. |
| FR-066 | Post search results shall include the post text, author information, creation timestamp, and engagement counts. |

---

## 3.8 Administration

| ID | Requirement |
|----|-------------|
| FR-067 | The system shall provide an Administrator role with elevated privileges, distinguishable from Registered User accounts. |
| FR-068 | The system shall allow an Administrator to suspend a user account, preventing the user from logging in or creating content. |
| FR-069 | The system shall allow an Administrator to reinstate a previously suspended user account. |
| FR-070 | The system shall allow an Administrator to delete any post from any user. |
| FR-071 | The system shall allow an Administrator to view a list of all registered users, including their account status and registration date. |
| FR-072 | The system shall allow an Administrator to post platform-wide announcements visible to all users on the Latest Timeline or a dedicated announcement feed. |
| FR-073 | The system shall provide an audit log recording all Administrator actions (user suspension, content deletion, announcements), including the acting Administrator's ID and timestamp. |
| FR-074 | The system shall allow users to report a post or account; reported items shall be queued for Administrator review. |
| FR-075 | The system shall allow an Administrator to dismiss or act upon user-submitted reports. |

---

# 4. External Interface Requirements

---

## 4.1 User Interfaces

### 4.1.1 Android Application

- The Android application shall target Android 10 (API Level 29) and above.
- The UI shall be implemented using Jetpack Compose declarative components.
- The application shall follow Material Design 3 guidelines for typography, color, spacing, and component behavior.
- The application shall support both light and dark themes, following the system preference by default.
- Navigation shall follow the single-activity, multiple-composable pattern with a bottom navigation bar providing access to Home, Search, Notifications, and Profile.
- The application shall display loading skeletons rather than spinner overlays for timeline and feed content.
- All interactive elements shall have a minimum touch target size of 48 × 48 dp.
- The application shall handle offline states gracefully, displaying cached content where available and showing appropriate error messaging when the network is unavailable.

### 4.1.2 Web Application

- The web application shall be implemented as a Single Page Application (SPA) using React 18 and TypeScript.
- The application shall be bundled with Vite and styled using Tailwind CSS utility classes.
- The layout shall be responsive and support viewport widths from 320 px (mobile) to 1440 px (desktop) without horizontal scrolling.
- The application shall use React Query for server state management, caching, and background refetching.
- The web application shall support the latest two stable releases of Chrome, Firefox, Safari, and Edge.
- Navigation shall be handled client-side using React Router without full-page reloads.
- The application shall display meaningful error states and empty states for all data-driven views.
- Accessibility shall conform to WCAG 2.1 Level AA standards, including keyboard navigability and screen reader support.

---

## 4.2 Hardware Interfaces

- The system has no direct hardware interface requirements for clients beyond standard network connectivity.
- The Android application shall request permissions for camera and photo library access only when the user explicitly initiates media upload.
- The server infrastructure shall run on Linux-based commodity hardware or cloud compute instances; no proprietary hardware is required.
- The system shall not rely on device-specific hardware features (e.g., biometric sensors) in the MVP.

---

## 4.3 Software Interfaces

### 4.3.1 REST API

- The backend shall expose a RESTful API documented via OpenAPI Specification 3.1, served at `/api-docs` in development and staging environments.
- All API requests and responses shall use `application/json` as the Content-Type, except for multipart file uploads which shall use `multipart/form-data`.
- API versioning shall be indicated via the URL path prefix `/api/v1/`.
- All API responses shall conform to a consistent envelope schema containing `status`, `data`, and `error` fields.

### 4.3.2 PostgreSQL Database

- The backend shall connect to PostgreSQL 15 or later as the primary relational data store.
- The connection pool shall be managed via HikariCP, configured for a maximum of 20 concurrent connections per application instance.
- Database schema migrations shall be managed using Flyway, with migration scripts stored in version control.

### 4.3.3 Redis Cache

- The backend shall connect to Redis 7 or later for caching timeline responses, session data, and rate-limit counters.
- The Redis client shall be configured with connection pooling and automatic reconnection on failure.
- Cached entries shall carry explicit TTL values; no entry shall be stored without a TTL.

### 4.3.4 Object Storage

- Media files (profile pictures, banner images, and post images) shall be stored in an S3-compatible object storage service.
- The backend shall generate pre-signed URLs with a limited validity window for secure client-direct media uploads.
- Stored objects shall be served over HTTPS via a CDN-backed public URL.

### 4.3.5 Email Service

- The system shall integrate with an SMTP-compatible email service for transactional emails (registration confirmation, password reset).
- Email templates shall be rendered server-side and sent asynchronously to avoid blocking API response times.

---

## 4.4 Communication Interfaces

### 4.4.1 HTTP / HTTPS

- All client-server communication shall occur exclusively over HTTPS using TLS 1.2 or higher.
- HTTP requests shall be redirected to HTTPS with a permanent 301 redirect.
- HTTP Strict Transport Security (HSTS) headers shall be set on all HTTPS responses.
- The API shall support HTTP/1.1; HTTP/2 support is desirable and shall be enabled at the Nginx reverse-proxy layer where available.

### 4.4.2 WebSocket (Future)

- Real-time notification delivery via WebSocket is planned for a future release.
- In the MVP, notifications shall be delivered by client-initiated polling.
- The architecture shall be designed to accommodate the addition of a WebSocket endpoint without structural changes to the existing REST API.

---

# 5. Non-Functional Requirements

---

## 5.1 Performance Requirements

| ID | Requirement |
|----|-------------|
| NFR-P01 | The REST API shall return a successful response within 300 ms on average, measured at the server under normal load (up to 100 concurrent users). |
| NFR-P02 | Timeline endpoints shall return the first page of results within 2 seconds under normal load, inclusive of cache lookup. |
| NFR-P03 | Media upload pre-signed URL generation shall complete within 500 ms. |
| NFR-P04 | Search query endpoints shall return results within 1 second for queries against up to 1 million indexed records. |
| NFR-P05 | The Android and web applications shall reach interactive state (Time to Interactive) within 3 seconds on a 4G mobile connection. |
| NFR-P06 | The API shall sustain a throughput of at least 200 requests per second on a single application instance without exceeding the 300 ms average response time threshold. |

---

## 5.2 Security Requirements

| ID | Requirement |
|----|-------------|
| NFR-S01 | All passwords shall be hashed using BCrypt with a minimum work factor of 12; plaintext passwords shall never be logged or stored. |
| NFR-S02 | JWT access tokens shall expire after 15 minutes; refresh tokens shall expire after 7 days. |
| NFR-S03 | The system shall implement token revocation for refresh tokens at logout, using a denylist stored in Redis. |
| NFR-S04 | All API inputs shall be validated and sanitized server-side before processing; client-side validation is supplementary only. |
| NFR-S05 | The system shall enforce rate limiting on authentication endpoints: a maximum of 10 failed login attempts per IP address per 15-minute window, after which the IP shall be temporarily blocked. |
| NFR-S06 | The system shall set `Secure`, `HttpOnly`, and `SameSite=Strict` attributes on any session cookies. |
| NFR-S07 | The system shall include CORS configuration restricting API access to the registered web application origin and rejecting all other cross-origin requests in production. |
| NFR-S08 | The system shall not expose internal stack traces, database error messages, or system paths in API error responses returned to clients. |
| NFR-S09 | All media URLs served from object storage shall use HTTPS; HTTP object storage URLs are not permitted. |
| NFR-S10 | Administrator actions shall require re-authentication or elevated-privilege tokens separate from standard user sessions. |
| NFR-S11 | The system shall log all authentication events (login, logout, failed attempts, token refresh) with timestamp, IP address, and user identifier for audit purposes. |

---

## 5.3 Availability & Reliability

| ID | Requirement |
|----|-------------|
| NFR-A01 | The system shall maintain a minimum uptime of 99.5% measured over any rolling 30-day window, excluding scheduled maintenance windows. |
| NFR-A02 | The API error rate (5xx responses as a percentage of total requests) shall not exceed 1% under normal operating conditions. |
| NFR-A03 | The Android application shall achieve a crash-free session rate of 99% or higher, as measured by crash reporting tooling. |
| NFR-A04 | The system shall implement health-check endpoints (`GET /health`) that return the current operational status of the API, database connection, and Redis connection. |
| NFR-A05 | The system shall be designed so that individual component failures (e.g., Redis unavailability) degrade functionality gracefully rather than causing total service failure. When Redis is unavailable, timeline responses shall be served directly from the database with appropriate latency tolerances. |
| NFR-A06 | Database backups shall be performed at minimum once every 24 hours, with point-in-time recovery capability. |

---

## 5.4 Scalability

| ID | Requirement |
|----|-------------|
| NFR-SC01 | The backend application shall be stateless, such that multiple instances may run concurrently behind a load balancer without session affinity requirements. |
| NFR-SC02 | The system architecture shall support horizontal scaling of the application tier by adding additional container instances without application code changes. |
| NFR-SC03 | The database schema shall be designed to support read replicas for read-heavy workloads; the application connection pool configuration shall distinguish between read and write connection targets. |
| NFR-SC04 | Object storage shall be provided by an externally managed, independently scalable service such that media storage capacity does not constrain application scaling. |
| NFR-SC05 | The system shall be designed for eventual migration to Kubernetes orchestration; Docker Compose is used for development only. |

---

## 5.5 Maintainability

| ID | Requirement |
|----|-------------|
| NFR-M01 | The backend codebase shall follow Clean Architecture principles, separating domain logic from infrastructure concerns. |
| NFR-M02 | All public API endpoints shall be documented in the OpenAPI specification; documentation shall be kept synchronized with implementation. |
| NFR-M03 | Database schema changes shall be managed exclusively through versioned Flyway migration scripts; manual schema alterations are not permitted in any environment. |
| NFR-M04 | The codebase shall maintain unit test coverage of at least 70% for domain and service layers. |
| NFR-M05 | The system shall emit structured logs in JSON format to standard output, suitable for ingestion by centralized log aggregation tooling. |
| NFR-M06 | Dependency versions shall be declared explicitly in build files (Maven `pom.xml`, `package.json`, Gradle build files); wildcard or unpinned versions are not permitted in production. |

---

## 5.6 Portability

| ID | Requirement |
|----|-------------|
| NFR-PO01 | The backend application shall be packaged as a Docker container image and shall run without modification on any OCI-compliant container runtime. |
| NFR-PO02 | The web application shall function correctly on the latest two stable releases of Chrome, Firefox, Safari, and Edge on both macOS and Windows. |
| NFR-PO03 | The Android application shall support devices running Android 10 (API Level 29) through the current Android release. |
| NFR-PO04 | No vendor-specific database extensions shall be used in the PostgreSQL schema; standard SQL constructs shall be preferred to preserve database portability. |
| NFR-PO05 | Object storage integration shall use the S3-compatible API so that the backing provider (AWS S3, MinIO, Cloudflare R2, etc.) may be substituted without application code changes. |

---

# 6. Data Requirements

---

## 6.1 Data Models

The following describes each core entity, its purpose, and its key fields. All entities use UUID v4 as the primary key unless noted otherwise.

### 6.1.1 User

Represents an account holder on the platform.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `username` | VARCHAR(30) | Unique, alphanumeric + underscore, case-insensitive |
| `email` | VARCHAR(255) | Unique, validated email address |
| `password_hash` | VARCHAR(255) | BCrypt hash of the user's password |
| `role` | ENUM | `GUEST`, `USER`, `ADMIN` |
| `status` | ENUM | `ACTIVE`, `SUSPENDED`, `DELETED` |
| `email_verified` | BOOLEAN | Whether the email address has been verified |
| `created_at` | TIMESTAMPTZ | Account creation timestamp |
| `updated_at` | TIMESTAMPTZ | Last modification timestamp |

### 6.1.2 Profile

Stores publicly visible user profile information, linked one-to-one with User.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user_id` | UUID | Foreign key to User |
| `display_name` | VARCHAR(50) | User's chosen display name |
| `bio` | VARCHAR(160) | Short biography |
| `website_url` | VARCHAR(255) | Optional website link |
| `location` | VARCHAR(100) | Optional location string |
| `profile_picture_url` | TEXT | URL to profile image in object storage |
| `banner_image_url` | TEXT | URL to banner image in object storage |
| `created_at` | TIMESTAMPTZ | Profile creation timestamp |
| `updated_at` | TIMESTAMPTZ | Last modification timestamp |

### 6.1.3 Post

Represents a piece of content published by a user.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `author_id` | UUID | Foreign key to User |
| `content` | VARCHAR(500) | Text body of the post |
| `type` | ENUM | `POST`, `REPLY`, `REPOST`, `QUOTE` |
| `parent_post_id` | UUID | Foreign key to Post (nullable; used for replies and quotes) |
| `repost_of_id` | UUID | Foreign key to Post (nullable; used for reposts) |
| `is_edited` | BOOLEAN | Whether the post has been edited |
| `edited_at` | TIMESTAMPTZ | Timestamp of last edit (nullable) |
| `like_count` | INTEGER | Denormalized like count |
| `reply_count` | INTEGER | Denormalized reply count |
| `repost_count` | INTEGER | Denormalized repost count |
| `created_at` | TIMESTAMPTZ | Post creation timestamp |
| `deleted_at` | TIMESTAMPTZ | Soft-delete timestamp (nullable) |

### 6.1.4 Media

Stores metadata for uploaded media files attached to posts or profiles.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `post_id` | UUID | Foreign key to Post (nullable) |
| `uploader_id` | UUID | Foreign key to User |
| `url` | TEXT | HTTPS URL of the object in storage |
| `type` | ENUM | `IMAGE`, `AVATAR`, `BANNER` |
| `mime_type` | VARCHAR(50) | MIME type (e.g., `image/jpeg`) |
| `size_bytes` | BIGINT | File size in bytes |
| `created_at` | TIMESTAMPTZ | Upload timestamp |

### 6.1.5 Comment (Reply)

Replies are modelled as Posts with `type = REPLY` and a non-null `parent_post_id`. No separate Comment entity is required at the data layer.

### 6.1.6 Like

Represents a user's like on a post.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user_id` | UUID | Foreign key to User |
| `post_id` | UUID | Foreign key to Post |
| `created_at` | TIMESTAMPTZ | Like timestamp |

A composite unique constraint on `(user_id, post_id)` enforces the one-like-per-user-per-post rule.

### 6.1.7 Follow

Represents a directional follow relationship between two users.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `follower_id` | UUID | Foreign key to User (the user who follows) |
| `followee_id` | UUID | Foreign key to User (the user being followed) |
| `created_at` | TIMESTAMPTZ | Follow timestamp |

A composite unique constraint on `(follower_id, followee_id)` enforces uniqueness. A check constraint prevents `follower_id = followee_id`.

### 6.1.8 Notification

Stores in-app notifications for users.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `recipient_id` | UUID | Foreign key to User (notification target) |
| `actor_id` | UUID | Foreign key to User (the user who caused the notification) |
| `type` | ENUM | `FOLLOW`, `LIKE`, `REPLY`, `MENTION`, `REPOST`, `QUOTE` |
| `post_id` | UUID | Foreign key to Post (nullable; context post) |
| `is_read` | BOOLEAN | Whether the notification has been read |
| `created_at` | TIMESTAMPTZ | Notification creation timestamp |

### 6.1.9 Bookmark

Stores a user's private saved posts.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user_id` | UUID | Foreign key to User |
| `post_id` | UUID | Foreign key to Post |
| `created_at` | TIMESTAMPTZ | Bookmark creation timestamp |

A composite unique constraint on `(user_id, post_id)` enforces uniqueness.

### 6.1.10 Hashtag

Stores unique hashtag strings indexed from post content.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `tag` | VARCHAR(100) | Lowercase hashtag string (without the `#` prefix) |
| `created_at` | TIMESTAMPTZ | First appearance timestamp |

### 6.1.11 PostHashtag

Junction table linking posts to hashtags (many-to-many).

| Field | Type | Description |
|-------|------|-------------|
| `post_id` | UUID | Foreign key to Post |
| `hashtag_id` | UUID | Foreign key to Hashtag |

---

## 6.2 Data Retention

- Active user data shall be retained indefinitely while the account remains in an `ACTIVE` or `SUSPENDED` state.
- When a user deletes their account, their User and Profile records shall be soft-deleted (status set to `DELETED`) and their display name and email shall be anonymized within 30 days.
- Deleted posts shall be soft-deleted immediately (populating `deleted_at`) and permanently purged from the database after 30 days.
- Associated media files shall be deleted from object storage within 24 hours of the owning post or account deletion.
- Notification records shall be retained for 90 days after creation and then permanently deleted.
- Audit log records (Administrator actions) shall be retained for a minimum of 1 year.

---

## 6.3 Data Integrity

- All foreign key relationships shall be enforced at the database level with appropriate `ON DELETE` cascade or restrict rules.
- Denormalized counters (like count, reply count, repost count on Post) shall be updated atomically using database-level increment/decrement operations to prevent race conditions.
- All write operations involving multiple tables shall execute within a single database transaction to ensure atomicity.
- Database constraints (unique, not-null, check) shall be defined at the schema level and shall not rely solely on application-layer validation.

---

## 6.4 Data Privacy

- User email addresses shall not be exposed in any public-facing API response.
- User IP addresses collected for rate-limiting and audit purposes shall not be stored beyond 90 days.
- Passwords (plaintext or hash) shall never appear in API responses, log output, or error messages.
- The system shall provide a mechanism for users to export all of their own data in a machine-readable format (future release).
- The system shall comply with applicable data protection principles, including data minimization and purpose limitation.

---

# 7. Business Rules

| ID | Rule |
|----|------|
| BR-001 | A username must be unique across the entire platform, case-insensitively. Attempting to register with a username that differs only in case from an existing username shall be rejected. |
| BR-002 | A username must be between 3 and 30 characters and may contain only lowercase letters, numbers, and underscores. |
| BR-003 | An email address must be unique across the entire platform. A single email address may not be associated with more than one account. |
| BR-004 | A post must not exceed 500 characters of text content. Empty posts (no text and no media) shall not be permitted. |
| BR-005 | A post may contain a maximum of 4 image attachments. |
| BR-006 | A post may be edited only within 15 minutes of its original creation timestamp. Edits after this window are not permitted. |
| BR-007 | A user may like a given post at most once. Duplicate likes are not permitted. |
| BR-008 | A user may not follow themselves. |
| BR-009 | A follow relationship between two users is unique; a user may not create duplicate follow relationships with the same target. |
| BR-010 | Notifications shall not be generated for a user's own actions (e.g., a user liking their own post shall not produce a notification). |
| BR-011 | A user whose account status is `SUSPENDED` shall be denied login; the system shall return an appropriate error message identifying the account as suspended. |
| BR-012 | A user whose account status is `DELETED` shall have all content hidden from public view, and their username shall be released for reuse after the anonymization period. |
| BR-013 | Hashtags are case-insensitive; `#Boondi` and `#boondi` shall be treated as the same hashtag and stored in lowercase. |
| BR-014 | A repost creates a new post entry of type `REPOST` in the database; a user may repost the same post only once. |
| BR-015 | A quote post requires a non-empty text body in addition to the reference to the original post. |
| BR-016 | A reply must reference an existing, non-deleted parent post. Replies to deleted posts shall be rejected. |
| BR-017 | Only the post author or an Administrator may delete a post. |
| BR-018 | Only the post author may edit a post, within the permitted editing window. |
| BR-019 | Platform-wide announcements created by an Administrator shall be visually distinguished from regular user posts in all client interfaces. |
| BR-020 | Rate limiting shall apply to all API endpoints; authenticated users shall be limited to 300 requests per minute; unauthenticated clients shall be limited to 30 requests per minute. |
| BR-021 | A user's follower count and following count shall always reflect the current number of active follow relationships and shall be recalculated if a discrepancy is detected. |
| BR-022 | Bookmarks are private; they shall not be visible to any user other than the owner, including Administrators. |
| BR-023 | A user may not bookmark their own post (implementation may optionally allow this; final decision to be confirmed during detailed design). |
| BR-024 | Search results shall not include posts or accounts with a `DELETED` status. Suspended user accounts may appear in search but shall be visually marked as suspended. |
| BR-025 | Profile biography text must not exceed 160 characters. |

---

# 8. User Stories

User stories are organized by feature area. Each story follows the format: *As a [role], I want to [action], so that [benefit].*

---

## 8.1 Registration & Authentication

- **US-001** — As a Guest, I want to register an account using my email address and a chosen username, so that I can participate in the Boondi community.
- **US-002** — As a Guest, I want to log in with my email and password, so that I can access my account and its features.
- **US-003** — As a Registered User, I want to log out of my session, so that my account remains secure on shared devices.
- **US-004** — As a Registered User, I want my session to be automatically renewed using a refresh token, so that I am not forced to re-enter my credentials frequently during an active session.
- **US-005** — As a Registered User, I want to receive a clear error message if I enter an incorrect password, so that I understand why my login failed.

---

## 8.2 User Profile

- **US-006** — As a Registered User, I want to upload a profile picture, so that other users can recognize me visually.
- **US-007** — As a Registered User, I want to write a short biography on my profile, so that other users can learn about me.
- **US-008** — As a Registered User, I want to view another user's profile, so that I can see their posts, follower count, and following count.
- **US-009** — As a Registered User, I want to update my display name, so that I can choose how my name appears to others without changing my username.
- **US-010** — As a Registered User, I want to add a website link and location to my profile, so that I can share additional information with the community.

---

## 8.3 Posting

- **US-011** — As a Registered User, I want to create a text post, so that I can share thoughts and updates with my followers.
- **US-012** — As a Registered User, I want to attach images to my post, so that I can share visual content alongside text.
- **US-013** — As a Registered User, I want to edit my post within 15 minutes of publishing, so that I can correct mistakes quickly.
- **US-014** — As a Registered User, I want to delete one of my posts, so that I can remove content I no longer wish to share.
- **US-015** — As a Registered User, I want to see a character counter while composing a post, so that I know how much of the 500-character limit I have used.
- **US-016** — As a Registered User, I want to use hashtags in my posts, so that my content can be discovered by users interested in that topic.

---

## 8.4 Social Interactions

- **US-017** — As a Registered User, I want to like a post, so that I can express appreciation for content I enjoy.
- **US-018** — As a Registered User, I want to unlike a post I have previously liked, so that I can retract appreciation if I change my mind.
- **US-019** — As a Registered User, I want to reply to a post, so that I can contribute to conversations.
- **US-020** — As a Registered User, I want to repost content from another user, so that I can share it with my followers.
- **US-021** — As a Registered User, I want to quote another user's post and add my own commentary, so that I can share content with context.
- **US-022** — As a Registered User, I want to bookmark a post, so that I can save it for later reading without interacting publicly.
- **US-023** — As a Registered User, I want to view all my bookmarked posts in one place, so that I can easily return to saved content.

---

## 8.5 Following

- **US-024** — As a Registered User, I want to follow another user, so that their posts appear in my Home Timeline.
- **US-025** — As a Registered User, I want to unfollow a user, so that I can remove their content from my timeline.
- **US-026** — As a Registered User, I want to view a list of users I follow and users who follow me, so that I can manage my connections.

---

## 8.6 Timeline & Feed

- **US-027** — As a Registered User, I want to view a Home Timeline of posts from users I follow, so that I can stay up to date with people I care about.
- **US-028** — As a Registered User, I want to switch to a Latest Timeline showing all recent posts, so that I can discover new content and users.
- **US-029** — As a Registered User, I want to view a Trending Timeline of the most popular posts and hashtags, so that I can see what topics are active in the community.
- **US-030** — As a Registered User, I want to view all posts by a specific user on their profile page, so that I can explore their content history.
- **US-031** — As a Registered User, I want my timeline to load quickly, so that I can begin reading content without perceiving a delay.

---

## 8.7 Notifications

- **US-032** — As a Registered User, I want to receive a notification when someone follows me, so that I am aware of new connections.
- **US-033** — As a Registered User, I want to receive a notification when someone likes my post, so that I can see which of my content resonates.
- **US-034** — As a Registered User, I want to receive a notification when someone replies to my post, so that I can continue the conversation.
- **US-035** — As a Registered User, I want to receive a notification when someone mentions my username in a post, so that I do not miss relevant discussions.
- **US-036** — As a Registered User, I want to mark all my notifications as read at once, so that I can clear my notification badge efficiently.

---

## 8.8 Search

- **US-037** — As a Registered User, I want to search for other users by username or display name, so that I can find and follow people I know.
- **US-038** — As a Registered User, I want to search for posts containing specific keywords, so that I can find relevant content.
- **US-039** — As a Registered User, I want to search by hashtag, so that I can explore all posts on a topic I am interested in.

---

## 8.9 Administration

- **US-040** — As an Administrator, I want to suspend a user account, so that I can prevent a user who violates community guidelines from accessing the platform.
- **US-041** — As an Administrator, I want to reinstate a suspended user account, so that a user who has addressed a violation can regain access.
- **US-042** — As an Administrator, I want to delete any post on the platform, so that I can remove content that violates community standards.
- **US-043** — As an Administrator, I want to review user-submitted reports, so that I can take appropriate moderation action.
- **US-044** — As an Administrator, I want to post a platform-wide announcement, so that I can communicate important information to all users.

---

# 9. Use Cases

---

## UC-001: Register Account

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-001 |
| **Name** | Register Account |
| **Actor** | Guest |
| **Preconditions** | The actor has navigated to the registration screen. No active authenticated session exists. |
| **Postconditions** | A new User record and associated Profile record are created. The actor receives an access token and refresh token and is redirected to the Home Timeline. |

**Main Flow**

1. The actor enters a desired username, email address, and password.
2. The actor submits the registration form.
3. The system validates that the username meets format requirements (3–30 characters, alphanumeric + underscore).
4. The system validates that the username is not already in use (case-insensitive).
5. The system validates that the email address is syntactically valid and not already registered.
6. The system validates that the password meets minimum complexity requirements.
7. The system hashes the password using BCrypt.
8. The system creates a new User record with status `ACTIVE` and role `USER`.
9. The system creates a default Profile record linked to the new User.
10. The system issues a JWT access token and refresh token.
11. The system returns HTTP 201 Created with the token pair.
12. The client stores the tokens and navigates the actor to the Home Timeline.

**Alternative Flows**

- **A1 — Username already taken (step 4):** The system returns HTTP 409 Conflict with a descriptive error message. The form remains populated.
- **A2 — Email already registered (step 5):** The system returns HTTP 409 Conflict with a descriptive error message.
- **A3 — Invalid input (steps 3–6):** The system returns HTTP 400 Bad Request with field-level validation errors. No User record is created.

---

## UC-002: Login

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-002 |
| **Name** | Login |
| **Actor** | Registered User |
| **Preconditions** | The actor has an existing account in `ACTIVE` status. No active authenticated session exists. |
| **Postconditions** | The actor receives a valid JWT access token and refresh token. |

**Main Flow**

1. The actor enters their email address and password.
2. The actor submits the login form.
3. The system retrieves the User record matching the provided email.
4. The system verifies the provided password against the stored BCrypt hash.
5. The system checks that the account status is `ACTIVE`.
6. The system issues a new JWT access token (15-minute expiry) and refresh token (7-day expiry).
7. The system returns HTTP 200 OK with the token pair.
8. The client stores the tokens and navigates the actor to the Home Timeline.

**Alternative Flows**

- **A1 — Email not found (step 3):** The system returns HTTP 401 Unauthorized with a generic "invalid credentials" message (email address is not disclosed in the error response).
- **A2 — Incorrect password (step 4):** The system returns HTTP 401 Unauthorized with a generic "invalid credentials" message. The system increments the failed attempt counter for the IP address.
- **A3 — Account suspended (step 5):** The system returns HTTP 403 Forbidden with a message indicating the account is suspended.
- **A4 — Rate limit exceeded:** After 10 failed attempts within 15 minutes from the same IP, the system returns HTTP 429 Too Many Requests and temporarily blocks further attempts from that IP.

---

## UC-003: Create Post

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-003 |
| **Name** | Create Post |
| **Actor** | Registered User |
| **Preconditions** | The actor is authenticated with a valid access token. |
| **Postconditions** | A new Post record is created and appears in the actor's User Timeline and in the Home Timeline of all followers. Hashtags and mentions are indexed. |

**Main Flow**

1. The actor composes a post, optionally attaching up to 4 images.
2. The actor submits the post.
3. The system validates that the text content does not exceed 500 characters.
4. The system validates that no more than 4 images are attached and each does not exceed 10 MB.
5. The system uploads any attached images to object storage and records Media entries.
6. The system creates a new Post record of type `POST`.
7. The system extracts and indexes any hashtags found in the post text.
8. The system extracts mentions and queues mention notifications.
9. The system returns HTTP 201 Created with the full Post representation.
10. The client displays the new post at the top of the actor's feed.

**Alternative Flows**

- **A1 — Text exceeds 500 characters (step 3):** The system returns HTTP 400 Bad Request. No post is created.
- **A2 — More than 4 images attached (step 4):** The system returns HTTP 400 Bad Request.
- **A3 — Image upload fails (step 5):** The system rolls back the transaction, deletes any partially uploaded objects from storage, and returns HTTP 500 Internal Server Error.

---

## UC-004: Follow User

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-004 |
| **Name** | Follow User |
| **Actor** | Registered User |
| **Preconditions** | The actor is authenticated. The target user exists and is not the actor. No existing follow relationship exists between actor and target. |
| **Postconditions** | A Follow record is created. The target user's follower count increments by 1. The actor's following count increments by 1. A `FOLLOW` notification is created for the target user. |

**Main Flow**

1. The actor navigates to the target user's profile.
2. The actor clicks the Follow button.
3. The system validates that the actor is not attempting to follow themselves.
4. The system checks that no existing follow relationship exists.
5. The system creates a Follow record with `follower_id` = actor and `followee_id` = target.
6. The system atomically increments the target user's follower count and the actor's following count.
7. The system creates a `FOLLOW` notification for the target user.
8. The system returns HTTP 200 OK.
9. The client updates the Follow button to an Unfollow state.

**Alternative Flows**

- **A1 — Actor attempts to follow themselves (step 3):** The system returns HTTP 400 Bad Request.
- **A2 — Follow relationship already exists (step 4):** The system returns HTTP 409 Conflict.
- **A3 — Target user not found:** The system returns HTTP 404 Not Found.

---

## UC-005: View Home Timeline

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-005 |
| **Name** | View Home Timeline |
| **Actor** | Registered User |
| **Preconditions** | The actor is authenticated. |
| **Postconditions** | The actor's client displays a paginated list of posts from followed users, ordered newest first. |

**Main Flow**

1. The actor navigates to the Home Timeline.
2. The client sends `GET /timeline/home` with the actor's access token.
3. The system checks the Redis cache for a pre-computed timeline result for the actor.
4. If the cache is warm, the system returns the cached result.
5. If the cache is cold, the system queries the database for posts by users the actor follows, ordered by `created_at` descending.
6. The system populates the cache with the result (TTL: 60 seconds).
7. The system returns HTTP 200 OK with a paginated list of posts.
8. The client renders the posts in the feed.

**Alternative Flows**

- **A1 — Actor follows no users:** The system returns HTTP 200 OK with an empty list and a suggested-users payload to encourage discovery.
- **A2 — Redis unavailable (step 3):** The system logs the cache failure and proceeds directly to step 5, serving the response from the database.

---

## UC-006: Like a Post

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-006 |
| **Name** | Like a Post |
| **Actor** | Registered User |
| **Preconditions** | The actor is authenticated. The target post exists and is not deleted. The actor has not previously liked this post. |
| **Postconditions** | A Like record is created. The post's like count increments by 1. A `LIKE` notification is created for the post author (unless the actor is the author). |

**Main Flow**

1. The actor taps or clicks the Like button on a post.
2. The client sends `POST /posts/{id}/like`.
3. The system retrieves the post and verifies it exists and is not deleted.
4. The system checks that no Like record exists for the `(actor, post)` pair.
5. The system creates a Like record.
6. The system atomically increments the post's `like_count`.
7. The system creates a `LIKE` notification for the post author if the author is not the actor.
8. The system returns HTTP 200 OK.
9. The client fills the Like button and updates the displayed count.

**Alternative Flows**

- **A1 — Post not found or deleted (step 3):** The system returns HTTP 404 Not Found.
- **A2 — Already liked (step 4):** The system returns HTTP 409 Conflict.

---

## UC-007: Search for Users and Posts

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-007 |
| **Name** | Search for Users and Posts |
| **Actor** | Registered User |
| **Preconditions** | The actor is authenticated. |
| **Postconditions** | The client displays matching users and/or posts relevant to the search query. |

**Main Flow**

1. The actor enters a search query into the search bar.
2. The client sends `GET /search/users?q={query}` and `GET /search/posts?q={query}` concurrently.
3. The system performs a case-insensitive match against user `username` and `display_name` fields.
4. The system performs a case-insensitive full-text match against post `content` fields.
5. Both endpoints exclude deleted accounts and deleted posts.
6. The system returns paginated results from each endpoint.
7. The client displays users and posts in separate tabs or sections on the search results screen.

**Alternative Flows**

- **A1 — No results found:** The system returns HTTP 200 OK with empty result lists. The client displays an appropriate empty state.
- **A2 — Query is blank or too short (fewer than 2 characters):** The client prevents submission and displays a hint to the actor.

---

## UC-008: Suspend User (Administrator)

| Attribute | Detail |
|-----------|--------|
| **Use Case ID** | UC-008 |
| **Name** | Suspend User |
| **Actor** | Administrator |
| **Preconditions** | The actor is authenticated with Administrator role. The target user account exists and is in `ACTIVE` status. |
| **Postconditions** | The target user's account status is set to `SUSPENDED`. Any active refresh tokens for the target are invalidated. An audit log entry is created. The target user is prevented from logging in. |

**Main Flow**

1. The Administrator navigates to the user management section of the admin interface.
2. The Administrator locates the target user account and selects "Suspend Account."
3. The Administrator provides a reason for suspension.
4. The Administrator confirms the action.
5. The system sets the target User's status to `SUSPENDED`.
6. The system invalidates all refresh tokens associated with the target user by adding them to the Redis denylist.
7. The system creates an audit log entry recording the action, the Administrator's ID, the target user's ID, the reason, and a timestamp.
8. The system returns HTTP 200 OK.
9. The admin interface reflects the updated account status.

**Alternative Flows**

- **A1 — Target user not found:** The system returns HTTP 404 Not Found.
- **A2 — Target user already suspended:** The system returns HTTP 409 Conflict.
- **A3 — Actor is not an Administrator:** The system returns HTTP 403 Forbidden.

---

# 10. Acceptance Criteria

Acceptance criteria are written in Gherkin format (Given / When / Then) to facilitate automated testing.

---

## 10.1 Authentication

```gherkin
Feature: User Registration

  Scenario: Successful registration
    Given a visitor has not previously registered on Boondi
    When they submit a registration form with a unique username, a valid email, and a strong password
    Then the system creates a new account
    And returns an access token and a refresh token
    And the client redirects to the Home Timeline

  Scenario: Duplicate username rejected
    Given a user with username "alice" already exists
    When a new visitor attempts to register with username "Alice"
    Then the system returns HTTP 409 Conflict
    And no new account is created

  Scenario: Password too weak
    Given a visitor is on the registration screen
    When they submit a password shorter than 8 characters
    Then the system returns HTTP 400 Bad Request
    And the response body includes a field-level error for the password field

Feature: User Login

  Scenario: Successful login
    Given a registered user with an active account
    When they submit their correct email and password
    Then the system returns HTTP 200 OK
    And the response includes a valid JWT access token and refresh token

  Scenario: Incorrect password
    Given a registered user with an active account
    When they submit their email with an incorrect password
    Then the system returns HTTP 401 Unauthorized
    And no tokens are issued

  Scenario: Suspended account login attempt
    Given a user whose account status is SUSPENDED
    When they attempt to log in with correct credentials
    Then the system returns HTTP 403 Forbidden
    And the response indicates the account is suspended
```

---

## 10.2 Post Management

```gherkin
Feature: Create Post

  Scenario: Successful text post
    Given an authenticated user
    When they submit a post with 280 characters of text and no images
    Then the system creates the post
    And returns HTTP 201 Created
    And the post appears in the user's timeline

  Scenario: Post exceeds character limit
    Given an authenticated user
    When they submit a post with 501 characters of text
    Then the system returns HTTP 400 Bad Request
    And no post record is created

  Scenario: Post with 4 images
    Given an authenticated user
    When they submit a post with valid text and 4 JPEG images each under 10 MB
    Then the system stores all 4 images in object storage
    And creates the post with 4 associated media records
    And returns HTTP 201 Created

  Scenario: Post with 5 images rejected
    Given an authenticated user
    When they submit a post with 5 image attachments
    Then the system returns HTTP 400 Bad Request

Feature: Edit Post

  Scenario: Edit within time window
    Given an authenticated user has a post created 5 minutes ago
    When they submit an edit to the post text
    Then the system updates the post content
    And marks the post as edited
    And records the edit timestamp

  Scenario: Edit after time window
    Given an authenticated user has a post created 20 minutes ago
    When they attempt to edit the post
    Then the system returns HTTP 403 Forbidden

Feature: Delete Post

  Scenario: Author deletes own post
    Given an authenticated user is the author of a post
    When they send DELETE /posts/{id}
    Then the system soft-deletes the post
    And removes associated media from object storage
    And returns HTTP 204 No Content

  Scenario: Non-author attempts to delete post
    Given an authenticated user who is not the post author
    When they send DELETE /posts/{id}
    Then the system returns HTTP 403 Forbidden
```

---

## 10.3 Social Interactions

```gherkin
Feature: Like and Unlike

  Scenario: Like a post
    Given an authenticated user who has not liked post P
    When they send POST /posts/{P}/like
    Then the system creates a Like record
    And increments the post's like count by 1
    And returns HTTP 200 OK

  Scenario: Duplicate like rejected
    Given an authenticated user who has already liked post P
    When they send POST /posts/{P}/like again
    Then the system returns HTTP 409 Conflict
    And the like count is unchanged

  Scenario: Unlike a post
    Given an authenticated user who has liked post P
    When they send DELETE /posts/{P}/like
    Then the system removes the Like record
    And decrements the post's like count by 1
    And returns HTTP 200 OK

Feature: Follow and Unfollow

  Scenario: Follow a user
    Given an authenticated user A who does not follow user B
    When A sends POST /users/{B}/follow
    Then a Follow record is created
    And B's follower count increments by 1
    And A's following count increments by 1

  Scenario: Follow self rejected
    Given an authenticated user A
    When A sends POST /users/{A}/follow
    Then the system returns HTTP 400 Bad Request

  Scenario: Duplicate follow rejected
    Given an authenticated user A who already follows user B
    When A sends POST /users/{B}/follow again
    Then the system returns HTTP 409 Conflict
```

---

## 10.4 Timeline

```gherkin
Feature: Home Timeline

  Scenario: Timeline returns posts from followed users
    Given an authenticated user A who follows users B and C
    And B has posted 3 posts and C has posted 2 posts
    When A requests GET /timeline/home
    Then the response contains posts from B and C
    And posts are ordered by creation timestamp descending

  Scenario: Empty timeline with no follows
    Given an authenticated user who follows no one
    When they request GET /timeline/home
    Then the system returns HTTP 200 OK
    And the data array is empty
```

---

## 10.5 Notifications

```gherkin
Feature: Notification Generation

  Scenario: Like notification
    Given user A authored post P
    And user B is authenticated and has not liked post P
    When B likes post P
    Then a LIKE notification is created with recipient_id = A and actor_id = B

  Scenario: No self-notification on like
    Given user A authored post P
    When A likes their own post P
    Then no notification is created

  Scenario: Follow notification
    Given user B is not following user A
    When B follows A
    Then a FOLLOW notification is created with recipient_id = A and actor_id = B

Feature: Read Notifications

  Scenario: Mark all as read
    Given an authenticated user with 5 unread notifications
    When they send POST /notifications/read-all
    Then all 5 notifications have is_read = true
    And the unread count returned by GET /notifications/unread-count is 0
```

---

## 10.6 Search

```gherkin
Feature: User Search

  Scenario: Search by username fragment
    Given users with usernames "alice_dev", "alice_design", and "bob_dev" exist
    When an authenticated user searches GET /search/users?q=alice
    Then the response includes "alice_dev" and "alice_design"
    And does not include "bob_dev"

  Scenario: Case-insensitive search
    Given a user with username "Charlie" exists
    When an authenticated user searches GET /search/users?q=charlie
    Then the response includes the "Charlie" account

Feature: Post Search

  Scenario: Search by keyword
    Given posts containing the word "kotlin" exist
    When an authenticated user searches GET /search/posts?q=kotlin
    Then all returned posts contain the word "kotlin" in their content

  Scenario: Deleted posts excluded from search
    Given a post containing "kotlin" has been deleted
    When an authenticated user searches GET /search/posts?q=kotlin
    Then the deleted post does not appear in the results
```

---

## 10.7 Administration

```gherkin
Feature: Suspend User

  Scenario: Administrator suspends an active user
    Given an Administrator is authenticated
    And a user with status ACTIVE exists
    When the Administrator sends a suspend request for that user with a reason
    Then the user's status is set to SUSPENDED
    And an audit log entry is created
    And the user is denied login on subsequent attempts

  Scenario: Non-administrator cannot suspend
    Given a Registered User is authenticated
    When they attempt to call the suspend endpoint
    Then the system returns HTTP 403 Forbidden

Feature: Delete Any Post

  Scenario: Administrator deletes a post
    Given an Administrator is authenticated
    And a post authored by another user exists
    When the Administrator sends DELETE /posts/{id}
    Then the post is soft-deleted
    And the media is queued for removal from object storage
    And an audit log entry is created
```

---

# 11. Future Enhancements

This section describes features explicitly excluded from the MVP that are planned for future releases. These enhancements are intended to expand the platform's capabilities after the core experience has been validated.

---

## 11.1 Direct Messaging

A private one-to-one and group messaging system will allow users to exchange messages outside the public post feed. Key capabilities will include:

- Threaded conversation view
- Media sharing within messages
- Message read receipts
- Unread message badge count
- Real-time delivery via WebSocket

Direct messages will be end-to-end encrypted between clients in a future iteration. The initial release will use server-side encryption at rest.

---

## 11.2 Stories

Time-limited, ephemeral content visible for 24 hours will be introduced as a Stories feature. Key capabilities will include:

- Image and short video story creation
- Viewer count and viewer list visible to the story author
- Automatic expiration after 24 hours
- Distinct story ring indicator on profile pictures in the feed

---

## 11.3 Communities

Topic-based communities will allow groups of users to organize content around shared interests. Key capabilities will include:

- Community creation and naming
- Role-based community membership (Owner, Moderator, Member)
- Community-specific post feed
- Invite link and join request flow
- Community-level rules and moderation tools

---

## 11.4 Polls

Structured voting on posts will allow users to create time-bounded polls alongside post content. Key capabilities will include:

- Up to 4 answer options per poll
- Configurable poll duration (1 hour to 7 days)
- Real-time vote count display
- Prevention of vote changes after submission
- Poll results visible to all users after the poll closes

---

## 11.5 Video Uploads

Support for user-uploaded video content will be introduced in a future release. Key capabilities will include:

- Video uploads up to a configurable size limit (target: 500 MB for MVP of this feature)
- Supported formats: MP4 (H.264), MOV
- Server-side transcoding to normalized HLS or DASH streams
- Thumbnail generation from the first frame or a user-selected frame
- Adaptive bitrate playback on both Android and web clients

---

## 11.6 Live Streaming

Real-time broadcast streaming will enable users to stream live video to their followers. Key capabilities will include:

- RTMP ingest with HLS delivery
- Live viewer count display
- Real-time comment overlay during streams
- Replay recording stored as a video post after the stream ends
- Stream moderation tools for the streamer and Administrators

---

## 11.7 AI-Powered Moderation

An automated content moderation layer will augment Administrator review workflows. Key capabilities will include:

- Image classification to detect policy-violating visual content
- Text classification to flag potentially harmful or abusive posts
- Automated hold queue for flagged content pending human review
- Confidence scores surfaced to Administrators to prioritize review

---

## 11.8 Recommendation Engine

A personalized content recommendation system will enhance content discovery. Key capabilities will include:

- Personalized "For You" timeline tab based on engagement signals (likes, replies, time spent)
- User recommendation ("People you may know") based on mutual connections and shared interests
- Trending hashtag personalization weighted by the user's follow graph
- Regular re-training of ranking models against collected engagement data

---

# 12. Appendix

---

## 12.1 Glossary

| Term | Definition |
|------|------------|
| Access Token | A short-lived JWT (15-minute expiry) used to authenticate API requests. Passed as a Bearer token in the HTTP Authorization header. |
| Administrator | A privileged user role with the ability to moderate content, suspend accounts, and post platform-wide announcements. |
| Audit Log | An immutable record of privileged administrative actions, capturing the actor, action type, target, reason, and timestamp. |
| BCrypt | A password-hashing algorithm based on the Blowfish cipher. Used by Boondi to store passwords securely with a configurable work factor. |
| Bookmark | A private mechanism allowing a user to save a post for later reference, invisible to all other users. |
| CDN | Content Delivery Network. A geographically distributed network of servers used to serve static assets (images, media) with low latency. |
| Clean Architecture | A software design philosophy separating business logic from infrastructure concerns through layered abstractions. |
| Comment | See Reply. In Boondi, replies to posts are modelled as Post records with type REPLY. |
| DTO | Data Transfer Object. A simple object used to carry data between process layers, particularly between the API layer and the service layer. |
| Feed | An ordered list of posts rendered in the client interface. Synonymous with Timeline in this document. |
| Follow | A directional relationship in which one user (follower) subscribes to the content of another user (followee). |
| Followee | The user being followed in a follow relationship. |
| Follower | The user who initiates a follow relationship. |
| Guest | An unauthenticated visitor who may view the landing page and access the registration and login screens but cannot interact with content. |
| Hashtag | A metadata tag embedded in post text using the `#` prefix, used to categorize content and enable topic-based discovery. |
| HikariCP | A high-performance JDBC connection pool library used to manage PostgreSQL connections in the Spring Boot backend. |
| Home Timeline | A personalized feed composed of posts from all users that the authenticated user follows. |
| HSTS | HTTP Strict Transport Security. A web security header that instructs browsers to interact with the server only over HTTPS. |
| JWT | JSON Web Token. A compact, URL-safe token format used for stateless authentication. Defined in RFC 7519. |
| Latest Timeline | A chronological feed of all posts from all users on the platform, ordered by creation time descending. |
| Like | A positive reaction a user can attach to a post, incrementing the post's like count. Each user may like a given post only once. |
| Mention | A reference to another user's account within post text, created using the `@username` syntax, which triggers a notification to the mentioned user. |
| Notification | An in-app alert generated by a social action (like, reply, follow, mention, repost, quote) and delivered to the relevant recipient. |
| Object Storage | A scalable, flat-namespace file storage service used to store user-uploaded media. Compatible with the Amazon S3 API. |
| Post | The primary unit of user-generated content on Boondi. A post may contain text (up to 500 characters) and up to 4 images. |
| Profile | A public-facing record associated with a User account, containing display name, biography, website URL, location, and profile/banner images. |
| Quote Post | A post that embeds a reference to another user's post alongside new text commentary authored by the quoting user. |
| RBAC | Role-Based Access Control. An authorization model in which access to system resources is determined by the roles assigned to a user. |
| Redis | An open-source, in-memory data store used by Boondi for caching timeline responses, storing refresh token denylists, and tracking rate-limit counters. |
| Refresh Token | A long-lived token (7-day expiry) used to obtain a new access token without requiring the user to re-enter credentials. |
| Registered User | A user who has completed registration and is authenticated. Has full access to social features excluding administrative functions. |
| Reply | A post of type REPLY that is linked to a parent post. Represents a direct response within a conversation thread. |
| Repost | A post of type REPOST that republishes an existing post to the reposting user's followers without additional commentary. |
| REST | Representational State Transfer. An architectural style for distributed hypermedia systems. The Boondi API adheres to REST principles. |
| SPA | Single Page Application. A web application that loads a single HTML page and dynamically updates content via JavaScript without full-page reloads. |
| Timeline | A time-ordered list of posts shown to a user. Boondi provides Home, Latest, User, and Trending timeline variants. |
| Trending Timeline | A feed surfacing posts and hashtags ranked by engagement activity within a rolling 24-hour window. |
| TTL | Time to Live. A duration after which a cached or stored record is automatically expired or invalidated. |
| User | An account holder on the Boondi platform. Each User has exactly one associated Profile. |
| User Timeline | A feed displaying all posts authored by a specific user, shown on that user's profile page. |
| UUID | Universally Unique Identifier. A 128-bit identifier used as the primary key for all entities in the Boondi database. |

---

## 12.2 Acronyms

| Acronym | Expansion |
|---------|-----------|
| API | Application Programming Interface |
| BCrypt | Blowfish Crypt (password hashing algorithm) |
| CDN | Content Delivery Network |
| CORS | Cross-Origin Resource Sharing |
| DTO | Data Transfer Object |
| HSTS | HTTP Strict Transport Security |
| HTTP | Hypertext Transfer Protocol |
| HTTPS | Hypertext Transfer Protocol Secure |
| IDE | Integrated Development Environment |
| IEEE | Institute of Electrical and Electronics Engineers |
| JWT | JSON Web Token |
| MVP | Minimum Viable Product |
| OCI | Open Container Initiative |
| OWASP | Open Web Application Security Project |
| RBAC | Role-Based Access Control |
| REST | Representational State Transfer |
| RFC | Request for Comments |
| SPA | Single Page Application |
| SQL | Structured Query Language |
| SRS | Software Requirements Specification |
| TLS | Transport Layer Security |
| TTL | Time to Live |
| UI | User Interface |
| URL | Uniform Resource Locator |
| UUID | Universally Unique Identifier |
| WCAG | Web Content Accessibility Guidelines |

---

## 12.3 Document Revision History

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 0.1 | 2025-06-01 | Boondi Core Team | Initial draft — sections 1 and 2 authored. |
| 0.2 | 2026-07-02 | Boondi Core Team | Sections 3–12 completed: functional requirements, interface requirements, non-functional requirements, data requirements, business rules, user stories, use cases, acceptance criteria, future enhancements, and appendix. |