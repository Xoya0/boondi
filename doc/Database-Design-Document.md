# Database Design Document (DDD) — Boondi

**Version**: 1.0.0
**Date**: 2026-07-02
**Author**: Database Architecture Team
**Status**: Approved — Ready for Implementation

---

## Table of Contents

1. [Introduction](#1-introduction)
   - 1.1 [Purpose](#11-purpose)
   - 1.2 [Scope](#12-scope)
   - 1.3 [Database Technology Choice](#13-database-technology-choice)
   - 1.4 [Conventions Used in This Document](#14-conventions-used-in-this-document)
2. [Database Architecture Overview](#2-database-architecture-overview)
   - 2.1 [Database Components](#21-database-components)
   - 2.2 [Data Flow Overview](#22-data-flow-overview)
   - 2.3 [Connection Strategy](#23-connection-strategy)
3. [Entity Relationship Diagram](#3-entity-relationship-diagram)
4. [Table Definitions](#4-table-definitions)
   - 4.1 [users](#41-users)
   - 4.2 [posts](#42-posts)
   - 4.3 [media](#43-media)
   - 4.4 [follows](#44-follows)
   - 4.5 [likes](#45-likes)
   - 4.6 [bookmarks](#46-bookmarks)
   - 4.7 [notifications](#47-notifications)
   - 4.8 [hashtags](#48-hashtags)
   - 4.9 [post_hashtags](#49-post_hashtags)
   - 4.10 [refresh_tokens](#410-refresh_tokens)
   - 4.11 [password_reset_tokens](#411-password_reset_tokens)
   - 4.12 [email_verifications](#412-email_verifications)
   - 4.13 [reports](#413-reports)
5. [Indexes Strategy](#5-indexes-strategy)
6. [PostgreSQL-Specific Features](#6-postgresql-specific-features)
   - 6.1 [UUID Generation](#61-uuid-generation)
   - 6.2 [ENUM Types](#62-enum-types)
   - 6.3 [Full-Text Search Setup](#63-full-text-search-setup)
   - 6.4 [Constraints and Check Constraints](#64-constraints-and-check-constraints)
   - 6.5 [Triggers](#65-triggers)
7. [Redis Schema](#7-redis-schema)
8. [Database Migrations](#8-database-migrations)
9. [Data Integrity and Constraints](#9-data-integrity--constraints)
10. [Query Patterns and Performance](#10-query-patterns--performance)
11. [Data Security](#11-data-security)
12. [Backup and Recovery](#12-backup--recovery)
13. [Scalability Considerations](#13-scalability-considerations)

---

## 1. Introduction

### 1.1 Purpose

This Database Design Document (DDD) defines the complete relational data model, caching strategy, and storage architecture for **Boondi** — a private social networking platform inspired by Twitter/X. It serves as the authoritative reference for:

- Backend engineers implementing JPA entities and Spring Data repositories
- DevOps engineers provisioning database infrastructure and managing migrations
- Security engineers auditing data handling and access control
- Future architects evaluating scalability decisions

This document describes every table, column, index, constraint, trigger, and Redis key structure required to build and operate the Boondi platform at MVP and beyond.

### 1.2 Scope

This document covers:

- **PostgreSQL** — primary relational database (all persistent application data)
- **Redis** — caching layer (sessions, timelines, rate limiting, counters)
- **Object Storage** — media files (S3-compatible; metadata only tracked in PostgreSQL)

This document does not cover:

- Application-layer business logic or service implementation
- API contract definitions (see separate API Design Document)
- Infrastructure provisioning or Kubernetes manifests
- Frontend data models or state management

### 1.3 Database Technology Choice

| Concern | Technology | Rationale |
|---|---|---|
| Primary persistence | PostgreSQL 16+ | ACID guarantees, rich type system, full-text search, JSON support, mature ecosystem |
| Caching and sessions | Redis 7+ | Sub-millisecond latency, native TTL support, atomic operations (INCR, ZADD), ideal for transient social data |
| Media storage | S3-compatible Object Storage | Blob storage is not suited for relational DBs; offloads bandwidth, enables CDN distribution |
| ORM | Hibernate / JPA (Spring Boot) | Java 21 compatibility, proven at scale, first-class PostgreSQL dialect support |
| Migration tool | Flyway | Versioned, checksum-validated migrations; integrates with Spring Boot auto-configuration |
| Connection pool | HikariCP | Lowest-latency JDBC pool; Spring Boot default; minimal overhead per connection |

PostgreSQL was selected over alternatives (MySQL, MongoDB) for the following reasons:

- **Referential integrity**: FK constraints enforce social graph consistency at the database level
- **ENUM types**: Native support simplifies post_type, notification_type, and role modeling
- **Full-text search**: Built-in `tsvector`/`tsquery` eliminates need for a separate search engine at MVP scale
- **UUID support**: Native `uuid` data type with index efficiency
- **JSONB**: Available for future extensibility (e.g., post metadata, notification payloads) without schema changes

### 1.4 Conventions Used in This Document

- **Table names**: lowercase, `snake_case`, plural (e.g., `users`, `post_hashtags`)
- **Column names**: lowercase, `snake_case` (e.g., `created_at`, `user_id`)
- **Primary keys**: Always UUID, always named `id`
- **Foreign keys**: Named `{referenced_table_singular}_id` (e.g., `user_id`, `post_id`)
- **Timestamps**: All timestamp columns use `TIMESTAMPTZ` (timestamp with time zone), stored in UTC
- **Soft delete**: `deleted_at TIMESTAMPTZ NULL` — when NULL, record is active; when set, record is soft-deleted
- **Boolean defaults**: Explicitly stated (`DEFAULT FALSE` or `DEFAULT TRUE`)
- **ENUM types**: Named in `snake_case` with `_type` or `_status` suffix (e.g., `post_type`, `report_status`)
- **SQL code blocks**: All DDL uses uppercase keywords for SQL reserved words; identifiers remain lowercase

---

## 2. Database Architecture Overview

### 2.1 Database Components

Boondi uses a three-tier storage architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                           │
│              (Java 21 + Spring Boot + Hibernate)                │
└────────────────────────┬───────────────┬────────────────────────┘
                         │               │
           ┌─────────────▼──┐    ┌───────▼──────────┐
           │   PostgreSQL   │    │      Redis        │
           │   (Primary)    │    │   (Cache/Queue)   │
           │                │    │                   │
           │ - Users        │    │ - Token blacklist │
           │ - Posts        │    │ - Home timelines  │
           │ - Social graph │    │ - Session data    │
           │ - Notifications│    │ - Rate limiting   │
           │ - Auth tokens  │    │ - Trending cache  │
           │ - Reports      │    │ - Notif counts    │
           └────────────────┘    └───────────────────┘
                                          │
                              ┌───────────▼──────────┐
                              │   Object Storage      │
                              │   (S3-compatible)     │
                              │                       │
                              │ - Profile images      │
                              │ - Banner images       │
                              │ - Post media files    │
                              │ - Video files         │
                              └──────────────────────┘
```

**PostgreSQL** holds all authoritative application data. It is the system of record. No application state that requires durability is stored outside PostgreSQL.

**Redis** holds derived, short-lived, or reconstructible data. If Redis is flushed entirely, the application remains functional — it will be slower until caches warm up, but data will not be lost.

**Object Storage** holds binary media files. PostgreSQL stores only the `storage_key` (the object key/path in the bucket) and a publicly accessible `media_url` (CDN or pre-signed URL). The database never stores binary content.

### 2.2 Data Flow Overview

**User Registration Flow**:
```
Client → API → users table (INSERT) → email_verifications table (INSERT)
             → Redis: no cache entry yet (first access is cold)
```

**Post Creation Flow**:
```
Client → API → posts table (INSERT)
             → media table (INSERT, if attachments)
             → post_hashtags table (INSERT, if hashtags detected)
             → hashtags table (UPSERT)
             → notifications table (INSERT, for mentions)
             → Redis: invalidate timeline:home:{followerId} for all followers
```

**Home Timeline Flow**:
```
Client → API → Redis: GET timeline:home:{userId}
                 HIT  → return cached post IDs → fetch post details from PostgreSQL
                 MISS → query PostgreSQL (follows JOIN posts) → cache result → return
```

**Like Flow**:
```
Client → API → likes table (INSERT)
             → posts.like_count (UPDATE via trigger or application)
             → notifications table (INSERT for post author)
             → Redis: INCR notif:unread:{postAuthorId}
```

### 2.3 Connection Strategy

Boondi uses **HikariCP** as the JDBC connection pool, auto-configured by Spring Boot.

**HikariCP Configuration** (`application.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: BoondiHikariPool
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000        # 5 minutes
      connection-timeout: 30000   # 30 seconds
      max-lifetime: 1800000       # 30 minutes
      keepalive-time: 60000       # 1 minute
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1
```

**Sizing rationale**:

- `maximum-pool-size: 20` — appropriate for 10–20 active users at MVP; PostgreSQL default `max_connections` is 100, leaving headroom for admin connections and future growth
- `minimum-idle: 5` — keeps 5 warm connections during low-traffic periods without exhausting DB resources
- `max-lifetime: 1800000` — connections are recycled every 30 minutes to prevent stale connections and respect any firewall idle timeouts

**Redis Connection** (Lettuce client, Spring Data Redis):

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6379
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2
```

---

## 3. Entity Relationship Diagram

### 3.1 Entity Relationship Overview

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        BOONDI — ENTITY RELATIONSHIP DIAGRAM                  │
└──────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │     users       │
                    │─────────────────│
                    │ PK id (UUID)    │
                    │    username     │
                    │    email        │
                    │    role         │
                    │    ...          │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────────────────────┐
         │                   │                                   │
         │ 1:1               │ 1:N                               │ 1:N
         │                   │                                   │
         ▼                   ▼                                   ▼
  (profile fields      ┌──────────┐                    ┌─────────────────┐
   embedded in         │  posts   │                    │  notifications  │
   users table)        │──────────│                    │─────────────────│
                       │ PK id    │◄──────────────────►│ recipient_id FK │
                       │ user_id  │  (1:N via          │ actor_id FK     │
                       │ content  │   post_id)         │ post_id FK      │
                       │ type     │                    └─────────────────┘
                       │ ...      │
                       └────┬─────┘
                            │
         ┌──────────────────┼──────────────────────────┐
         │                  │                          │
         │ 1:N              │ 1:N                      │ M:N
         │                  │                          │
         ▼                  ▼                          ▼
    ┌─────────┐        ┌──────────┐            ┌──────────────┐
    │  media  │        │ replies  │            │  post_       │
    │─────────│        │ (posts   │            │  hashtags    │
    │ post_id │        │  where   │            │──────────────│
    │ user_id │        │ parent_  │            │ PK post_id   │
    │ type    │        │ post_id  │            │ PK hashtag_id│
    │ url     │        │ NOT NULL)│            └──────┬───────┘
    └─────────┘        └──────────┘                   │
                           ▲                           │
                           │ self-ref (parent_post_id) │ N:1
                           │                           ▼
                        ┌──┘                    ┌──────────┐
                        │                       │ hashtags │
                        │                       │──────────│
                   ┌────┴──────────────────────►│ PK id    │
                   │                            │ name     │
                   │                            └──────────┘
         ┌─────────┴────────┐
         │                  │
         ▼                  ▼
    ┌──────────┐      ┌──────────┐
    │  likes   │      │bookmarks │
    │──────────│      │──────────│
    │ user_id  │      │ user_id  │
    │ post_id  │      │ post_id  │
    └──────────┘      └──────────┘

         ┌─────────────────────┐
         │       follows       │  (M:N self-referential on users)
         │─────────────────────│
         │ follower_id → users │
         │ following_id → users│
         └─────────────────────┘
```

### 3.2 Relationship Summary

| Relationship | Cardinality | Implementation |
|---|---|---|
| users → posts | 1:N | `posts.user_id` FK → `users.id` |
| users → profile | 1:1 | Profile fields embedded directly in `users` table |
| posts → replies | 1:N (self-referential) | `posts.parent_post_id` FK → `posts.id` |
| posts → quotes | 1:N (self-referential) | `posts.quoted_post_id` FK → `posts.id` |
| users → follows | M:N (self-referential) | `follows` junction table (`follower_id`, `following_id`) |
| users → likes | M:N | `likes` junction table (`user_id`, `post_id`) |
| posts → likes | M:N | `likes` junction table (`user_id`, `post_id`) |
| posts → media | 1:N | `media.post_id` FK → `posts.id` |
| posts → hashtags | M:N | `post_hashtags` junction table |
| users → bookmarks | M:N | `bookmarks` junction table (`user_id`, `post_id`) |
| users → notifications | 1:N | `notifications.recipient_id` FK → `users.id` |
| users → refresh_tokens | 1:N | `refresh_tokens.user_id` FK → `users.id` |
| users → reports | M:N (as reporter / reported) | `reports` table with two nullable FKs |

---

## 4. Table Definitions

### 4.1 `users`

**Description**: Central entity representing every person who can log in to Boondi. Profile information (bio, avatar, banner) is co-located in this table to avoid a separate join on every profile fetch. Soft-delete is supported via `deleted_at`.

**DDL**:

```sql
CREATE TABLE users (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(50)     NOT NULL UNIQUE,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password_hash       VARCHAR(255)    NOT NULL,
    display_name        VARCHAR(100),
    bio                 TEXT,
    profile_picture_url TEXT,
    banner_image_url    TEXT,
    role                user_role       NOT NULL DEFAULT 'USER',
    is_verified         BOOLEAN         NOT NULL DEFAULT FALSE,
    is_suspended        BOOLEAN         NOT NULL DEFAULT FALSE,
    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    follower_count      INTEGER         NOT NULL DEFAULT 0,
    following_count     INTEGER         NOT NULL DEFAULT 0,
    post_count          INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ     NULL
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key; UUIDs prevent enumeration attacks |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | Public handle (e.g., `@alice`); lowercase enforced at application layer |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Login credential; treated as PII |
| `password_hash` | `VARCHAR(255)` | NOT NULL | BCrypt hash (60 chars); plaintext never stored |
| `display_name` | `VARCHAR(100)` | NULL | User's chosen display name; differs from username |
| `bio` | `TEXT` | NULL | Short user biography; displayed on profile |
| `profile_picture_url` | `TEXT` | NULL | CDN URL to profile picture stored in Object Storage |
| `banner_image_url` | `TEXT` | NULL | CDN URL to profile banner image in Object Storage |
| `role` | `user_role` (ENUM) | NOT NULL, DEFAULT `'USER'` | `USER` or `ADMIN`; controls access to admin endpoints |
| `is_verified` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Blue-check / account verification badge |
| `is_suspended` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Admin-set suspension flag; suspended users cannot post or interact |
| `email_verified` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Set to TRUE after email verification flow completes |
| `follower_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized counter; maintained by trigger |
| `following_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized counter; maintained by trigger |
| `post_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized counter; maintained by trigger |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Account creation timestamp (UTC) |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Last profile update timestamp; updated by trigger |
| `deleted_at` | `TIMESTAMPTZ` | NULL | Soft-delete marker; NULL means active account |

**Indexes**:

```sql
CREATE UNIQUE INDEX idx_users_username ON users (username);
CREATE UNIQUE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_created_at      ON users (created_at DESC);
CREATE INDEX idx_users_active          ON users (id) WHERE deleted_at IS NULL;
```

**Constraints**:

```sql
ALTER TABLE users ADD CONSTRAINT chk_users_username_length
    CHECK (char_length(username) >= 3);

ALTER TABLE users ADD CONSTRAINT chk_users_follower_count_positive
    CHECK (follower_count >= 0);

ALTER TABLE users ADD CONSTRAINT chk_users_following_count_positive
    CHECK (following_count >= 0);

ALTER TABLE users ADD CONSTRAINT chk_users_post_count_positive
    CHECK (post_count >= 0);
```

---

### 4.2 `posts`

**Description**: The core content entity. Supports original posts, replies (threaded via `parent_post_id`), reposts, and quote-posts (via `quoted_post_id`). Soft-delete via `deleted_at`. Counter caches (`like_count`, `reply_count`, etc.) are denormalized for read performance.

**DDL**:

```sql
CREATE TABLE posts (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         VARCHAR(500)    NOT NULL,
    post_type       post_type       NOT NULL DEFAULT 'ORIGINAL',
    parent_post_id  UUID            NULL REFERENCES posts(id) ON DELETE SET NULL,
    quoted_post_id  UUID            NULL REFERENCES posts(id) ON DELETE SET NULL,
    like_count      INTEGER         NOT NULL DEFAULT 0,
    reply_count     INTEGER         NOT NULL DEFAULT 0,
    repost_count    INTEGER         NOT NULL DEFAULT 0,
    bookmark_count  INTEGER         NOT NULL DEFAULT 0,
    is_pinned       BOOLEAN         NOT NULL DEFAULT FALSE,
    search_vector   TSVECTOR,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ     NULL
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | Author of the post |
| `content` | `VARCHAR(500)` | NOT NULL | Post body text; max 500 characters enforced by DB |
| `post_type` | `post_type` (ENUM) | NOT NULL, DEFAULT `'ORIGINAL'` | One of: `ORIGINAL`, `REPLY`, `REPOST`, `QUOTE` |
| `parent_post_id` | `UUID` | NULL, FK → `posts.id` ON DELETE SET NULL | Set when `post_type = 'REPLY'`; references the parent post |
| `quoted_post_id` | `UUID` | NULL, FK → `posts.id` ON DELETE SET NULL | Set when `post_type = 'QUOTE'`; references the quoted post |
| `like_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized count; updated by trigger |
| `reply_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized count; updated by trigger |
| `repost_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized count; updated by trigger |
| `bookmark_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized count; updated by trigger |
| `is_pinned` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Pinned to author's profile top; only one post per user should be pinned (enforced at app layer) |
| `search_vector` | `TSVECTOR` | NULL | Precomputed full-text search vector; maintained by trigger |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Post creation timestamp (UTC) |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Last edit timestamp |
| `deleted_at` | `TIMESTAMPTZ` | NULL | Soft-delete marker |

**Indexes**:

```sql
CREATE INDEX idx_posts_user_id        ON posts (user_id, created_at DESC);
CREATE INDEX idx_posts_parent_post_id ON posts (parent_post_id) WHERE parent_post_id IS NOT NULL;
CREATE INDEX idx_posts_created_at     ON posts (created_at DESC);
CREATE INDEX idx_posts_post_type      ON posts (post_type);
CREATE INDEX idx_posts_active         ON posts (user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_posts_search_vector  ON posts USING GIN (search_vector);
```

**Constraints**:

```sql
ALTER TABLE posts ADD CONSTRAINT chk_posts_content_not_empty
    CHECK (char_length(trim(content)) > 0);

ALTER TABLE posts ADD CONSTRAINT chk_posts_content_max_length
    CHECK (char_length(content) <= 500);

ALTER TABLE posts ADD CONSTRAINT chk_posts_reply_has_parent
    CHECK (post_type != 'REPLY' OR parent_post_id IS NOT NULL);

ALTER TABLE posts ADD CONSTRAINT chk_posts_quote_has_quoted
    CHECK (post_type != 'QUOTE' OR quoted_post_id IS NOT NULL);

ALTER TABLE posts ADD CONSTRAINT chk_posts_no_self_reply
    CHECK (parent_post_id != id);

ALTER TABLE posts ADD CONSTRAINT chk_posts_no_self_quote
    CHECK (quoted_post_id != id);

ALTER TABLE posts ADD CONSTRAINT chk_posts_counts_positive
    CHECK (like_count >= 0 AND reply_count >= 0 AND repost_count >= 0 AND bookmark_count >= 0);
```

---

### 4.3 `media`

**Description**: Tracks media attachments (images, videos) associated with posts. The actual binary files reside in Object Storage; this table stores metadata and the storage key needed to construct or fetch the URL.

**DDL**:

```sql
CREATE TABLE media (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id         UUID            NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id         UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type      media_type      NOT NULL,
    storage_key     TEXT            NOT NULL,
    media_url       TEXT            NOT NULL,
    file_size       BIGINT          NULL,
    width           INTEGER         NULL,
    height          INTEGER         NULL,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `post_id` | `UUID` | NOT NULL, FK → `posts.id` ON DELETE CASCADE | The post this media belongs to |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | Uploader; denormalized for access control checks without joining posts |
| `media_type` | `media_type` (ENUM) | NOT NULL | One of: `IMAGE`, `VIDEO` |
| `storage_key` | `TEXT` | NOT NULL | Object key in the storage bucket (e.g., `media/user-id/post-id/filename.jpg`) |
| `media_url` | `TEXT` | NOT NULL | Publicly accessible URL (CDN or pre-signed); may be refreshed periodically |
| `file_size` | `BIGINT` | NULL | File size in bytes; used for quota enforcement |
| `width` | `INTEGER` | NULL | Image/video width in pixels; populated by media processing pipeline |
| `height` | `INTEGER` | NULL | Image/video height in pixels; populated by media processing pipeline |
| `display_order` | `INTEGER` | NOT NULL, DEFAULT `0` | Controls display order when multiple media items are in one post (0-indexed) |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Upload timestamp |

**Indexes**:

```sql
CREATE INDEX idx_media_post_id ON media (post_id, display_order);
CREATE INDEX idx_media_user_id ON media (user_id);
```

**Constraints**:

```sql
ALTER TABLE media ADD CONSTRAINT chk_media_file_size_positive
    CHECK (file_size IS NULL OR file_size > 0);

ALTER TABLE media ADD CONSTRAINT chk_media_dimensions_positive
    CHECK (
        (width IS NULL AND height IS NULL) OR
        (width > 0 AND height > 0)
    );

ALTER TABLE media ADD CONSTRAINT chk_media_display_order_positive
    CHECK (display_order >= 0);
```

---

### 4.4 `follows`

**Description**: Models the directional follow relationship between users. `follower_id` is the user who clicked "Follow"; `following_id` is the user being followed. A composite unique constraint prevents duplicate follow relationships. A check constraint prevents self-follows.

**DDL**:

```sql
CREATE TABLE follows (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id    UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_follows_pair UNIQUE (follower_id, following_id)
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `follower_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | The user initiating the follow |
| `following_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | The user being followed |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Timestamp when the follow occurred |

**Indexes**:

```sql
CREATE INDEX idx_follows_follower_id  ON follows (follower_id);
CREATE INDEX idx_follows_following_id ON follows (following_id);
```

**Constraints**:

```sql
ALTER TABLE follows ADD CONSTRAINT chk_follows_no_self_follow
    CHECK (follower_id != following_id);
```

**Trigger**: On INSERT into `follows`, increment `users.following_count` for `follower_id` and `users.follower_count` for `following_id`. On DELETE, decrement both. (See Section 6.5.)

---

### 4.5 `likes`

**Description**: Records which users have liked which posts. Acts as a M:N junction between `users` and `posts`. A composite unique constraint ensures a user can like a post exactly once.

**DDL**:

```sql
CREATE TABLE likes (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id     UUID            NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_likes_user_post UNIQUE (user_id, post_id)
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | User who liked the post |
| `post_id` | `UUID` | NOT NULL, FK → `posts.id` ON DELETE CASCADE | Post that was liked |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Timestamp of the like |

**Indexes**:

```sql
CREATE INDEX idx_likes_post_id ON likes (post_id);
CREATE INDEX idx_likes_user_id ON likes (user_id);
```

**Trigger**: On INSERT into `likes`, increment `posts.like_count`. On DELETE, decrement `posts.like_count`. (See Section 6.5.)

---

### 4.6 `bookmarks`

**Description**: Records private bookmarks that users save for later reading. Bookmarks are user-private and not visible to other users. A composite unique constraint prevents duplicate bookmarks.

**DDL**:

```sql
CREATE TABLE bookmarks (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id     UUID            NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_bookmarks_user_post UNIQUE (user_id, post_id)
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | User who bookmarked the post |
| `post_id` | `UUID` | NOT NULL, FK → `posts.id` ON DELETE CASCADE | Post that was bookmarked |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Timestamp when bookmark was added |

**Indexes**:

```sql
CREATE INDEX idx_bookmarks_user_id ON bookmarks (user_id, created_at DESC);
CREATE INDEX idx_bookmarks_post_id ON bookmarks (post_id);
```

**Trigger**: On INSERT into `bookmarks`, increment `posts.bookmark_count`. On DELETE, decrement `posts.bookmark_count`. (See Section 6.5.)

---

### 4.7 `notifications`

**Description**: Stores in-app notifications delivered to users when social events occur (likes, replies, follows, mentions, reposts, quotes). `recipient_id` is who receives the notification; `actor_id` is who triggered it. `post_id` is contextually optional (e.g., not needed for follow notifications).

**DDL**:

```sql
CREATE TABLE notifications (
    id                  UUID                    PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id        UUID                    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id            UUID                    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type   notification_type       NOT NULL,
    post_id             UUID                    NULL REFERENCES posts(id) ON DELETE CASCADE,
    is_read             BOOLEAN                 NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `recipient_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | User who will see the notification |
| `actor_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | User whose action triggered the notification |
| `notification_type` | `notification_type` (ENUM) | NOT NULL | One of: `LIKE`, `REPLY`, `FOLLOW`, `MENTION`, `REPOST`, `QUOTE` |
| `post_id` | `UUID` | NULL, FK → `posts.id` ON DELETE CASCADE | Related post (NULL for `FOLLOW` notifications) |
| `is_read` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Whether recipient has viewed this notification |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When the notification was generated |

**Indexes**:

```sql
CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_id, is_read, created_at DESC);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_id, created_at DESC);
```

**Constraints**:

```sql
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_no_self_notify
    CHECK (recipient_id != actor_id);

ALTER TABLE notifications ADD CONSTRAINT chk_notifications_post_required
    CHECK (
        notification_type = 'FOLLOW' OR post_id IS NOT NULL
    );
```

---

### 4.8 `hashtags`

**Description**: Canonical registry of all hashtags used across the platform. Each hashtag is stored once (lowercase, without the `#` prefix). The `post_count` is a denormalized counter updated by triggers on `post_hashtags`.

**DDL**:

```sql
CREATE TABLE hashtags (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL UNIQUE,
    post_count  INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `name` | `VARCHAR(100)` | NOT NULL, UNIQUE | Hashtag text, lowercase, without `#` (e.g., `photography`, `boondi`) |
| `post_count` | `INTEGER` | NOT NULL, DEFAULT `0` | Denormalized count of active posts using this hashtag |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When this hashtag was first used on the platform |

**Indexes**:

```sql
CREATE UNIQUE INDEX idx_hashtags_name ON hashtags (name);
CREATE INDEX idx_hashtags_post_count  ON hashtags (post_count DESC);
```

**Constraints**:

```sql
ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_name_lowercase
    CHECK (name = lower(name));

ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_name_no_hash
    CHECK (name NOT LIKE '#%');

ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_name_not_empty
    CHECK (char_length(trim(name)) > 0);

ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_post_count_positive
    CHECK (post_count >= 0);
```

---

### 4.9 `post_hashtags`

**Description**: M:N junction table linking posts to hashtags. The composite primary key `(post_id, hashtag_id)` is both the PK and the natural unique constraint, ensuring a post cannot be tagged with the same hashtag twice.

**DDL**:

```sql
CREATE TABLE post_hashtags (
    post_id     UUID    NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    hashtag_id  UUID    NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,

    PRIMARY KEY (post_id, hashtag_id)
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `post_id` | `UUID` | PK (composite), NOT NULL, FK → `posts.id` ON DELETE CASCADE | The post being tagged |
| `hashtag_id` | `UUID` | PK (composite), NOT NULL, FK → `hashtags.id` ON DELETE CASCADE | The hashtag applied to the post |

**Indexes**:

```sql
-- The composite PK creates an index on (post_id, hashtag_id) automatically.
-- An additional index on hashtag_id alone enables "all posts with hashtag X" queries.
CREATE INDEX idx_post_hashtags_hashtag_id ON post_hashtags (hashtag_id);
```

**Trigger**: On INSERT into `post_hashtags`, increment `hashtags.post_count`. On DELETE, decrement `hashtags.post_count`. (See Section 6.5.)

---

### 4.10 `refresh_tokens`

**Description**: Stores hashed refresh tokens for JWT authentication. Boondi uses stateless access tokens (short-lived, not stored in DB) and stateful refresh tokens (long-lived, stored here as a hash). Tokens are revocable by setting `is_revoked = TRUE` or deleting the row.

**DDL**:

```sql
CREATE TABLE refresh_tokens (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255)    NOT NULL,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_revoked  BOOLEAN         NOT NULL DEFAULT FALSE,
    device_info TEXT            NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | Owner of the refresh token |
| `token_hash` | `VARCHAR(255)` | NOT NULL | SHA-256 hash of the actual token value; raw token never persisted |
| `expires_at` | `TIMESTAMPTZ` | NOT NULL | Absolute expiry time; tokens past this timestamp are invalid regardless of `is_revoked` |
| `is_revoked` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Explicit revocation (logout, security event); checked before expiry |
| `device_info` | `TEXT` | NULL | User-agent / device identifier for multi-device session management |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When the token was issued |

**Indexes**:

```sql
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at)
    WHERE is_revoked = FALSE;
```

---

### 4.11 `password_reset_tokens`

**Description**: One-time tokens issued when a user requests a password reset via email. Each token is stored as a hash; the plaintext is emailed to the user and never stored. After use, `is_used` is set to TRUE.

**DDL**:

```sql
CREATE TABLE password_reset_tokens (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255)    NOT NULL,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | Account this token is for |
| `token_hash` | `VARCHAR(255)` | NOT NULL | SHA-256 hash of the token sent to the user's email |
| `expires_at` | `TIMESTAMPTZ` | NOT NULL | Token expiry (typically 1 hour from issue) |
| `is_used` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Set to TRUE on successful password reset; prevents replay |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When the reset was requested |

**Indexes**:

```sql
CREATE INDEX idx_prt_user_id    ON password_reset_tokens (user_id);
CREATE INDEX idx_prt_token_hash ON password_reset_tokens (token_hash);
```

---

### 4.12 `email_verifications`

**Description**: Stores tokens for the email verification flow that activates a new account. After a user clicks the verification link, `is_used` is set to TRUE and `users.email_verified` is set to TRUE in the same transaction.

**DDL**:

```sql
CREATE TABLE email_verifications (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255)    NOT NULL,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `user_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | Account awaiting verification |
| `token_hash` | `VARCHAR(255)` | NOT NULL | SHA-256 hash of the token sent via email |
| `expires_at` | `TIMESTAMPTZ` | NOT NULL | Token expiry (typically 24 hours from issue) |
| `is_used` | `BOOLEAN` | NOT NULL, DEFAULT `FALSE` | Set to TRUE after successful verification |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When the verification email was sent |

**Indexes**:

```sql
CREATE INDEX idx_ev_user_id    ON email_verifications (user_id);
CREATE INDEX idx_ev_token_hash ON email_verifications (token_hash);
```

---

### 4.13 `reports`

**Description**: Allows users to report abusive or policy-violating content to admins. A report can target either a user (`reported_user_id`) or a post (`reported_post_id`), or both. Admins review reports and update the `status`.

**DDL**:

```sql
CREATE TABLE reports (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id         UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id    UUID            NULL REFERENCES users(id) ON DELETE SET NULL,
    reported_post_id    UUID            NULL REFERENCES posts(id) ON DELETE SET NULL,
    reason              TEXT            NOT NULL,
    status              report_status   NOT NULL DEFAULT 'PENDING',
    reviewed_by         UUID            NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

**Column Definitions**:

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PK, DEFAULT `gen_random_uuid()` | Surrogate primary key |
| `reporter_id` | `UUID` | NOT NULL, FK → `users.id` ON DELETE CASCADE | User submitting the report |
| `reported_user_id` | `UUID` | NULL, FK → `users.id` ON DELETE SET NULL | Reported user (NULL if report is only against a post) |
| `reported_post_id` | `UUID` | NULL, FK → `posts.id` ON DELETE SET NULL | Reported post (NULL if report is only against a user) |
| `reason` | `TEXT` | NOT NULL | Reporter-provided explanation of the issue |
| `status` | `report_status` (ENUM) | NOT NULL, DEFAULT `'PENDING'` | One of: `PENDING`, `REVIEWED`, `RESOLVED`, `DISMISSED` |
| `reviewed_by` | `UUID` | NULL, FK → `users.id` ON DELETE SET NULL | Admin who reviewed the report |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | When the report was submitted |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `NOW()` | Last status update |

**Indexes**:

```sql
CREATE INDEX idx_reports_status      ON reports (status, created_at DESC);
CREATE INDEX idx_reports_reporter_id ON reports (reporter_id);
CREATE INDEX idx_reports_reviewed_by ON reports (reviewed_by) WHERE reviewed_by IS NOT NULL;
```

**Constraints**:

```sql
ALTER TABLE reports ADD CONSTRAINT chk_reports_has_target
    CHECK (reported_user_id IS NOT NULL OR reported_post_id IS NOT NULL);

ALTER TABLE reports ADD CONSTRAINT chk_reports_no_self_report
    CHECK (reporter_id != reported_user_id);

ALTER TABLE reports ADD CONSTRAINT chk_reports_reason_not_empty
    CHECK (char_length(trim(reason)) > 0);
```

---

## 5. Indexes Strategy

### 5.1 Index Overview

Indexes in Boondi are designed around the most frequent query patterns: timeline reads, notification fetches, social graph traversals, and full-text search. Primary keys are always indexed automatically by PostgreSQL.

### 5.2 Index Definitions by Table

| Table | Index Name | Columns | Type | Purpose |
|---|---|---|---|---|
| `users` | `idx_users_username` | `username` | B-tree (UNIQUE) | Login and profile lookup by handle |
| `users` | `idx_users_email` | `email` | B-tree (UNIQUE) | Login by email |
| `users` | `idx_users_created_at` | `created_at DESC` | B-tree | Admin user list, sorted by join date |
| `users` | `idx_users_active` | `id` WHERE `deleted_at IS NULL` | Partial B-tree | Filter active users without full table scan |
| `posts` | `idx_posts_user_id` | `(user_id, created_at DESC)` | Composite B-tree | User profile timeline: all posts by a user, newest first |
| `posts` | `idx_posts_parent_post_id` | `parent_post_id` WHERE NOT NULL | Partial B-tree | Fetch all replies to a post |
| `posts` | `idx_posts_created_at` | `created_at DESC` | B-tree | Global/explore feed |
| `posts` | `idx_posts_post_type` | `post_type` | B-tree | Filter by post type |
| `posts` | `idx_posts_active` | `(user_id, created_at DESC)` WHERE `deleted_at IS NULL` | Partial composite B-tree | Active posts only; avoids soft-deleted content |
| `posts` | `idx_posts_search_vector` | `search_vector` | GIN | Full-text search |
| `likes` | `idx_likes_post_id` | `post_id` | B-tree | All likes on a post (count verification) |
| `likes` | `idx_likes_user_id` | `user_id` | B-tree | All posts liked by a user |
| `follows` | `idx_follows_follower_id` | `follower_id` | B-tree | "Who does this user follow?" (home timeline query) |
| `follows` | `idx_follows_following_id` | `following_id` | B-tree | "Who follows this user?" (follower list) |
| `notifications` | `idx_notifications_recipient_unread` | `(recipient_id, is_read, created_at DESC)` | Composite B-tree | Unread notification badge count and list |
| `notifications` | `idx_notifications_recipient_created` | `(recipient_id, created_at DESC)` | Composite B-tree | Full notification history |
| `hashtags` | `idx_hashtags_name` | `name` | B-tree (UNIQUE) | Hashtag lookup by name |
| `hashtags` | `idx_hashtags_post_count` | `post_count DESC` | B-tree | Trending hashtag ordering |
| `post_hashtags` | (PK index) | `(post_id, hashtag_id)` | B-tree | "Which hashtags does post X have?" |
| `post_hashtags` | `idx_post_hashtags_hashtag_id` | `hashtag_id` | B-tree | "Which posts use hashtag X?" |
| `refresh_tokens` | `idx_refresh_tokens_user_id` | `user_id` | B-tree | Revoke all tokens for a user on logout/suspend |
| `refresh_tokens` | `idx_refresh_tokens_token_hash` | `token_hash` | B-tree | Token lookup during refresh grant |
| `refresh_tokens` | `idx_refresh_tokens_expires_at` | `expires_at` WHERE `is_revoked = FALSE` | Partial B-tree | Cleanup job: delete expired active tokens |

### 5.3 Composite Index Rationale for Timeline Queries

The home timeline query — "show me recent posts from users I follow" — is the most read-heavy operation on the platform. It follows this pattern:

```sql
SELECT p.*
FROM   posts p
JOIN   follows f ON p.user_id = f.following_id
WHERE  f.follower_id = :currentUserId
  AND  p.deleted_at IS NULL
ORDER  BY p.created_at DESC
LIMIT  20 OFFSET :offset;
```

This query benefits from two indexes working together:

1. **`idx_follows_follower_id` on `follows(follower_id)`**: Allows PostgreSQL to quickly retrieve the set of `following_id` values for the current user without scanning the full `follows` table.

2. **`idx_posts_active` on `posts(user_id, created_at DESC) WHERE deleted_at IS NULL`**: A composite partial index. Because `user_id` is the leading column and `created_at DESC` is the secondary column, PostgreSQL can perform an index scan that simultaneously filters by author AND delivers results in reverse chronological order — eliminating an explicit sort.

The composite column ordering `(user_id, created_at DESC)` matters: B-tree indexes in PostgreSQL sort on the leading column first. Placing `created_at DESC` second means for any given `user_id`, posts are already sorted, enabling efficient `LIMIT` operations.

At scale (10,000+ users), this query pattern transitions to a **fan-out-on-write** approach where the timeline is precomputed in Redis, and the SQL query becomes a fallback for cache misses only.

---

## 6. PostgreSQL-Specific Features

### 6.1 UUID Generation

Boondi uses PostgreSQL's built-in `gen_random_uuid()` function (available in PostgreSQL 13+ via the `pgcrypto` extension or natively in PG 14+). This generates cryptographically random UUID v4 values.

```sql
-- Enable extension (required for PostgreSQL < 14)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- All tables use this as the default:
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

Alternatively, for PostgreSQL 13 compatibility:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Then use:
id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
```

**Recommendation**: Use `gen_random_uuid()` on PostgreSQL 14+. It is built-in, requires no extension, and is marginally faster.

**Java / Hibernate note**: Spring Boot with Hibernate should be configured to let the database generate UUIDs, not the application:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id", updatable = false, nullable = false)
private UUID id;
```

### 6.2 ENUM Types

All enumerated columns use custom PostgreSQL `TYPE` definitions rather than `VARCHAR` or `INTEGER` codes. This enforces data integrity at the database level and makes the schema self-documenting.

```sql
-- User roles
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

-- Post variants
CREATE TYPE post_type AS ENUM ('ORIGINAL', 'REPLY', 'REPOST', 'QUOTE');

-- Notification events
CREATE TYPE notification_type AS ENUM (
    'LIKE',
    'REPLY',
    'FOLLOW',
    'MENTION',
    'REPOST',
    'QUOTE'
);

-- Media file types
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO');

-- Report lifecycle states
CREATE TYPE report_status AS ENUM (
    'PENDING',
    'REVIEWED',
    'RESOLVED',
    'DISMISSED'
);
```

**Important**: Adding new values to a PostgreSQL ENUM requires `ALTER TYPE ... ADD VALUE`. This is a low-overhead operation in PostgreSQL 12+ and does not require a table rewrite. It must be reflected in a Flyway migration.

**Hibernate mapping**:

```java
@Enumerated(EnumType.STRING)
@Column(name = "role", nullable = false)
private UserRole role = UserRole.USER;
```

### 6.3 Full-Text Search Setup

Boondi implements full-text search on post content using PostgreSQL's native `tsvector` and `tsquery` system, which provides ranking and stemming without a separate search engine.

**Step 1**: Add the `search_vector` column (already in the `posts` DDL above).

**Step 2**: Create a trigger to auto-populate `search_vector` on insert and update:

```sql
CREATE OR REPLACE FUNCTION posts_search_vector_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector('english', coalesce(NEW.content, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_posts_search_vector_update
BEFORE INSERT OR UPDATE OF content
ON posts
FOR EACH ROW EXECUTE FUNCTION posts_search_vector_update();
```

**Step 3**: Create a GIN index for fast FTS queries:

```sql
CREATE INDEX idx_posts_search_vector ON posts USING GIN (search_vector);
```

**Step 4**: Query pattern with ranking:

```sql
SELECT
    p.id,
    p.content,
    p.created_at,
    ts_rank(p.search_vector, query) AS rank
FROM
    posts p,
    plainto_tsquery('english', :searchTerm) AS query
WHERE
    p.search_vector @@ query
  AND p.deleted_at IS NULL
ORDER BY
    rank DESC,
    p.created_at DESC
LIMIT 20;
```

**Phrase search** (exact phrase matching):

```sql
-- Use phraseto_tsquery for phrase matching
WHERE p.search_vector @@ phraseto_tsquery('english', :phrase)
```

**Hashtag search**: Hashtags are searched via the `hashtags` table (exact match on `name`), not via FTS.

### 6.4 Constraints and Check Constraints

Beyond the constraints listed per-table, the following summarizes the critical business rules enforced at the database layer:

```sql
-- Prevent empty or whitespace-only post content
ALTER TABLE posts ADD CONSTRAINT chk_posts_content_not_empty
    CHECK (char_length(trim(content)) > 0);

-- Enforce 500-character limit (belt-and-suspenders alongside VARCHAR(500))
ALTER TABLE posts ADD CONSTRAINT chk_posts_content_max_length
    CHECK (char_length(content) <= 500);

-- Replies must have a parent
ALTER TABLE posts ADD CONSTRAINT chk_posts_reply_has_parent
    CHECK (post_type != 'REPLY' OR parent_post_id IS NOT NULL);

-- Quote posts must have a quoted post
ALTER TABLE posts ADD CONSTRAINT chk_posts_quote_has_quoted
    CHECK (post_type != 'QUOTE' OR quoted_post_id IS NOT NULL);

-- A post cannot be its own parent (no infinite recursion)
ALTER TABLE posts ADD CONSTRAINT chk_posts_no_self_reply
    CHECK (parent_post_id != id);

-- A user cannot follow themselves
ALTER TABLE follows ADD CONSTRAINT chk_follows_no_self_follow
    CHECK (follower_id != following_id);

-- A notification actor cannot be the same as the recipient (no self-notifications)
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_no_self_notify
    CHECK (recipient_id != actor_id);

-- A report must have at least one target
ALTER TABLE reports ADD CONSTRAINT chk_reports_has_target
    CHECK (reported_user_id IS NOT NULL OR reported_post_id IS NOT NULL);

-- Hashtag names are always lowercase, never prefixed with #
ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_name_lowercase
    CHECK (name = lower(name));
ALTER TABLE hashtags ADD CONSTRAINT chk_hashtags_name_no_hash
    CHECK (name NOT LIKE '#%');

-- Usernames must be at least 3 characters
ALTER TABLE users ADD CONSTRAINT chk_users_username_length
    CHECK (char_length(username) >= 3);
```

### 6.5 Triggers

Triggers maintain denormalized counter caches, update `updated_at` timestamps, and compute full-text search vectors automatically. This keeps counters consistent without requiring the application to manage them manually.

#### 6.5.1 `updated_at` Auto-Update Trigger

Applied to all tables with `updated_at`:

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER tg_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER tg_reports_updated_at
    BEFORE UPDATE ON reports
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

#### 6.5.2 Like Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_post_like_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE posts SET like_count = like_count + 1 WHERE id = NEW.post_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE posts SET like_count = GREATEST(like_count - 1, 0) WHERE id = OLD.post_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_likes_update_count
AFTER INSERT OR DELETE ON likes
FOR EACH ROW EXECUTE FUNCTION update_post_like_count();
```

#### 6.5.3 Bookmark Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_post_bookmark_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE posts SET bookmark_count = bookmark_count + 1 WHERE id = NEW.post_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE posts SET bookmark_count = GREATEST(bookmark_count - 1, 0) WHERE id = OLD.post_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_bookmarks_update_count
AFTER INSERT OR DELETE ON bookmarks
FOR EACH ROW EXECUTE FUNCTION update_post_bookmark_count();
```

#### 6.5.4 Reply Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_post_reply_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.post_type = 'REPLY' AND NEW.parent_post_id IS NOT NULL THEN
        UPDATE posts SET reply_count = reply_count + 1 WHERE id = NEW.parent_post_id;
    ELSIF TG_OP = 'DELETE' AND OLD.post_type = 'REPLY' AND OLD.parent_post_id IS NOT NULL THEN
        UPDATE posts SET reply_count = GREATEST(reply_count - 1, 0) WHERE id = OLD.parent_post_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_posts_reply_count
AFTER INSERT OR DELETE ON posts
FOR EACH ROW EXECUTE FUNCTION update_post_reply_count();
```

#### 6.5.5 Follow Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_follow_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE users SET following_count = following_count + 1 WHERE id = NEW.follower_id;
        UPDATE users SET follower_count  = follower_count  + 1 WHERE id = NEW.following_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE users SET following_count = GREATEST(following_count - 1, 0) WHERE id = OLD.follower_id;
        UPDATE users SET follower_count  = GREATEST(follower_count  - 1, 0) WHERE id = OLD.following_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_follows_update_counts
AFTER INSERT OR DELETE ON follows
FOR EACH ROW EXECUTE FUNCTION update_follow_counts();
```

#### 6.5.6 Post Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_user_post_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.deleted_at IS NULL THEN
        UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
    ELSIF TG_OP = 'UPDATE' THEN
        -- Soft delete: count drops when deleted_at is set
        IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
            UPDATE users SET post_count = GREATEST(post_count - 1, 0) WHERE id = NEW.user_id;
        -- Restore: count rises if deleted_at is cleared
        ELSIF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
            UPDATE users SET post_count = post_count + 1 WHERE id = NEW.user_id;
        END IF;
    ELSIF TG_OP = 'DELETE' AND OLD.deleted_at IS NULL THEN
        UPDATE users SET post_count = GREATEST(post_count - 1, 0) WHERE id = OLD.user_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_posts_update_user_count
AFTER INSERT OR UPDATE OF deleted_at OR DELETE ON posts
FOR EACH ROW EXECUTE FUNCTION update_user_post_count();
```

#### 6.5.7 Hashtag Post Count Trigger

```sql
CREATE OR REPLACE FUNCTION update_hashtag_post_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE hashtags SET post_count = post_count + 1 WHERE id = NEW.hashtag_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE hashtags SET post_count = GREATEST(post_count - 1, 0) WHERE id = OLD.hashtag_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_post_hashtags_update_count
AFTER INSERT OR DELETE ON post_hashtags
FOR EACH ROW EXECUTE FUNCTION update_hashtag_post_count();
```

---

## 7. Redis Schema

Redis stores ephemeral, high-frequency-read data that would be expensive to recompute from PostgreSQL on every request. All keys follow a consistent naming convention: `{domain}:{entity}:{identifier}`.

### 7.1 Refresh Token Blacklist

When a JWT access token is revoked (logout, security event), its identifier is placed in the blacklist so requests bearing that token are rejected even before expiry.

```
Key pattern:   blacklist:token:{tokenJti}
Type:          STRING (value: "1")
TTL:           Set to remaining token lifetime (seconds until exp claim)
Example key:   blacklist:token:550e8400-e29b-41d4-a716-446655440000
```

```bash
# On logout / token revocation:
SET blacklist:token:{jti} "1" EX {remainingTtlSeconds}

# On every authenticated request:
EXISTS blacklist:token:{jti}
# Returns 1 = token is blacklisted (reject), 0 = token is valid (proceed)
```

### 7.2 Home Timeline Cache

The home timeline (posts from followed users) is expensive to compute fresh on every request. It is cached as a sorted set scored by post creation timestamp.

```
Key pattern:   timeline:home:{userId}
Type:          ZSET (score = Unix timestamp of post creation, member = post UUID)
TTL:           300 seconds (5 minutes)
Example key:   timeline:home:550e8400-e29b-41d4-a716-446655440000
```

```bash
# Populate cache (fan-out on write — when a user posts):
ZADD timeline:home:{followerId} {createdAtUnixMs} {postId}
EXPIRE timeline:home:{followerId} 300

# Trim to last 200 entries to bound memory:
ZREMRANGEBYRANK timeline:home:{followerId} 0 -201

# Read timeline (newest 20 posts):
ZREVRANGEBYSCORE timeline:home:{userId} +inf -inf LIMIT 0 20

# Invalidate on follow/unfollow:
DEL timeline:home:{userId}
```

### 7.3 User Session Data

Caches frequently accessed user attributes to reduce PostgreSQL reads on every authenticated request. Refreshed when user updates their profile.

```
Key pattern:   session:{userId}
Type:          HASH
TTL:           3600 seconds (1 hour)
Fields:        username, display_name, role, is_suspended, email_verified, profile_picture_url
```

```bash
# Cache user data after authentication:
HSET session:{userId} \
    username "alice" \
    display_name "Alice Smith" \
    role "USER" \
    is_suspended "false" \
    email_verified "true" \
    profile_picture_url "https://cdn.boondi.app/media/alice-pfp.jpg"
EXPIRE session:{userId} 3600

# Read specific field:
HGET session:{userId} role

# Invalidate on profile update or suspension:
DEL session:{userId}
```

### 7.4 Rate Limiting

Per-endpoint, per-IP (or per-user) rate limiting using atomic increment operations.

```
Key pattern:   ratelimit:{identifier}:{endpoint}
Type:          STRING (integer counter)
TTL:           Window duration in seconds
Example:       ratelimit:192.168.1.1:POST:/api/posts
               ratelimit:user:{userId}:POST:/api/posts
```

```bash
# Increment counter, set TTL only on first increment (SETNX behavior):
local count = INCR ratelimit:{ip}:{endpoint}
if count == 1 then
    EXPIRE ratelimit:{ip}:{endpoint} 60   # 60-second window
end
# If count > limit, reject request with HTTP 429

# Lua script for atomic check-and-increment:
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local current = redis.call('INCR', key)
if current == 1 then
    redis.call('EXPIRE', key, window)
end
if current > limit then
    return 0
end
return 1
```

### 7.5 Trending Hashtags Cache

A sorted set where each hashtag is a member and its score reflects recent post activity. Updated when new posts are published; serves the "Trending" section.

```
Key pattern:   trending:hashtags
Type:          ZSET (score = recent post count or weighted decay score, member = hashtag name)
TTL:           600 seconds (10 minutes)
```

```bash
# Increment score when a post with a hashtag is published:
ZINCRBY trending:hashtags 1 "photography"

# Apply TTL to the whole key (refresh on rebuild):
EXPIRE trending:hashtags 600

# Get top 10 trending hashtags:
ZREVRANGEBYSCORE trending:hashtags +inf -inf WITHSCORES LIMIT 0 10

# Scheduled job rebuilds the ZSET from PostgreSQL every 10 minutes:
# SELECT h.name, COUNT(*) as score
# FROM post_hashtags ph
# JOIN hashtags h ON ph.hashtag_id = h.id
# JOIN posts p ON ph.post_id = p.id
# WHERE p.created_at > NOW() - INTERVAL '24 hours'
#   AND p.deleted_at IS NULL
# GROUP BY h.name
# ORDER BY score DESC
# LIMIT 50;
```

### 7.6 Notification Count Cache

Tracks the count of unread notifications per user for the notification badge without querying PostgreSQL on every page load.

```
Key pattern:   notif:unread:{userId}
Type:          STRING (integer counter)
TTL:           None (persists until explicitly cleared or reset)
```

```bash
# Increment when a new notification is created:
INCR notif:unread:{recipientUserId}

# Read for badge display:
GET notif:unread:{userId}

# Reset to 0 when user views notifications:
SET notif:unread:{userId} 0

# Delete key entirely on user account deletion:
DEL notif:unread:{userId}
```

---

## 8. Database Migrations

### 8.1 Migration Tool — Flyway

Boondi uses **Flyway** for versioned, repeatable, checksum-validated database migrations. Flyway integrates with Spring Boot's auto-configuration and runs migrations automatically on application startup.

**Spring Boot configuration** (`application.yml`):

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true
    out-of-order: false
    schemas:
      - public
```

**Maven dependency** (`pom.xml`):

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 8.2 Migration Naming Convention

Flyway migrations follow the pattern:

```
V{version}__{description}.sql
```

Rules:
- `V` prefix is mandatory for versioned migrations
- Version is a positive integer or decimal (e.g., `V1`, `V1_1`, `V10`)
- Two underscores separate version from description
- Description uses underscores (not spaces): `create_users`, not `create users`
- File must be in `src/main/resources/db/migration/`

Example: `V1__create_users.sql`

### 8.3 Initial Migration Order

Migrations must be run in strict dependency order — referenced tables must exist before tables that reference them via foreign keys.

```
src/main/resources/db/migration/
├── V1__create_enum_types.sql
├── V2__create_users.sql
├── V3__create_posts.sql
├── V4__create_media.sql
├── V5__create_social.sql
├── V6__create_notifications.sql
├── V7__create_hashtags.sql
├── V8__create_auth_tokens.sql
├── V9__create_reports.sql
├── V10__create_indexes.sql
├── V11__create_fts_index.sql
└── V12__create_triggers.sql
```

| Migration File | Contents |
|---|---|
| `V1__create_enum_types.sql` | All `CREATE TYPE` statements (`user_role`, `post_type`, `notification_type`, `media_type`, `report_status`) |
| `V2__create_users.sql` | `users` table DDL + `updated_at` trigger |
| `V3__create_posts.sql` | `posts` table DDL + `updated_at` trigger + post count trigger |
| `V4__create_media.sql` | `media` table DDL |
| `V5__create_social.sql` | `follows`, `likes`, `bookmarks` tables DDL + follow/like/bookmark count triggers |
| `V6__create_notifications.sql` | `notifications` table DDL |
| `V7__create_hashtags.sql` | `hashtags` and `post_hashtags` tables DDL + hashtag count trigger |
| `V8__create_auth_tokens.sql` | `refresh_tokens`, `password_reset_tokens`, `email_verifications` tables DDL |
| `V9__create_reports.sql` | `reports` table DDL + `updated_at` trigger |
| `V10__create_indexes.sql` | All secondary indexes (beyond PKs) |
| `V11__create_fts_index.sql` | FTS trigger + `search_vector` GIN index |
| `V12__create_triggers.sql` | All counter cache triggers (if not already in earlier files) |

---

## 9. Data Integrity & Constraints

### 9.1 Referential Integrity — ON DELETE Behavior

Foreign key `ON DELETE` actions are chosen deliberately to preserve data consistency and minimize orphaned records:

| FK Relationship | ON DELETE Action | Rationale |
|---|---|---|
| `posts.user_id → users.id` | `CASCADE` | If a user account is hard-deleted, all their posts go with them |
| `media.post_id → posts.id` | `CASCADE` | Media without a post is orphaned; remove it |
| `media.user_id → users.id` | `CASCADE` | Media without an owner is orphaned |
| `follows.follower_id → users.id` | `CASCADE` | Follow relationship is meaningless without both parties |
| `follows.following_id → users.id` | `CASCADE` | Same as above |
| `likes.user_id → users.id` | `CASCADE` | A like without a user is meaningless |
| `likes.post_id → posts.id` | `CASCADE` | A like without a post is meaningless |
| `bookmarks.user_id → users.id` | `CASCADE` | Bookmark without user is orphaned |
| `bookmarks.post_id → posts.id` | `CASCADE` | Bookmark without post is orphaned |
| `notifications.recipient_id → users.id` | `CASCADE` | Delete notifications when user is deleted |
| `notifications.actor_id → users.id` | `CASCADE` | Notifications from deleted actors are removed |
| `notifications.post_id → posts.id` | `CASCADE` | Remove notification if the triggering post is hard-deleted |
| `post_hashtags.post_id → posts.id` | `CASCADE` | Remove tag associations when post is deleted |
| `post_hashtags.hashtag_id → hashtags.id` | `CASCADE` | Remove associations when hashtag is deleted (rare) |
| `refresh_tokens.user_id → users.id` | `CASCADE` | Revoke all tokens if user is deleted |
| `password_reset_tokens.user_id → users.id` | `CASCADE` | Invalidate reset tokens for deleted users |
| `email_verifications.user_id → users.id` | `CASCADE` | Invalidate verification tokens for deleted users |
| `reports.reporter_id → users.id` | `CASCADE` | If reporter account is deleted, remove their reports |
| `reports.reported_user_id → users.id` | `SET NULL` | Preserve the report record even if the reported user account is deleted — admin audit trail |
| `reports.reported_post_id → posts.id` | `SET NULL` | Preserve the report even if the post is removed |
| `reports.reviewed_by → users.id` | `SET NULL` | Preserve the report even if the admin reviewer account is deleted |
| `posts.parent_post_id → posts.id` | `SET NULL` | If parent post is deleted, orphan replies but keep them (app shows "reply to deleted post") |
| `posts.quoted_post_id → posts.id` | `SET NULL` | Same as above |

**Note**: `ON DELETE CASCADE` applies to **hard deletes** only. Boondi's primary strategy is **soft deletes** (`deleted_at`), so hard deletes should only occur during GDPR deletion requests or admin purges.

### 9.2 Soft Delete Strategy

Soft deletes allow data recovery, maintain referential integrity during moderation, and preserve notification and timeline history without dangling references.

**Implementation**:

- Columns: `deleted_at TIMESTAMPTZ NULL` on `users` and `posts`
- Convention: `NULL` = active record; non-NULL = soft-deleted
- Application layer: all queries must include `WHERE deleted_at IS NULL` unless the operation explicitly targets deleted records (e.g., admin restore)
- JPA `@Where` annotation: apply a global filter at the entity level:

```java
@Entity
@Table(name = "posts")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE posts SET deleted_at = NOW() WHERE id = ?")
public class Post {
    // ...
}
```

**Partial indexes** on `deleted_at IS NULL` (defined in Section 5) ensure that soft-delete filtering does not degrade query performance by including deleted rows in the index.

**Data retention policy**: Soft-deleted posts are retained for 30 days, then scheduled for permanent purge via a maintenance job. Soft-deleted user accounts are retained for 90 days to allow reactivation.

### 9.3 Counter Cache Consistency

Counter caches (`like_count`, `reply_count`, `follower_count`, etc.) are denormalized aggregates maintained by PostgreSQL triggers (defined in Section 6.5).

**Trade-offs**:

| Approach | Pros | Cons |
|---|---|---|
| Trigger-based (chosen) | Always consistent; no app logic required; atomic with the data change | Slight write overhead; triggers fire inside transactions (potential for deadlock on high-concurrency) |
| Application-level | Flexible; can batch updates | Risk of inconsistency on crash or transaction rollback; requires careful coordination |
| Scheduled recalculation | Eventually accurate; good for low-frequency data | Stale during window between recalculations; adds operational complexity |

**Chosen approach**: Trigger-based for MVP. For high-scale (10,000+ concurrent users), transition to an **application-level increment with Redis as the write buffer** (INCR in Redis → periodic flush to PostgreSQL), reducing DB write contention.

**Periodic reconciliation**: A nightly scheduled job recalculates all counters from source tables and corrects any drift:

```sql
-- Reconcile like_count
UPDATE posts p
SET    like_count = (
    SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id
)
WHERE  p.deleted_at IS NULL;
```

### 9.4 Unique Constraints Summary

| Table | Unique Constraint | Purpose |
|---|---|---|
| `users` | `username` | Unique public handle |
| `users` | `email` | Unique login credential |
| `follows` | `(follower_id, following_id)` | Prevent duplicate follows |
| `likes` | `(user_id, post_id)` | Prevent double-liking |
| `bookmarks` | `(user_id, post_id)` | Prevent duplicate bookmarks |
| `post_hashtags` | `(post_id, hashtag_id)` (PK) | One tag per hashtag per post |
| `hashtags` | `name` | One canonical hashtag per name |

---

## 10. Query Patterns & Performance

### 10.1 Home Timeline Query

Fetches the most recent posts from all users that `currentUserId` follows, paginated with cursor-based pagination.

**Offset-based (simple, for MVP)**:

```sql
SELECT
    p.id,
    p.content,
    p.post_type,
    p.like_count,
    p.reply_count,
    p.repost_count,
    p.bookmark_count,
    p.created_at,
    u.id          AS author_id,
    u.username    AS author_username,
    u.display_name AS author_display_name,
    u.profile_picture_url AS author_avatar
FROM
    posts p
    INNER JOIN users u ON p.user_id = u.id
    INNER JOIN follows f ON p.user_id = f.following_id
WHERE
    f.follower_id = :currentUserId
    AND p.deleted_at IS NULL
    AND u.deleted_at IS NULL
ORDER BY
    p.created_at DESC
LIMIT  :pageSize
OFFSET :offset;
```

**Cursor-based (recommended for production)**:

```sql
-- Use the created_at of the last seen post as a cursor
SELECT
    p.id,
    p.content,
    p.created_at,
    u.username
FROM
    posts p
    INNER JOIN users u ON p.user_id = u.id
    INNER JOIN follows f ON p.user_id = f.following_id
WHERE
    f.follower_id = :currentUserId
    AND p.deleted_at IS NULL
    AND p.created_at < :cursor          -- cursor is last seen post's created_at
ORDER BY
    p.created_at DESC
LIMIT :pageSize;
```

**Performance notes**:
- Uses `idx_follows_follower_id` and `idx_posts_active` composite index
- At 10,000 users with 1,000 follows each: consider Redis timeline cache (Section 7.2) for O(1) reads

### 10.2 Trending Query

Finds the most engaged-with posts in the last 24 hours, ranked by engagement score.

```sql
SELECT
    p.id,
    p.content,
    p.created_at,
    u.username,
    (p.like_count + p.repost_count * 2 + p.reply_count) AS engagement_score
FROM
    posts p
    INNER JOIN users u ON p.user_id = u.id
WHERE
    p.created_at >= NOW() - INTERVAL '24 hours'
    AND p.deleted_at IS NULL
    AND p.post_type = 'ORIGINAL'
ORDER BY
    engagement_score DESC,
    p.created_at DESC
LIMIT 20;
```

**Trending hashtags query** (for rebuilding the Redis ZSET):

```sql
SELECT
    h.name,
    COUNT(ph.post_id) AS recent_post_count
FROM
    hashtags h
    INNER JOIN post_hashtags ph ON h.id = ph.hashtag_id
    INNER JOIN posts p ON ph.post_id = p.id
WHERE
    p.created_at >= NOW() - INTERVAL '24 hours'
    AND p.deleted_at IS NULL
GROUP BY
    h.id, h.name
ORDER BY
    recent_post_count DESC
LIMIT 20;
```

### 10.3 User Profile Query

Fetches a user's public profile data in a single query. Counter caches (`follower_count`, `following_count`, `post_count`) make this a point lookup without aggregation.

```sql
-- Public profile fetch
SELECT
    u.id,
    u.username,
    u.display_name,
    u.bio,
    u.profile_picture_url,
    u.banner_image_url,
    u.is_verified,
    u.follower_count,
    u.following_count,
    u.post_count,
    u.created_at,
    -- Is the requesting user following this profile?
    EXISTS (
        SELECT 1 FROM follows
        WHERE follower_id = :requestingUserId
          AND following_id = u.id
    ) AS is_following,
    -- Is the requesting user followed by this profile?
    EXISTS (
        SELECT 1 FROM follows
        WHERE follower_id = u.id
          AND following_id = :requestingUserId
    ) AS is_followed_by
FROM
    users u
WHERE
    u.username = :username
    AND u.deleted_at IS NULL;
```

### 10.4 Search Query

Full-text search on post content with relevance ranking.

```sql
SELECT
    p.id,
    p.content,
    p.created_at,
    p.like_count,
    u.username,
    u.display_name,
    u.profile_picture_url,
    ts_rank(p.search_vector, query) AS relevance_rank
FROM
    posts p
    INNER JOIN users u ON p.user_id = u.id,
    plainto_tsquery('english', :searchTerm) AS query
WHERE
    p.search_vector @@ query
    AND p.deleted_at IS NULL
    AND u.deleted_at IS NULL
ORDER BY
    relevance_rank DESC,
    p.created_at DESC
LIMIT  :pageSize
OFFSET :offset;
```

**User search** (search by username or display name):

```sql
SELECT
    id,
    username,
    display_name,
    profile_picture_url,
    follower_count,
    is_verified
FROM
    users
WHERE
    (
        username ILIKE '%' || :query || '%'
        OR display_name ILIKE '%' || :query || '%'
    )
    AND deleted_at IS NULL
ORDER BY
    follower_count DESC,
    username
LIMIT 20;
```

**Note**: For user search at scale, add a GIN index on `(username gin_trgm_ops)` using the `pg_trgm` extension for trigram-based ILIKE performance.

### 10.5 Notification Query

Fetches notifications for the current user, unread items first.

```sql
-- Fetch notifications, unread first, then by date
SELECT
    n.id,
    n.notification_type,
    n.is_read,
    n.created_at,
    n.post_id,
    actor.id           AS actor_id,
    actor.username     AS actor_username,
    actor.display_name AS actor_display_name,
    actor.profile_picture_url AS actor_avatar,
    p.content          AS post_content
FROM
    notifications n
    INNER JOIN users actor ON n.actor_id = actor.id
    LEFT  JOIN posts p ON n.post_id = p.id
WHERE
    n.recipient_id = :userId
ORDER BY
    n.is_read ASC,      -- FALSE (unread) sorts first
    n.created_at DESC
LIMIT  :pageSize
OFFSET :offset;
```

**Mark all as read**:

```sql
UPDATE notifications
SET    is_read = TRUE
WHERE  recipient_id = :userId
  AND  is_read = FALSE;
-- Then reset Redis counter:
-- SET notif:unread:{userId} 0
```

**Unread count** (used for badge; prefer Redis cache):

```sql
SELECT COUNT(*)
FROM   notifications
WHERE  recipient_id = :userId
  AND  is_read = FALSE;
```

---

## 11. Data Security

### 11.1 Password Storage

Passwords are **never stored in plaintext**. The application stores only the output of BCrypt hashing, which includes the salt embedded in the hash string.

- **Algorithm**: BCrypt with cost factor 12 (adjustable based on server capability)
- **Library**: Spring Security's `BCryptPasswordEncoder`
- **Hash format**: `$2a$12$...` (60-character string; fits `VARCHAR(255)`)
- **Verification**: Compare input via `passwordEncoder.matches(rawPassword, storedHash)` — never by decrypting

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

BCrypt automatically handles salt generation and embedding. The `password_hash` column stores the complete output including the salt prefix.

### 11.2 Token Storage

No raw token values are ever stored in the database:

| Token Type | Stored Value | Reason |
|---|---|---|
| Refresh tokens | SHA-256 hash of the opaque token string | If DB is compromised, attacker cannot use the hashes to authenticate |
| Password reset tokens | SHA-256 hash | One-time use; hash is meaningless after use |
| Email verification tokens | SHA-256 hash | Same as above |

The plaintext token is generated in the application layer (using `SecureRandom`), transmitted to the user via email or API response, and then discarded. Only the hash is persisted.

```java
// Token generation
String rawToken = generateSecureToken(); // 32-byte SecureRandom → Base64URL
String tokenHash = DigestUtils.sha256Hex(rawToken);
// Store tokenHash in DB, return rawToken to user
```

JWT access tokens are **not stored in the database** at all. They are stateless and validated via signature verification. Revocation is handled via the Redis blacklist (Section 7.1).

### 11.3 PII Handling

**Personally Identifiable Information (PII)** in Boondi includes:

| Field | Table | Classification | Protection |
|---|---|---|---|
| `email` | `users` | PII — contact info | Unique index; never exposed in public APIs; encrypted at rest (disk encryption) |
| `password_hash` | `users` | Sensitive — credential | Never returned in API responses; filtered at service layer |
| `display_name` | `users` | PII — identity | User-visible; can be pseudonymous |
| `bio` | `users` | Semi-public | User-visible on profile |
| `profile_picture_url` | `users` | Public | URL to CDN asset |
| `device_info` | `refresh_tokens` | PII — device data | Stored as opaque text; not returned to users; used only for session management display |

**GDPR / Right to Erasure**: When a user requests account deletion:
1. Soft-delete the `users` record (`deleted_at = NOW()`)
2. Schedule a hard-delete job to run after 90 days
3. Anonymize PII immediately: overwrite `email`, `display_name`, `bio` with anonymized values in the same transaction
4. Hard-delete all `refresh_tokens`, `password_reset_tokens`, `email_verifications` immediately
5. Posts and social graph are hard-deleted via `CASCADE` on `users.id` hard-delete

### 11.4 Access Control at DB Level

Create dedicated database users with minimum required privileges:

```sql
-- Application runtime user (used by HikariCP)
CREATE USER boondi_app WITH PASSWORD 'strong-random-password';
GRANT CONNECT ON DATABASE boondi TO boondi_app;
GRANT USAGE ON SCHEMA public TO boondi_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO boondi_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO boondi_app;
-- NOT granted: CREATE TABLE, DROP TABLE, TRUNCATE, REFERENCES

-- Migration user (used by Flyway only)
CREATE USER boondi_migration WITH PASSWORD 'another-strong-password';
GRANT ALL PRIVILEGES ON DATABASE boondi TO boondi_migration;
-- This user runs migrations; credentials stored only in CI/CD secrets

-- Read-only user (for analytics, read replicas)
CREATE USER boondi_readonly WITH PASSWORD 'readonly-password';
GRANT CONNECT ON DATABASE boondi TO boondi_readonly;
GRANT USAGE ON SCHEMA public TO boondi_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO boondi_readonly;
```

Additional hardening:
- Enable SSL/TLS for all PostgreSQL connections (`ssl = on` in `postgresql.conf`)
- Restrict `pg_hba.conf` to allow connections only from application server IP ranges
- Enable `log_statement = 'ddl'` in PostgreSQL to audit schema changes
- Rotate database credentials via a secrets manager (e.g., HashiCorp Vault, AWS Secrets Manager) on a scheduled basis

---

## 12. Backup & Recovery

### 12.1 Backup Strategy

**Development / Staging**: Manual `pg_dump` snapshots before significant schema changes.

**Production**:

| Method | Frequency | Retention | Tool |
|---|---|---|---|
| Logical backup (full) | Daily (off-peak hours) | 30 days | `pg_dump` → compressed `.sql.gz` → Object Storage |
| WAL archiving (continuous) | Continuous | 7 days | `archive_command` or managed DB WAL shipping |
| Point-in-Time Recovery (PITR) | Available from WAL | 7 days | `pg_basebackup` + WAL replay |

**`pg_dump` command**:

```bash
pg_dump \
  --host=$DB_HOST \
  --port=5432 \
  --username=boondi_migration \
  --format=custom \
  --compress=9 \
  --file=boondi_$(date +%Y%m%d_%H%M%S).dump \
  boondi
```

**WAL archiving** (`postgresql.conf`):

```ini
wal_level = replica
archive_mode = on
archive_command = 'aws s3 cp %p s3://boondi-wal-archive/%f'
```

**Backup verification**: Restore the last daily backup to a test instance every week to confirm backup integrity. A backup that cannot be restored is not a backup.

### 12.2 Recovery Point Objective (RPO)

| Environment | RPO | Mechanism |
|---|---|---|
| MVP / Development | 24 hours | Daily `pg_dump` |
| Production (Target) | 5 minutes | WAL archiving (continuous) |
| Production (Stretched Goal) | Near-zero | Synchronous streaming replication to standby |

RPO defines the maximum acceptable data loss window. With WAL archiving, the most you can lose is the interval since the last WAL file was archived (typically seconds to minutes, depending on `archive_timeout`).

### 12.3 Recovery Time Objective (RTO)

| Scenario | RTO | Approach |
|---|---|---|
| Single table corruption | < 1 hour | Restore specific table from `pg_dump` |
| Full DB restore (from daily dump) | < 2 hours | `pg_restore` from latest daily backup |
| Full DB restore (PITR) | < 30 minutes | `pg_basebackup` restore + WAL replay to target time |
| Failover to standby | < 5 minutes | Promote hot standby (if streaming replication is configured) |

RTO defines the maximum acceptable downtime. For Boondi's MVP (10–20 users), an RTO of 2 hours from a daily dump is acceptable. As the user base grows, invest in streaming replication to reduce RTO to minutes.

---

## 13. Scalability Considerations

### 13.1 Read Replicas for Timeline Queries

Timeline reads are the highest-volume query pattern. At moderate scale (1,000+ active users), offload read traffic to one or more PostgreSQL read replicas.

**Architecture**:

```
┌──────────────────────┐     Write (INSERT/UPDATE/DELETE)
│  Spring Boot App     │──────────────────────────────────► Primary PostgreSQL
│                      │
│  DataSource Router   │     Read (SELECT)
│  (LazyConnectionDS)  │──────────────────────────────────► Read Replica(s)
└──────────────────────┘
```

**Spring Boot configuration** (routing data source):

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put("primary", primary);
        targets.put("replica", replica);

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                        ? "replica" : "primary";
            }
        };
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(primary);
        return routing;
    }
}
```

Service methods with `@Transactional(readOnly = true)` will automatically route to the replica.

### 13.2 Connection Pooling — PgBouncer

For 10,000+ users, application-level HikariCP pools may create too many connections on the PostgreSQL server (one pool per app instance × pool size). **PgBouncer** acts as a connection multiplexer between the application and PostgreSQL.

```
App Instance 1 (HikariCP: 20 conns)  ─┐
App Instance 2 (HikariCP: 20 conns)  ─┼──► PgBouncer (transaction pooling) ──► PostgreSQL (50 real conns)
App Instance 3 (HikariCP: 20 conns)  ─┘
```

**PgBouncer mode**: Use **transaction pooling** for Boondi's stateless REST API (a connection is held only for the duration of a transaction, then returned to the pool). This allows 60 app-side connections to share 50 real PostgreSQL connections efficiently.

**PgBouncer configuration** (`pgbouncer.ini`):

```ini
[databases]
boondi = host=postgres-primary port=5432 dbname=boondi

[pgbouncer]
pool_mode = transaction
max_client_conn = 500
default_pool_size = 50
min_pool_size = 5
reserve_pool_size = 10
reserve_pool_timeout = 5
server_idle_timeout = 600
```

### 13.3 Table Partitioning

At very large scale (millions of posts), the `posts` table becomes the primary bottleneck. Partition it by `created_at` using PostgreSQL's native range partitioning.

```sql
-- Convert posts to a partitioned table (requires migration with pg_partman or manual)
CREATE TABLE posts_partitioned (
    -- same columns as posts
) PARTITION BY RANGE (created_at);

-- Create monthly partitions
CREATE TABLE posts_2026_07
    PARTITION OF posts_partitioned
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE posts_2026_08
    PARTITION OF posts_partitioned
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

-- Automate with pg_partman extension
CREATE EXTENSION IF NOT EXISTS pg_partman;
SELECT partman.create_parent(
    p_parent_table := 'public.posts_partitioned',
    p_control := 'created_at',
    p_type := 'native',
    p_interval := 'monthly',
    p_premake := 3
);
```

**Benefits of partitioning**:
- Timeline queries (filtered by recent `created_at`) scan only recent partitions, not the entire table
- Old partitions can be archived or moved to cheaper storage (tablespace tiering)
- `VACUUM` and `ANALYZE` run faster on individual partitions

**Timeline for adoption**: Implement partitioning when the `posts` table exceeds ~50 million rows or query times consistently exceed 100ms for indexed lookups.

### 13.4 Archival Strategy

Posts older than 2 years (or as defined by product policy) can be moved to an archival PostgreSQL instance or cold storage:

```sql
-- Move old posts to archive table
INSERT INTO posts_archive
SELECT * FROM posts
WHERE  created_at < NOW() - INTERVAL '2 years'
  AND  deleted_at IS NOT NULL;  -- Archive only soft-deleted old posts

DELETE FROM posts
WHERE  id IN (SELECT id FROM posts_archive WHERE created_at < NOW() - INTERVAL '2 years');
```

For the posts of active accounts older than a threshold but not deleted, a two-tier approach works:
- **Hot tier** (PostgreSQL primary): Last 12 months of posts — fast indexed access
- **Warm tier** (PostgreSQL archive / read replica): Older posts — accessible but slower
- **Cold tier** (Object Storage as JSONL): Posts older than 2 years — retrieved only on explicit user request

This archival strategy keeps the primary `posts` table bounded in size, ensuring consistent query performance as the platform grows.

---

*End of Database Design Document*

---

**Document History**:

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0.0 | 2026-07-02 | Database Architecture Team | Initial release — covers all MVP tables, indexes, triggers, and Redis schema |
