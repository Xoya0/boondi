# Boondi Backend

> **Boondi** is a private social networking platform — a feature-rich, production-grade backend built with Spring Boot 3.3, PostgreSQL, Redis, and MinIO object storage.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Development Guide](#development-guide)
- [Testing](#testing)
- [Deployment](#deployment)

---

## Features

| Domain | Features |
|--------|---------|
| **Authentication** | Register, Login, JWT access + refresh tokens, token rotation, logout with blacklisting, email verification, password reset |
| **Users** | Profiles with avatar and banner, bio, username change, follow/unfollow, follower/following lists |
| **Posts** | Create, edit (30-min window), soft-delete, reply threads, quote posts, image uploads |
| **Interactions** | Like, unlike, repost, unrepost, bookmark, unbookmark |
| **Timelines** | Latest (public), Home (following), Trending (24h score), User, Replies, Bookmarks — cursor-paginated |
| **Search** | Full-text post search (PostgreSQL tsvector), user prefix search, hashtag prefix search |
| **Hashtags** | Extracted and indexed on post creation, trending hashtags (Redis-cached) |
| **Notifications** | Fan-out on like/repost/reply/follow, unread count, mark read/all-read |
| **Storage** | S3-compatible object storage (MinIO) for avatars, banners, post images |

---

## Architecture

```
+-----------------------------------------------------------+
|                    Presentation Layer                      |
|  AuthController  PostController  UserController  ...       |
+------------------------+----------------------------------+
                         |
+------------------------v----------------------------------+
|                   Application Layer                        |
|  AuthService  PostService  FollowService  ...             |
|  DTOs (Request/Response)  Mappers                         |
+------------------------+----------------------------------+
                         |
+------------------------v----------------------------------+
|                    Domain Layer                            |
|  Entities: User  Post  Follow  Notification  ...          |
|  Repository Interfaces  Enums                             |
+------------------------+----------------------------------+
                         |
+------------------------v----------------------------------+
|                 Infrastructure Layer                       |
|  JPA Repositories  Redis  MinIO/S3  SMTP                  |
|  JWT  Security Filter Chain  Exception Handler            |
+-----------------------------------------------------------+
```

The project follows **Clean Architecture**:
- **Domain** — pure business entities and repository interfaces (no framework dependencies)
- **Application** — use-case services and DTOs (depends only on Domain)
- **Infrastructure** — persistence, caching, storage, email implementations
- **Presentation** — REST controllers (thin layer, delegates to Application)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Cache | Redis 7 |
| Object Storage | MinIO (S3-compatible) via AWS SDK v2 |
| Security | Spring Security 6, JJWT 0.12.6 |
| Email | Spring Mail (SMTP) |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven 3 |
| Containerisation | Docker + Docker Compose |

---

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 21 (for local development without Docker)
- Maven 3.9+

### 1. Clone the repository

```bash
git clone https://github.com/your-org/boondi.git
cd boondi
```

### 2. Create environment file

```bash
cp .env.example .env
# Edit .env with your secrets (see Environment Variables section)
```

### 3. Start all services

```bash
docker compose up -d
```

This starts:
- **PostgreSQL** on port `5432`
- **Redis** on port `6379`
- **MinIO** on port `9000` (console: `9001`)
- **MailHog** on port `1025` (web UI: `8025`)
- **Backend** on port `8080`
- **Nginx** on port `80`

### 4. Verify the backend is running

```bash
curl http://localhost:8080/api/v1/health
```

### 5. Open API documentation

Visit http://localhost:8080/api/v1/swagger-ui.html

---

## Environment Variables

| Variable | Default (dev only) | Description |
|----------|-------------------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/boondi` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `boondi` | Database username |
| `DB_PASSWORD` | *(required)* | Database password |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | *(empty)* | Redis password |
| `JWT_SECRET` | *(required — no default in prod)* | HMAC-SHA256 signing key, min 256 bits. Generate: `openssl rand -base64 64` |
| `MAIL_HOST` | `localhost` | SMTP server hostname |
| `MAIL_PORT` | `1025` | SMTP server port |
| `MAIL_USERNAME` | *(empty)* | SMTP username |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
| `STORAGE_ENDPOINT` | `http://localhost:9000` | MinIO/S3 endpoint URL |
| `STORAGE_ACCESS_KEY` | *(required)* | MinIO/S3 access key |
| `STORAGE_SECRET_KEY` | *(required)* | MinIO/S3 secret key |
| `STORAGE_BUCKET` | `boondi-media` | S3 bucket name |
| `STORAGE_REGION` | `us-east-1` | S3 region |
| `STORAGE_PUBLIC_URL` | `http://localhost:9000` | Public-facing URL for uploaded files |
| `APP_BASE_URL` | `http://localhost:3000` | Frontend base URL (used in email links) |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Comma-separated allowed CORS origins |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev` or `prod`) |

---

## API Reference

All endpoints are prefixed with `/api/v1`. Full interactive documentation: `/api/v1/swagger-ui.html`

### Authentication

```
POST   /auth/register              Register a new user
POST   /auth/login                 Login (returns access + refresh tokens)
POST   /auth/refresh               Refresh access token (token rotation)
POST   /auth/logout                Logout (revokes refresh token, blacklists access token)
GET    /auth/verify-email?token=   Verify email from link
POST   /auth/resend-verification   Resend verification email [Auth]
POST   /auth/forgot-password       Request password reset email
POST   /auth/reset-password        Reset password with token from email
```

### Posts

```
POST   /posts                         Create a post
GET    /posts/{postId}                Get a post (public)
PUT    /posts/{postId}                Edit a post (author, 30-min window) [Auth]
DELETE /posts/{postId}                Delete a post (author, soft delete) [Auth]
GET    /posts/{postId}/replies        Get replies (cursor-paginated, public)
POST   /posts/images                  Upload post image (JPEG/PNG/WebP, max 5MB) [Auth]
POST   /posts/{postId}/like           Like [Auth]
DELETE /posts/{postId}/like           Remove like [Auth]
POST   /posts/{postId}/repost         Repost [Auth]
DELETE /posts/{postId}/repost         Remove repost [Auth]
POST   /posts/{postId}/bookmark       Bookmark [Auth]
DELETE /posts/{postId}/bookmark       Remove bookmark [Auth]
```

**Create a post:**
```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello Boondi! #hello"}'
```

**Reply to a post:**
```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great post!","parentPostId":"<uuid>"}'
```

### Users

```
GET    /users/{username}              Get profile (public)
GET    /users/{username}/posts        Get user posts (cursor-paginated, public)
GET    /users/{username}/followers    Get followers (cursor-paginated, public)
GET    /users/{username}/following    Get following (cursor-paginated, public)
POST   /users/{username}/follow       Follow [Auth]
DELETE /users/{username}/follow       Unfollow [Auth]
PUT    /users/me                      Update profile [Auth]
POST   /users/me/avatar               Upload avatar (max 5MB) [Auth]
POST   /users/me/banner               Upload banner (max 10MB) [Auth]
GET    /users/me/bookmarks            Get bookmarked posts (cursor-paginated) [Auth]
```

### Timelines

```
GET    /timelines/latest              Latest posts, reverse-chronological (public)
GET    /timelines/home                Home feed from followed users [Auth]
GET    /timelines/trending            Trending posts last 24h by score (public)
```

All timelines accept `?cursor=<ISO-8601>&limit=<1-50>`. Response includes `nextCursor`.

### Search

```
GET    /search/users?q=<query>        Search users by username/display name
GET    /search/posts?q=<query>        Full-text search posts
GET    /search/hashtags?q=<query>     Search hashtags by prefix
GET    /hashtags/trending             Top 10 trending hashtags (last 24h, Redis-cached)
```

### Notifications

```
GET    /notifications                 List notifications (cursor-paginated) [Auth]
GET    /notifications/unread-count    Get unread count [Auth]
PATCH  /notifications/{id}/read       Mark as read [Auth]
PATCH  /notifications/read-all        Mark all as read [Auth]
```

### Response Envelope

```json
{
  "success": true,
  "data": { },
  "message": "Human-readable message",
  "timestamp": "2026-07-06T10:30:00"
}
```

On error:
```json
{
  "success": false,
  "errorCode": "USER_NOT_FOUND",
  "message": "User not found: john_doe",
  "path": "/api/v1/users/john_doe",
  "timestamp": "2026-07-06T10:30:00"
}
```

---

## Database Schema

Managed by Flyway migrations in `src/main/resources/db/migration/`.

| Migration | Description |
|-----------|-------------|
| `V1` | `users` table with soft-delete, indexes |
| `V2` | `email_verifications`, `password_reset_tokens` |
| `V3` | `posts` table with soft-delete, self-referencing FKs |
| `V4` | `follows` table with composite PK and self-follow check constraint |
| `V5` | `post_likes`, `post_reposts`, `post_bookmarks` |
| `V6` | `notifications`, `hashtags`, `post_hashtags`, full-text search `tsvector` column |

### Key Design Decisions

- **Soft deletes:** `users` and `posts` use `deleted_at` with `@SQLRestriction("deleted_at IS NULL")`
- **Cursor pagination:** Time-based ISO-8601 cursors for feeds; offset cursors for search/trending
- **Denormalized counters:** `like_count`, `follower_count`, etc. on entities for O(1) reads
- **Full-text search:** PostgreSQL generated `tsvector` column with GIN index on `posts.content`
- **Token security:** Tokens stored as SHA-256 hashes; raw tokens only returned to clients

### DBeaver Connection (Local Dev)

```
Host: localhost  |  Port: 5432
Database: boondi  |  User: boondi  |  Password: boondi
```

---

## Development Guide

### Running Locally (without Docker for the backend)

```bash
# Start only infrastructure
docker compose up -d postgres redis minio mailhog

# Run the backend
cd backend
./mvnw spring-boot:run
```

### Project Structure

```
backend/src/main/java/com/boondi/
├── BoondiApplication.java
├── application/
│   ├── dto/request/          # Validated request bodies
│   ├── dto/response/         # Response DTOs
│   ├── mapper/               # Entity -> DTO mappers
│   └── service/              # Use-case services
├── domain/
│   ├── entity/               # JPA entities
│   ├── enums/                # Domain enumerations
│   └── repository/           # Repository interfaces
├── infrastructure/
│   ├── config/               # Spring configuration
│   ├── exception/            # Exception types + global handler
│   ├── security/             # JWT, filters, UserDetails
│   └── service/              # Email, Storage implementations
└── presentation/
    └── controller/           # REST controllers
```

### Adding a New Feature

1. **Domain:** Add entity in `domain/entity/`, interface in `domain/repository/`
2. **Application:** Add service in `application/service/`, DTOs in `application/dto/`
3. **Infrastructure:** Add Flyway migration, implement repository queries
4. **Presentation:** Add controller in `presentation/controller/`
5. **Tests:** Unit test the service, integration test the endpoint

### Logging Convention

```java
log.info("Action completed: entityId={}, userId={}", entity.getId(), userId);
log.warn("Unexpected state: detail={}", detail);
log.error("Critical failure at path: error={}", e.getMessage(), e);
```

---

## Testing

```bash
cd backend
./mvnw test
```

Test infrastructure uses Testcontainers (PostgreSQL) configured via `application-test.yml`.

### Test Structure (to be implemented)

```
src/test/java/com/boondi/
├── unit/
│   ├── service/              # Mock-based unit tests
│   └── security/             # JWT token tests
└── integration/
    ├── auth/                 # Full auth flow tests
    ├── post/                 # Post lifecycle tests
    └── follow/               # Follow/unfollow tests
```

---

## Deployment

Full step-by-step production deployment and monitoring instructions (TLS setup, secrets, backups, DB/Redis/app monitoring, troubleshooting) live in **[`doc/Deployment-and-Monitoring-Guide.md`](doc/Deployment-and-Monitoring-Guide.md)**. Short version:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Production hardening already done as of Sprint 10 (see `PROGRESS.md`'s Sprint 10 section for the full audit): non-root Dockerfile user, Swagger UI/API docs disabled by default under the `prod` profile (`application-prod.yml`), actuator `show-details` set to `never` (not `always` — public health checks shouldn't leak DB/Redis internals), `JWT_SECRET` fails the app fast at startup if it's still the dev placeholder, and every secret in `docker-compose.prod.yml` is required (no silent defaults).

### Health Check Endpoints

```
GET /health                    # app-level, no auth, minimal detail — what Nginx/Docker actually probe
GET /api/v1/actuator/health    # bare UP/DOWN in prod (show-details: never)
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Write tests for your changes
4. Ensure all tests pass: `./mvnw test`
5. Open a pull request

---

## License

MIT License
