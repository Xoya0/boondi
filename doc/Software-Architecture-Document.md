# Software Architecture Document (SAD) — Boondi

**Document Version**: 1.0.0
**Date**: 2026-07-02
**Status**: Active
**Classification**: Internal — Engineering

---

## Table of Contents

1. [Introduction](#1-introduction)
   - 1.1 [Purpose](#11-purpose)
   - 1.2 [Scope](#12-scope)
   - 1.3 [Definitions and Acronyms](#13-definitions-and-acronyms)
   - 1.4 [References](#14-references)
   - 1.5 [Document Overview](#15-document-overview)
2. [Architectural Goals and Constraints](#2-architectural-goals-and-constraints)
   - 2.1 [Architectural Goals](#21-architectural-goals)
   - 2.2 [Constraints](#22-constraints)
   - 2.3 [Quality Attributes](#23-quality-attributes)
3. [System Overview](#3-system-overview)
   - 3.1 [High-Level Architecture Diagram](#31-high-level-architecture-diagram)
   - 3.2 [Component Summary](#32-component-summary)
   - 3.3 [Deployment Overview](#33-deployment-overview)
4. [Architectural Style and Patterns](#4-architectural-style-and-patterns)
   - 4.1 [Architectural Style](#41-architectural-style)
   - 4.2 [Design Patterns Used](#42-design-patterns-used)
   - 4.3 [API Design Principles](#43-api-design-principles)
5. [Backend Architecture](#5-backend-architecture)
   - 5.1 [Package Structure](#51-package-structure)
   - 5.2 [Layer Descriptions](#52-layer-descriptions)
   - 5.3 [Spring Boot Configuration Strategy](#53-spring-boot-configuration-strategy)
   - 5.4 [Security Architecture](#54-security-architecture)
   - 5.5 [Caching Strategy](#55-caching-strategy)
   - 5.6 [Media and File Handling](#56-media-and-file-handling)
   - 5.7 [Notification Architecture](#57-notification-architecture)
   - 5.8 [Search Architecture](#58-search-architecture)
6. [Database Architecture](#6-database-architecture)
   - 6.1 [Database Choice Rationale](#61-database-choice-rationale)
   - 6.2 [Entity Relationship Overview](#62-entity-relationship-overview)
   - 6.3 [Key Database Design Decisions](#63-key-database-design-decisions)
   - 6.4 [Redis Usage](#64-redis-usage)
7. [Android Architecture](#7-android-architecture)
   - 7.1 [Architectural Pattern](#71-architectural-pattern)
   - 7.2 [Layer Structure](#72-layer-structure)
   - 7.3 [Dependency Injection](#73-dependency-injection)
   - 7.4 [Offline Support](#74-offline-support)
   - 7.5 [Navigation](#75-navigation)
   - 7.6 [State Management](#76-state-management)
8. [Web Frontend Architecture](#8-web-frontend-architecture)
   - 8.1 [Architectural Pattern](#81-architectural-pattern)
   - 8.2 [Folder Structure](#82-folder-structure)
   - 8.3 [State Management](#83-state-management)
   - 8.4 [Routing](#84-routing)
   - 8.5 [API Layer](#85-api-layer)
   - 8.6 [Styling](#86-styling)
9. [API Architecture](#9-api-architecture)
   - 9.1 [REST API Design](#91-rest-api-design)
   - 9.2 [Endpoint Catalog](#92-endpoint-catalog)
   - 9.3 [Request and Response Format](#93-request-and-response-format)
   - 9.4 [Authentication Flow](#94-authentication-flow)
   - 9.5 [Pagination Strategy](#95-pagination-strategy)
   - 9.6 [Error Handling](#96-error-handling)
   - 9.7 [API Versioning Strategy](#97-api-versioning-strategy)
10. [Infrastructure and Deployment](#10-infrastructure-and-deployment)
    - 10.1 [Development Environment](#101-development-environment)
    - 10.2 [Production Architecture](#102-production-architecture)
    - 10.3 [Docker Configuration Overview](#103-docker-configuration-overview)
    - 10.4 [Nginx Configuration Role](#104-nginx-configuration-role)
    - 10.5 [Logging and Monitoring Strategy](#105-logging-and-monitoring-strategy)
    - 10.6 [Future: Kubernetes Migration Path](#106-future-kubernetes-migration-path)
11. [Security Architecture](#11-security-architecture)
    - 11.1 [Authentication and Authorization](#111-authentication-and-authorization)
    - 11.2 [Data Security](#112-data-security)
    - 11.3 [Input Validation and Sanitization](#113-input-validation-and-sanitization)
    - 11.4 [Rate Limiting](#114-rate-limiting)
    - 11.5 [OWASP Top 10 Mitigations](#115-owasp-top-10-mitigations)
12. [Cross-Cutting Concerns](#12-cross-cutting-concerns)
    - 12.1 [Logging](#121-logging)
    - 12.2 [Error Handling](#122-error-handling)
    - 12.3 [Validation](#123-validation)
    - 12.4 [Internationalization](#124-internationalization)
    - 12.5 [Configuration Management](#125-configuration-management)
13. [Architecture Decision Records](#13-architecture-decision-records)
14. [Future Architecture Considerations](#14-future-architecture-considerations)
    - 14.1 [Scaling Strategy](#141-scaling-strategy)
    - 14.2 [Microservices Migration Path](#142-microservices-migration-path)
    - 14.3 [Message Queue](#143-message-queue)
    - 14.4 [Search Enhancement](#144-search-enhancement)
    - 14.5 [CDN Integration](#145-cdn-integration)

---

## 1. Introduction

### 1.1 Purpose

This Software Architecture Document (SAD) describes the complete architecture of **Boondi**, a private social networking platform. The document serves as the authoritative technical reference for engineering decisions, system design, and implementation guidance across all platform components: backend API, Android client, and web frontend.

This document is intended for:

- **Backend Engineers** implementing the Spring Boot API and data layer
- **Android Engineers** building the Kotlin/Jetpack Compose mobile client
- **Web Engineers** building the React/TypeScript web client
- **DevOps/Infrastructure Engineers** managing deployment pipelines and infrastructure
- **Technical Leadership** reviewing and approving architectural decisions
- **New Team Members** requiring onboarding to system design and conventions

### 1.2 Scope

This document covers the architecture of the Boondi platform as described by the following boundaries:

**In Scope:**
- Backend REST API (Java 21, Spring Boot)
- Android mobile application (Kotlin, Jetpack Compose)
- Web application (React 18, TypeScript)
- Database design (PostgreSQL, Redis)
- Infrastructure and deployment (Docker, Nginx)
- Security model (JWT, RBAC, OWASP)
- API design and contracts
- Media storage and delivery

**Out of Scope:**
- Third-party integrations beyond those explicitly stated
- Business logic and product feature specifications (see PRD and SRS)
- Mobile iOS client (future)
- Desktop clients

### 1.3 Definitions and Acronyms

| Term | Definition |
|------|-----------|
| **SAD** | Software Architecture Document |
| **API** | Application Programming Interface |
| **REST** | Representational State Transfer |
| **JWT** | JSON Web Token — a compact, URL-safe means of representing claims |
| **JPA** | Jakarta Persistence API — ORM standard for Java |
| **ORM** | Object-Relational Mapper |
| **DTO** | Data Transfer Object — object used to transfer data between layers |
| **RBAC** | Role-Based Access Control |
| **MVVM** | Model-View-ViewModel — UI architectural pattern |
| **DI** | Dependency Injection |
| **UUID** | Universally Unique Identifier (RFC 4122) |
| **CDN** | Content Delivery Network |
| **TLS** | Transport Layer Security |
| **HTTPS** | HTTP over TLS |
| **BCrypt** | Adaptive password hashing function |
| **CQRS** | Command Query Responsibility Segregation |
| **ADR** | Architecture Decision Record |
| **MVP** | Minimum Viable Product |
| **FCM** | Firebase Cloud Messaging |
| **SRS** | Software Requirements Specification |
| **PRD** | Product Requirements Document |
| **FTS** | Full-Text Search |
| **TTL** | Time-To-Live (cache expiry) |
| **CI/CD** | Continuous Integration / Continuous Deployment |
| **HPA** | Horizontal Pod Autoscaler (Kubernetes) |

### 1.4 References

| Reference | Description |
|-----------|-------------|
| `Social_Media_PRD.md` | Product Requirements Document for Boondi |
| `Software-Requirements-Specification.md` | Software Requirements Specification |
| RFC 7519 | JSON Web Token specification |
| RFC 4122 | UUID specification |
| OWASP Top 10 (2021) | Web application security risks |
| Spring Boot 3.x Documentation | Official Spring Boot reference |
| OpenAPI Specification 3.1 | API description format standard |
| Jetpack Compose Documentation | Android UI framework reference |
| React 18 Documentation | Web UI library reference |

### 1.5 Document Overview

This document is organized into fourteen sections:

- **Sections 1–2**: Establish purpose, scope, and architectural goals
- **Section 3**: Provides a high-level system overview and deployment context
- **Section 4**: Describes architectural style and recurring design patterns
- **Sections 5–8**: Provide detailed architecture for each system layer (backend, database, Android, web)
- **Section 9**: Specifies the API contract design
- **Section 10**: Covers infrastructure and deployment
- **Sections 11–12**: Address security and cross-cutting concerns
- **Section 13**: Records key architectural decisions with rationale
- **Section 14**: Outlines the future evolution path

---

## 2. Architectural Goals and Constraints

### 2.1 Architectural Goals

The following goals drive all architectural decisions for Boondi:

| # | Goal | Description |
|---|------|-------------|
| G1 | **Correctness** | The system must behave according to specifications under all expected conditions |
| G2 | **Scalability** | The platform must support 10–20 initial users and scale to 10,000+ without re-architecture |
| G3 | **Performance** | API endpoints must respond in under 300ms on average; timeline loads under 500ms |
| G4 | **Security** | All data must be protected in transit and at rest; authentication must be robust |
| G5 | **Maintainability** | Code must be organized for readability, testability, and ease of modification |
| G6 | **Availability** | The platform must target 99.9% uptime in production |
| G7 | **Developer Experience** | Architecture must support rapid iteration with clear layer boundaries |
| G8 | **Portability** | Application must run consistently across development and production via containerization |
| G9 | **Observability** | All critical operations must be logged and monitorable |
| G10 | **Evolvability** | Architecture must accommodate future feature additions without structural overhaul |

### 2.2 Constraints

#### Technical Constraints

| Constraint | Rationale |
|------------|-----------|
| Java 21 (LTS) | Long-term support, virtual threads (Project Loom), modern language features |
| PostgreSQL as primary database | Relational integrity, JSONB support, full-text search, proven at scale |
| Stateless backend | Required for horizontal scaling; session state externalized to Redis |
| UUID primary keys | Avoids sequential ID enumeration attacks; supports distributed generation |
| HTTPS enforced everywhere | Regulatory and security baseline; no plaintext traffic |
| Docker for all environments | Reproducibility and parity between dev and production |
| OpenAPI 3.1 documentation | Enables client code generation and contractual API communication |

#### Resource Constraints

| Constraint | Impact |
|------------|--------|
| Small initial team | Monolithic backend preferred over microservices initially |
| Private platform | Invite-only model; no open registration required at MVP |
| Limited infrastructure budget | Single-server production initially; Kubernetes deferred |

#### Organizational Constraints

| Constraint | Impact |
|------------|--------|
| Single primary backend | All API concerns in one Spring Boot application for MVP |
| Shared API between Android and Web | Both clients consume the same REST API with no client-specific endpoints |

### 2.3 Quality Attributes

#### Performance

| Metric | Target |
|--------|--------|
| API average response time | < 300ms (p95) |
| Timeline feed load time | < 500ms |
| Media upload acknowledgment | < 2 seconds |
| Database query time (indexed) | < 50ms |
| Cache hit ratio (timelines) | > 80% |

Performance is addressed through:
- Redis caching of hot data (timelines, user profiles)
- Database indexing on frequently queried columns
- Cursor-based pagination for timeline feeds
- Asynchronous media processing

#### Security

- JWT-based stateless authentication with short-lived access tokens (15 minutes) and refresh tokens (7 days)
- BCrypt password hashing (cost factor 12)
- HTTPS/TLS 1.2+ enforced at Nginx layer
- Input validation at every API boundary
- RBAC with Guest, User, and Admin roles
- Rate limiting on authentication and write endpoints
- OWASP Top 10 mitigations applied

#### Scalability

- Stateless API enables horizontal scaling by adding instances behind a load balancer
- Redis caching reduces database read load
- Database read replicas planned for scale (see Section 14)
- Object storage (S3-compatible) for media scales independently
- Cursor-based pagination prevents database degradation at scale

#### Maintainability

- Clean Architecture with strict layer boundaries enforced
- DTO pattern prevents entity leakage across layers
- Repository pattern abstracts data access
- Spring profiles separate environment configuration
- Consistent error handling through global exception handler
- Comprehensive Swagger/OpenAPI documentation

#### Availability

| Component | Target |
|-----------|--------|
| API uptime | 99.9% |
| Database | PostgreSQL with automated backups; WAL archiving |
| Cache | Redis with persistence (RDB snapshots) |
| Media storage | S3-compatible object store with high durability |

---

## 3. System Overview

### 3.1 High-Level Architecture Diagram

```
  +----------------------------------+    +------------------------------+
  |         CLIENT TIER              |    |       CLIENT TIER            |
  |                                  |    |                              |
  |   +---------------------------+  |    |  +-----------------------+   |
  |   |   Android App (Kotlin)    |  |    |  |  Web App (React 18)   |   |
  |   |   Jetpack Compose         |  |    |  |  TypeScript + Vite    |   |
  |   |   Retrofit + Room         |  |    |  |  Tailwind + Rq        |   |
  |   +---------------------------+  |    |  +-----------------------+   |
  +-----------------|----------------+    +-------------|----------------+
                    |  HTTPS/TLS                        |  HTTPS/TLS
                    |                                   |
  +-----------------v-----------------------------------v----------------+
  |                        EDGE / GATEWAY TIER                          |
  |                                                                      |
  |   +----------------------------------------------------------------+ |
  |   |              Nginx Reverse Proxy                               | |
  |   |   - SSL/TLS Termination       - Static File Serving           | |
  |   |   - Request Routing           - Rate Limiting (future)        | |
  |   |   - Gzip Compression          - Security Headers              | |
  |   +----------------------------------------------------------------+ |
  +----------------------------------------------------------------------+
                    |
                    | HTTP (internal network)
                    |
  +-----------------v----------------------------------------------------+
  |                      APPLICATION TIER                                |
  |                                                                      |
  |   +----------------------------------------------------------------+ |
  |   |        Spring Boot API  (Java 21)                              | |
  |   |                                                                | |
  |   |   [Controller Layer]  ->  [Service Layer]  ->  [Repo Layer]   | |
  |   |                                                                | |
  |   |   Spring Security (JWT Filter Chain)                          | |
  |   |   Bean Validation  |  Global Exception Handler                | |
  |   |   Swagger/OpenAPI 3.1  |  Actuator Endpoints                  | |
  |   +----------------------------------------------------------------+ |
  +----------------------------------------------------------------------+
           |                    |                     |
           | JDBC/HikariCP      | Lettuce (Redis)     | S3 SDK
           |                    |                     |
  +--------v--------+  +--------v-------+  +----------v-----------+
  |   DATA TIER     |  |   CACHE TIER   |  |   STORAGE TIER       |
  |                 |  |                |  |                       |
  | +-----------+   |  | +-----------+  |  | +-------------------+ |
  | | PostgreSQL|   |  | |  Redis    |  |  | | S3-Compatible     | |
  | | Primary   |   |  | | - Sessions|  |  | | Object Store      | |
  | | Database  |   |  | | - Cache   |  |  | | (Media Files)     | |
  | +-----------+   |  | | - Rate    |  |  | +-------------------+ |
  |                 |  | |   Limits  |  |  |                       |
  |                 |  | +-----------+  |  |                       |
  +-----------------+  +----------------+  +-----------------------+
                                 |
                    +------------v----------+
                    |   NOTIFICATION TIER   |
                    |                       |
                    |   Email Service       |
                    |   (SMTP / SendGrid)   |
                    |   Future: FCM Push    |
                    +-----------------------+
```

### 3.2 Component Summary

| Component | Technology | Responsibility |
|-----------|-----------|----------------|
| **Android Client** | Kotlin, Jetpack Compose, Hilt, Retrofit, Room | Mobile user interface; offline caching; push notifications |
| **Web Client** | React 18, TypeScript, Vite, Tailwind CSS, React Query | Browser-based user interface; responsive design |
| **Nginx** | Nginx 1.25+ | Reverse proxy; SSL termination; static asset serving; routing |
| **Spring Boot API** | Java 21, Spring Boot 3.x | Core business logic; REST API; authentication; data orchestration |
| **PostgreSQL** | PostgreSQL 15+ | Primary relational data store; full-text search; ACID transactions |
| **Redis** | Redis 7+ | Session/token cache; timeline cache; rate limiting counters |
| **Object Store** | S3-compatible (MinIO dev / AWS S3 prod) | Binary media storage (images, avatars) |
| **Email Service** | SMTP / SendGrid | Transactional email for notifications |

### 3.3 Deployment Overview

**Development Environment**: Docker Compose orchestrates all services locally. Each engineer runs an identical stack: PostgreSQL container, Redis container, Spring Boot application, and Nginx, all networked via Docker bridge network. The React web app runs via Vite dev server.

**Production Environment**: Linux server running Docker with Docker Compose (initial), transitioning to Kubernetes as scale demands. Nginx terminates TLS and proxies requests to the Spring Boot container. All secrets are managed via environment variables injected at runtime.

```
Production Host (Linux)
+--------------------------------------------------------------+
|  Docker Network: boondi-net                                  |
|                                                              |
|  +----------+    +---------------+    +------------------+  |
|  |  Nginx   |--->| Spring Boot   |--->|   PostgreSQL     |  |
|  | :443     |    | API :8080     |    |   :5432          |  |
|  +----------+    +---------------+    +------------------+  |
|       |                  |            +------------------+  |
|       |                  +---------->|    Redis         |  |
|       |                  |            |    :6379         |  |
|       |                  |            +------------------+  |
|       v                  v                                   |
|  Static Web          External:                               |
|  Assets (dist)       Object Store (S3)                       |
|                      Email Provider                          |
+--------------------------------------------------------------+
         |
         | :443 (HTTPS/TLS)
         |
    Internet / Users
```

---

## 4. Architectural Style and Patterns

### 4.1 Architectural Style

Boondi adopts a **Layered Architecture** at the macro level combined with **Clean Architecture** principles within each application layer. This combination provides clear separation of concerns while remaining pragmatic for a small team.

#### Layered Architecture (Macro)

```
+-----------------------------------------------+
|           Presentation / Client Tier           |
|    Android App         Web App (React)         |
+-----------------------------------------------+
                        |
+-----------------------------------------------+
|              API Gateway Tier                  |
|                    Nginx                       |
+-----------------------------------------------+
                        |
+-----------------------------------------------+
|           Application / Business Tier          |
|            Spring Boot REST API                |
+-----------------------------------------------+
                        |
+-----------------------------------------------+
|               Data / Infrastructure Tier       |
|      PostgreSQL    Redis    Object Store        |
+-----------------------------------------------+
```

#### Clean Architecture (Spring Boot Backend — micro level)

The Spring Boot application enforces dependency direction inward:

```
+----------------------------------------------------------+
|  Infrastructure / Frameworks                             |
|  (Spring, JPA, Redis, S3 SDK, Email)                     |
|   +--------------------------------------------------+   |
|   |  Interface Adapters                              |   |
|   |  (Controllers, DTOs, Mappers, Repositories)     |   |
|   |   +------------------------------------------+  |   |
|   |   |  Application / Use Cases                 |  |   |
|   |   |  (Service Layer — business logic)        |  |   |
|   |   |   +----------------------------------+   |  |   |
|   |   |   |  Domain / Enterprise Rules       |   |  |   |
|   |   |   |  (Entities, Domain Events,       |   |  |   |
|   |   |   |   Domain Exceptions)             |   |  |   |
|   |   |   +----------------------------------+   |  |   |
|   |   +------------------------------------------+  |   |
|   +--------------------------------------------------+   |
+----------------------------------------------------------+

Dependency Rule: Outer layers depend on inner layers.
Inner layers have NO knowledge of outer layers.
```

### 4.2 Design Patterns Used

#### Repository Pattern

All database access is encapsulated behind repository interfaces. Services depend on repository interfaces, not concrete implementations.

```
UserService  -->  UserRepository (interface)
                         ^
                         |
               UserRepositoryImpl (Spring Data JPA)
```

This allows switching the data access implementation without modifying business logic.

#### Service Layer

All business logic resides in `@Service`-annotated classes. Controllers are thin orchestrators that delegate entirely to the service layer. Services encapsulate:
- Business rule enforcement
- Transaction management (`@Transactional`)
- Cross-entity operations
- Event publishing for notifications

#### DTO Pattern

Entities (JPA-managed objects) never leave the service layer. Controllers receive and return DTOs only. Mappers (using MapStruct or manual mapping) translate between entities and DTOs.

```
HTTP Request JSON  -->  [Controller]  -->  Request DTO
                                               |
                                           [Service]
                                               |
                                          [Repository]  -->  Entity (DB)
                                               |
                                           [Service]
                                               |
                              Response DTO  <--  [Controller]  -->  HTTP Response JSON
```

#### Factory / Builder Pattern

Complex object construction (e.g., building a Post with media, hashtags, and metadata) uses the Builder pattern via Lombok's `@Builder` annotation. Factories are used for creating domain events and notification payloads.

#### Observer Pattern (Notifications)

Notification creation is decoupled from the triggering action via Spring's `ApplicationEvent` / `ApplicationEventPublisher` mechanism. When a user likes a post, the like service publishes a `PostLikedEvent`. A `NotificationEventListener` handles this event and creates the corresponding `Notification` entity.

```
LikeService
    |
    |-- publishes --> PostLikedEvent
                           |
                   [ApplicationEventListener]
                           |
                   NotificationService.createNotification(...)
```

This ensures notification logic is not interleaved with core business logic.

#### CQRS (Future Consideration)

The current architecture serves read and write operations through the same service and repository layer. As read traffic grows (timelines, search), CQRS can be introduced by:
- Separating Command (write) and Query (read) models
- Optimizing read models for specific query patterns (e.g., denormalized timeline projections)
- Introducing a separate read replica for query handlers

### 4.3 API Design Principles

#### RESTful Design

- Resources are nouns, not verbs: `/posts` not `/getPosts`
- HTTP verbs convey action: `GET` (read), `POST` (create), `PUT` (replace), `PATCH` (partial update), `DELETE` (remove)
- Resource nesting reflects relationships: `/posts/{postId}/comments`
- Stateless: each request carries all information needed (JWT in Authorization header)

#### Versioning Strategy

API versioning uses **URL path prefixing**:

```
/api/v1/posts
/api/v1/users
```

All endpoints in MVP are `v1`. Breaking changes introduce `v2` routes while maintaining `v1` for a deprecation window. Version negotiation via Accept headers is not used to keep client code simple.

#### Error Format

All errors return a consistent JSON envelope (see Section 9.6).

---

## 5. Backend Architecture

### 5.1 Package Structure

The backend is organized under the root package `com.boondi` with the following structure:

```
com.boondi
├── BoondiApplication.java              # Spring Boot entry point
│
├── config/                             # Infrastructure configuration
│   ├── SecurityConfig.java             # Spring Security configuration
│   ├── RedisConfig.java                # Redis connection and cache managers
│   ├── S3Config.java                   # S3-compatible object storage client
│   ├── OpenApiConfig.java              # Swagger/OpenAPI configuration
│   ├── WebMvcConfig.java               # CORS, MVC configuration
│   └── JpaConfig.java                  # JPA auditing, entity manager config
│
├── security/                           # Security infrastructure
│   ├── JwtTokenProvider.java           # JWT generation and validation
│   ├── JwtAuthenticationFilter.java    # OncePerRequestFilter for JWT
│   ├── CustomUserDetailsService.java   # UserDetailsService implementation
│   ├── SecurityConstants.java          # Token expiry, secret key constants
│   └── UserPrincipal.java              # Authentication principal wrapper
│
├── domain/                             # Domain entities (JPA)
│   ├── User.java
│   ├── Profile.java
│   ├── Post.java
│   ├── Media.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Follow.java
│   ├── Notification.java
│   ├── Bookmark.java
│   ├── Hashtag.java
│   └── enums/
│       ├── Role.java                   # GUEST, USER, ADMIN
│       ├── NotificationType.java       # LIKE, COMMENT, FOLLOW, MENTION
│       └── MediaType.java              # IMAGE, VIDEO (future)
│
├── repository/                         # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── LikeRepository.java
│   ├── FollowRepository.java
│   ├── NotificationRepository.java
│   ├── BookmarkRepository.java
│   ├── MediaRepository.java
│   └── HashtagRepository.java
│
├── service/                            # Business logic layer
│   ├── AuthService.java
│   ├── UserService.java
│   ├── PostService.java
│   ├── CommentService.java
│   ├── LikeService.java
│   ├── FollowService.java
│   ├── TimelineService.java
│   ├── NotificationService.java
│   ├── BookmarkService.java
│   ├── MediaService.java
│   ├── SearchService.java
│   └── HashtagService.java
│
├── controller/                         # REST controllers (presentation layer)
│   ├── AuthController.java             # /api/v1/auth/**
│   ├── UserController.java             # /api/v1/users/**
│   ├── PostController.java             # /api/v1/posts/**
│   ├── CommentController.java          # /api/v1/posts/{id}/comments/**
│   ├── LikeController.java             # /api/v1/posts/{id}/likes/**
│   ├── FollowController.java           # /api/v1/users/{id}/follow/**
│   ├── TimelineController.java         # /api/v1/timeline/**
│   ├── NotificationController.java     # /api/v1/notifications/**
│   ├── BookmarkController.java         # /api/v1/bookmarks/**
│   ├── MediaController.java            # /api/v1/media/**
│   └── SearchController.java           # /api/v1/search/**
│
├── dto/                                # Data Transfer Objects
│   ├── request/                        # Incoming request bodies
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── CreatePostRequest.java
│   │   ├── UpdateProfileRequest.java
│   │   └── ...
│   └── response/                       # Outgoing response bodies
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── PostResponse.java
│       ├── PagedResponse.java
│       ├── ApiError.java
│       └── ...
│
├── mapper/                             # Entity <-> DTO mappers
│   ├── UserMapper.java
│   ├── PostMapper.java
│   ├── CommentMapper.java
│   └── NotificationMapper.java
│
├── event/                              # Domain events and listeners
│   ├── PostLikedEvent.java
│   ├── PostCommentedEvent.java
│   ├── UserFollowedEvent.java
│   ├── MentionEvent.java
│   └── NotificationEventListener.java
│
├── exception/                          # Exception hierarchy and handler
│   ├── BoondiException.java            # Base exception
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   ├── ValidationException.java
│   ├── DuplicateResourceException.java
│   └── GlobalExceptionHandler.java     # @RestControllerAdvice
│
└── util/                               # Shared utilities
    ├── SlugUtils.java                  # Hashtag and username slug generation
    ├── PaginationUtils.java            # Cursor encoding/decoding
    ├── SecurityUtils.java              # Current user extraction
    └── DateTimeUtils.java              # Timestamp formatting
```

### 5.2 Layer Descriptions

#### Presentation Layer (Controllers, DTOs)

Controllers are the entry points for all HTTP requests. Their responsibilities are strictly limited:

1. Parse and validate the incoming HTTP request (via `@Valid` on DTOs)
2. Extract authentication context (`@AuthenticationPrincipal`)
3. Call the appropriate service method
4. Map the service result to a response DTO
5. Return the appropriate HTTP status code

Controllers must not contain business logic. A typical controller method:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<PostResponse> createPost(
    @Valid @RequestBody CreatePostRequest request,
    @AuthenticationPrincipal UserPrincipal principal) {

    PostResponse response = postService.createPost(request, principal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

DTOs are plain Java records or classes (with Lombok) annotated with Jakarta Validation constraints. Request DTOs enforce input constraints. Response DTOs control what data is exposed to clients.

#### Business Logic Layer (Services)

Services contain all business rules and orchestrate data access. Key characteristics:

- Annotated with `@Service` and `@Transactional` at the class or method level
- Depend on repository interfaces (not implementations)
- Publish domain events via `ApplicationEventPublisher`
- Interact with Redis cache directly (or via Spring Cache abstraction)
- Throw typed domain exceptions that the global exception handler maps to HTTP responses
- Never return JPA entities directly; always map to DTOs or domain objects before returning

#### Data Access Layer (Repositories, JPA Entities)

Repositories extend `JpaRepository<Entity, UUID>` and `JpaSpecificationExecutor` where complex queries are needed. Custom JPQL or native SQL queries are added via `@Query` annotations. Named queries are avoided in favor of method name conventions for simple lookups.

JPA Entities:

- Annotated with `@Entity`, `@Table`
- UUIDs generated via `@GeneratedValue(strategy = GenerationType.UUID)` (Java 21 + Hibernate 6)
- `created_at` and `updated_at` managed via `@EntityListeners(AuditingEntityListener.class)` with `@CreatedDate` and `@LastModifiedDate`
- Soft deletes use a `deleted_at` nullable timestamp column; a Hibernate filter or repository query excludes soft-deleted records

Example entity skeleton:

```java
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 500)
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ... relationships, getters
}
```

#### Infrastructure Layer (Config, Security, Cache)

This layer contains framework configuration and integration with external systems:

- **SecurityConfig**: Defines the Spring Security filter chain, public/protected endpoints, CSRF configuration (disabled for stateless API), CORS rules
- **RedisConfig**: Configures Lettuce connection factory, `RedisTemplate<String, Object>`, and cache TTLs
- **S3Config**: Initializes the S3 client (AWS SDK v2) with endpoint, credentials, and bucket configuration
- **OpenApiConfig**: Configures Springdoc OpenAPI with JWT authentication scheme and server URLs
- **WebMvcConfig**: Configures allowed CORS origins from environment configuration

### 5.3 Spring Boot Configuration Strategy

Configuration is managed via `application.yml` with Spring profiles:

| Profile | Activation | Purpose |
|---------|-----------|---------|
| `default` | Always active | Shared base configuration |
| `dev` | `spring.profiles.active=dev` | Local development (Docker Compose) |
| `test` | Activated by Spring Test | Test database (H2 in-memory or Testcontainers) |
| `prod` | `spring.profiles.active=prod` | Production settings (strict security, no Swagger UI) |

```
src/main/resources/
├── application.yml              # Base configuration
├── application-dev.yml          # Development overrides
├── application-test.yml         # Test overrides
└── application-prod.yml         # Production overrides
```

Sensitive values (database passwords, JWT secret, S3 credentials) are never committed to source control. They are injected via environment variables using Spring's `${ENV_VAR_NAME}` syntax in `application.yml`:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

boondi:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry-ms: 900000       # 15 minutes
    refresh-token-expiry-ms: 604800000   # 7 days
  s3:
    endpoint: ${S3_ENDPOINT}
    access-key: ${S3_ACCESS_KEY}
    secret-key: ${S3_SECRET_KEY}
    bucket: ${S3_BUCKET}
```

### 5.4 Security Architecture

#### JWT Flow

Boondi uses a dual-token strategy:

- **Access Token**: Short-lived (15 minutes), used in `Authorization: Bearer <token>` header for all authenticated requests
- **Refresh Token**: Long-lived (7 days), stored in an HttpOnly cookie (web) or secure storage (Android), used to obtain new access tokens without re-authentication

```
1. User submits credentials (POST /api/v1/auth/login)
         |
         v
2. AuthService validates credentials (BCrypt compare)
         |
         v
3. JwtTokenProvider generates:
   - Access Token (15 min, signed HS256 with secret)
   - Refresh Token (7 days, stored in Redis with userId mapping)
         |
         v
4. Response: { accessToken, refreshToken, expiresIn }

--- Subsequent Requests ---

5. Client includes: Authorization: Bearer <accessToken>
         |
         v
6. JwtAuthenticationFilter intercepts request
   - Extracts token from header
   - Validates signature and expiry
   - Loads UserPrincipal from token claims (no DB lookup)
   - Sets SecurityContextHolder
         |
         v
7. Request proceeds to Controller

--- Token Refresh ---

8. Client sends: POST /api/v1/auth/refresh { refreshToken }
         |
         v
9. AuthService validates refresh token against Redis
   - If valid: issues new access token + rotates refresh token
   - If expired/invalid: returns 401, client must re-login
```

#### Spring Security Filter Chain

```
Request
   |
   +-> JwtAuthenticationFilter (OncePerRequestFilter)
   |        - Extracts Bearer token
   |        - Validates JWT signature and claims
   |        - Populates SecurityContextHolder
   |
   +-> Spring Security Authorization
   |        - Checks if endpoint requires authentication
   |        - Verifies role against @PreAuthorize or httpSecurity rules
   |
   +-> Controller (if authorized)
```

Public endpoints (no authentication required):
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `GET /actuator/health`

#### Role-Based Access Control

| Role | Permissions |
|------|------------|
| `GUEST` | Read public profiles and posts (if implemented); no write access |
| `USER` | Create/edit own posts, follow users, like, comment, bookmark, manage own profile |
| `ADMIN` | All USER permissions; delete any post/comment; manage users; access admin endpoints |

Role enforcement uses Spring Security's `@PreAuthorize` annotations on service methods and controller endpoints:

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteAnyPost(UUID postId) { ... }

@PreAuthorize("hasRole('USER')")
public PostResponse createPost(...) { ... }

// Ownership check — user can only edit their own post
@PreAuthorize("@postSecurityService.isOwner(#postId, authentication.principal.userId)")
public PostResponse updatePost(UUID postId, ...) { ... }
```

### 5.5 Caching Strategy

Redis serves as the caching layer using Spring Cache abstraction (`@Cacheable`, `@CacheEvict`, `@CachePut`).

#### What Gets Cached

| Cache Key Pattern | Content | TTL | Invalidation Trigger |
|-------------------|---------|-----|---------------------|
| `timeline:home:{userId}` | Home feed (list of post IDs) | 5 minutes | New post by followed user |
| `timeline:trending` | Trending posts (global) | 2 minutes | Periodic refresh |
| `user:profile:{userId}` | User profile response | 15 minutes | Profile update |
| `user:followers:{userId}` | Follower count | 10 minutes | Follow/Unfollow event |
| `post:{postId}` | Post detail response | 10 minutes | Post edit or delete |
| `post:likes:{postId}` | Like count | 5 minutes | Like/Unlike event |
| `auth:refresh:{token}` | Refresh token -> userId mapping | 7 days | Logout or token rotation |
| `ratelimit:{ip}:{endpoint}` | Request counter | 1 minute (sliding) | Automatic TTL expiry |

#### Cache Invalidation Approach

Cache invalidation follows an **event-driven write-through** strategy:

1. **On Write**: After any mutation (create/update/delete), the relevant cache entries are evicted via `@CacheEvict` or by direct `RedisTemplate.delete()` calls
2. **On Read Miss**: Data is fetched from PostgreSQL and populated into cache
3. **TTL-Based Expiry**: All cache entries carry a TTL as a safety net against stale data
4. **Manual Eviction**: Admin endpoints allow forced cache eviction for specific resources

Timeline caches are invalidated selectively. When User A (who is followed by User B) creates a post, the cache key `timeline:home:{userB_id}` is evicted. Timelines are not precomputed fan-out style at MVP scale.

### 5.6 Media and File Handling

#### Upload Flow

```
Client
  |
  +-- POST /api/v1/media/upload (multipart/form-data)
  |        Authorization: Bearer <token>
  |        Content-Type: multipart/form-data
  |        File: <binary>
  |
  v
MediaController
  |
  v
MediaService.uploadMedia(MultipartFile file, UUID userId)
  |
  +-- Validate: file type (JPEG, PNG, WebP only), max size (10MB)
  |
  +-- Generate: unique object key = "media/{userId}/{uuid}.{ext}"
  |
  +-- Upload to S3-compatible object store (async stream)
  |
  +-- Persist: Media entity to PostgreSQL
  |   { id, user_id, object_key, content_type, size_bytes, created_at }
  |
  +-- Return: MediaResponse { mediaId, url, contentType }
  |
Client uses mediaId when creating posts or updating profile avatar
```

#### Storage Strategy

All media files are stored in an S3-compatible object store:

- **Development**: MinIO running in Docker, accessible at `http://localhost:9000`
- **Production**: AWS S3, DigitalOcean Spaces, or equivalent

Object keys follow the pattern: `{category}/{userId}/{uuid}.{extension}`

Categories: `avatars`, `post-media`, `covers`

Media URLs served to clients are pre-signed S3 URLs or public CDN URLs (see Section 5.6 CDN).

#### CDN Considerations

At MVP scale, media is served directly from the object store. As traffic grows:

- CloudFront (AWS) or Cloudflare CDN is placed in front of the S3 bucket
- Media URLs in API responses point to CDN endpoints
- Cache-Control headers are set to long TTLs for immutable media files
- The `Media` entity stores only the object key, not the full URL, so CDN migration requires no database changes

### 5.7 Notification Architecture

#### Event-Driven Notification Creation

Notifications are created asynchronously via Spring's application event system:

```
User action triggers event:

LikeService.likePost(postId, likerId)
    |
    +-- Creates Like entity (database)
    |
    +-- Publishes: PostLikedEvent { postId, likerId, postOwnerId }
    |
    [async, @TransactionalEventListener]
    |
NotificationEventListener.onPostLiked(PostLikedEvent event)
    |
    +-- NotificationService.createNotification(
            recipientId = event.postOwnerId,
            actorId = event.likerId,
            type = LIKE,
            referenceId = event.postId
        )
    |
    +-- Persists Notification entity to PostgreSQL
    |
    +-- [Optional] Sends email notification via Email Service
    |
    +-- [Future] Sends FCM push notification to Android device
```

The `@TransactionalEventListener(phase = AFTER_COMMIT)` ensures notification creation only occurs after the triggering transaction successfully commits.

#### Notification Types

| Type | Trigger | Recipient |
|------|---------|-----------|
| `LIKE` | User likes a post | Post owner |
| `COMMENT` | User comments on a post | Post owner |
| `FOLLOW` | User follows another user | Followed user |
| `MENTION` | User is mentioned in a post/comment | Mentioned user |
| `REPLY` | User replies to a comment | Comment owner |

#### Push Notifications (Future)

- Android: Firebase Cloud Messaging (FCM) integration in `NotificationService`
- Device tokens are stored in the `User` entity (or a separate `DeviceToken` table)
- Web: Web Push API (future)

### 5.8 Search Architecture

#### MVP: PostgreSQL Full-Text Search

PostgreSQL's built-in full-text search provides adequate performance for MVP scale (< 10,000 users and posts):

**Post search** uses `tsvector` / `tsquery`:

```sql
-- Migration: add search vector column
ALTER TABLE posts ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

CREATE INDEX idx_posts_search_vector ON posts USING GIN(search_vector);

-- Query
SELECT * FROM posts
WHERE search_vector @@ plainto_tsquery('english', :query)
  AND deleted_at IS NULL
ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
LIMIT :limit OFFSET :offset;
```

**User search** matches on username and display name:

```sql
SELECT * FROM users
WHERE username ILIKE '%' || :query || '%'
   OR display_name ILIKE '%' || :query || '%'
LIMIT :limit;
```

**Hashtag search** is an exact index lookup on the `hashtags` table.

#### Future: Elasticsearch

When post volume exceeds ~100,000 or search latency degrades, Elasticsearch is introduced:

- Logstash or Debezium (CDC) syncs PostgreSQL changes to Elasticsearch
- The `SearchService` interface remains unchanged; the Elasticsearch implementation is substituted
- Enables advanced features: fuzzy matching, relevance scoring, aggregations, autocomplete

---

## 6. Database Architecture

### 6.1 Database Choice Rationale

PostgreSQL is chosen as the primary database for the following reasons:

| Factor | PostgreSQL | Alternative (MongoDB) |
|--------|------------|----------------------|
| **Data model** | Relational — suited to social graph (users, follows, likes) | Document — less efficient for relational queries |
| **ACID compliance** | Full ACID transactions | Multi-document transactions added in 4.0, less mature |
| **Full-text search** | Native `tsvector`/`GIN` indexing | Atlas Search (paid) or external |
| **JSON support** | `JSONB` for flexible fields | Native, but loses relational benefits |
| **Schema migrations** | Flyway/Liquibase — structured and versioned | Schema-less is harder to govern |
| **Operational maturity** | 35+ years, extensive tooling | Good, but less ecosystem depth |
| **Scale** | Read replicas, partitioning, citus (horizontal) | Horizontal sharding native |

PostgreSQL's relational model is the natural fit for a social network where the core data (users, posts, follows, likes) is highly relational.

### 6.2 Entity Relationship Overview

```
users (1) ----< posts (many)
users (1) ----< comments (many)
users (many) >----< users (many)    [via follows]
users (many) >----< posts (many)    [via likes]
users (many) >----< posts (many)    [via bookmarks]
posts (1) ----< comments (many)
posts (1) ----< media (many)
posts (many) >----< hashtags (many) [via post_hashtags]
users (1) ----< notifications (many) [as recipient]
```

#### Core Tables

**users**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL, indexed |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL, indexed |
| `password_hash` | VARCHAR(72) | NOT NULL |
| `role` | VARCHAR(20) | NOT NULL, DEFAULT 'USER' |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| `updated_at` | TIMESTAMPTZ | NOT NULL |
| `deleted_at` | TIMESTAMPTZ | NULL (soft delete) |

**profiles**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `user_id` | UUID | FK -> users.id, UNIQUE, NOT NULL |
| `display_name` | VARCHAR(100) | NOT NULL |
| `bio` | VARCHAR(300) | NULL |
| `avatar_media_id` | UUID | FK -> media.id, NULL |
| `cover_media_id` | UUID | FK -> media.id, NULL |
| `website_url` | VARCHAR(255) | NULL |
| `location` | VARCHAR(100) | NULL |
| `updated_at` | TIMESTAMPTZ | NOT NULL |

**posts**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `user_id` | UUID | FK -> users.id, NOT NULL, indexed |
| `content` | VARCHAR(500) | NOT NULL |
| `reply_to_post_id` | UUID | FK -> posts.id, NULL (for replies) |
| `repost_of_post_id` | UUID | FK -> posts.id, NULL (future reposts) |
| `like_count` | INTEGER | NOT NULL, DEFAULT 0 |
| `comment_count` | INTEGER | NOT NULL, DEFAULT 0 |
| `search_vector` | TSVECTOR | GENERATED, GIN indexed |
| `created_at` | TIMESTAMPTZ | NOT NULL, indexed |
| `deleted_at` | TIMESTAMPTZ | NULL (soft delete) |

**follows**

| Column | Type | Constraints |
|--------|------|-------------|
| `follower_id` | UUID | FK -> users.id, NOT NULL |
| `following_id` | UUID | FK -> users.id, NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| PK | (follower_id, following_id) | Composite primary key |

**likes**

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | UUID | FK -> users.id, NOT NULL |
| `post_id` | UUID | FK -> posts.id, NOT NULL, indexed |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| PK | (user_id, post_id) | Composite primary key (prevents duplicate likes) |

**comments**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `post_id` | UUID | FK -> posts.id, NOT NULL, indexed |
| `user_id` | UUID | FK -> users.id, NOT NULL |
| `content` | VARCHAR(500) | NOT NULL |
| `parent_comment_id` | UUID | FK -> comments.id, NULL (nested replies) |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| `deleted_at` | TIMESTAMPTZ | NULL |

**media**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `user_id` | UUID | FK -> users.id, NOT NULL |
| `object_key` | VARCHAR(500) | NOT NULL, UNIQUE |
| `content_type` | VARCHAR(50) | NOT NULL |
| `size_bytes` | BIGINT | NOT NULL |
| `width_px` | INTEGER | NULL |
| `height_px` | INTEGER | NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL |

**post_media** (join table)

| Column | Type | Constraints |
|--------|------|-------------|
| `post_id` | UUID | FK -> posts.id, NOT NULL |
| `media_id` | UUID | FK -> media.id, NOT NULL |
| `position` | INTEGER | NOT NULL, DEFAULT 0 |
| PK | (post_id, media_id) | Composite |

**notifications**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `recipient_id` | UUID | FK -> users.id, NOT NULL, indexed |
| `actor_id` | UUID | FK -> users.id, NOT NULL |
| `type` | VARCHAR(30) | NOT NULL (LIKE, COMMENT, FOLLOW, MENTION) |
| `reference_id` | UUID | NULL (post or comment ID) |
| `is_read` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `created_at` | TIMESTAMPTZ | NOT NULL, indexed |

**hashtags**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK, NOT NULL |
| `name` | VARCHAR(100) | UNIQUE, NOT NULL, indexed |
| `post_count` | INTEGER | NOT NULL, DEFAULT 0 |

**post_hashtags** (join table)

| Column | Type | Constraints |
|--------|------|-------------|
| `post_id` | UUID | FK -> posts.id |
| `hashtag_id` | UUID | FK -> hashtags.id |
| PK | (post_id, hashtag_id) | Composite |

**bookmarks**

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | UUID | FK -> users.id |
| `post_id` | UUID | FK -> posts.id |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| PK | (user_id, post_id) | Composite |

### 6.3 Key Database Design Decisions

#### UUID Primary Keys

All entities use UUID (v4) primary keys. Rationale:

- **Security**: Sequential integer IDs are trivially enumerable. UUIDs prevent API clients from guessing valid resource IDs
- **Distributed generation**: UUIDs can be generated in the application layer without a database round-trip
- **Merge-friendly**: No ID collision risk when merging data from different sources

Trade-off: UUID storage (16 bytes) is larger than BIGINT (8 bytes), and UUID primary key index pages are larger. Mitigated by using PostgreSQL's native UUID type (not VARCHAR).

#### Soft Deletes

Posts, comments, and users use soft deletes via a `deleted_at` TIMESTAMPTZ column:

- `deleted_at IS NULL` = active record
- `deleted_at IS NOT NULL` = soft-deleted

Benefits:
- Enables data recovery and audit trails
- Prevents notification reference breakage (a notification referencing a deleted post still has the row)
- Supports content moderation workflows

All repository queries must include `WHERE deleted_at IS NULL`. This is enforced via a Hibernate `@Filter` on affected entities to prevent accidental inclusion of deleted records.

#### Timestamps

All tables include:
- `created_at TIMESTAMPTZ NOT NULL` — set once at insert, never updated
- `updated_at TIMESTAMPTZ NOT NULL` — updated on every write

Both are managed via JPA Auditing (`@EnableJpaAuditing` + `@CreatedDate` + `@LastModifiedDate`). All timestamps are stored in UTC.

#### Indexing Strategy

| Table | Index | Type | Reason |
|-------|-------|------|--------|
| `users` | `(username)` | B-Tree UNIQUE | Login, profile lookup |
| `users` | `(email)` | B-Tree UNIQUE | Login, registration |
| `posts` | `(user_id, created_at DESC)` | B-Tree | User timeline queries |
| `posts` | `(created_at DESC)` | B-Tree | Global/trending timeline |
| `posts` | `(reply_to_post_id)` | B-Tree | Fetch replies to a post |
| `posts` | `(search_vector)` | GIN | Full-text search |
| `follows` | `(follower_id)` | B-Tree | "Who am I following?" |
| `follows` | `(following_id)` | B-Tree | "Who follows me?" |
| `likes` | `(post_id)` | B-Tree | Like count, check if liked |
| `notifications` | `(recipient_id, created_at DESC)` | B-Tree | Notification feed |
| `notifications` | `(recipient_id, is_read)` | B-Tree | Unread count query |
| `hashtags` | `(name)` | B-Tree UNIQUE | Hashtag lookup |

### 6.4 Redis Usage

Redis 7+ is used with persistence enabled (RDB snapshots every 15 minutes).

#### Session / Token Storage

Refresh tokens are stored as Redis string keys:

```
Key:   auth:refresh:{tokenHash}
Value: {userId}:{issuedAt}:{deviceInfo}
TTL:   7 days
```

On logout, the key is deleted immediately, invalidating the refresh token.

#### Timeline Caching

Home timeline for a user is cached as a Redis List or sorted set of post IDs:

```
Key:   timeline:home:{userId}
Type:  Redis List (ordered, most recent first)
Value: [postId1, postId2, ..., postId20]
TTL:   5 minutes
```

When the cache misses, `TimelineService` queries PostgreSQL and repopulates the cache. Post data is fetched separately per post (using individual `post:{postId}` cache entries).

Trending timeline is a global sorted set refreshed on a scheduled task (every 2 minutes):

```
Key:   timeline:trending
Type:  Redis Sorted Set (score = like_count + comment_count * 2)
TTL:   2 minutes
```

#### Rate Limiting

Rate limiting uses Redis INCR + EXPIRE pattern:

```
Key:   ratelimit:{clientIp}:{endpoint}
Value: request count (incremented per request)
TTL:   60 seconds (sliding window reset)
```

Limits applied:

| Endpoint Group | Limit |
|----------------|-------|
| `POST /auth/login` | 10 requests / minute / IP |
| `POST /auth/register` | 5 requests / minute / IP |
| `POST /posts` | 30 posts / hour / user |
| `POST /posts/{id}/likes` | 60 likes / minute / user |
| General API | 300 requests / minute / user |

---

## 7. Android Architecture

### 7.1 Architectural Pattern

The Android application follows **MVVM (Model-View-ViewModel)** combined with **Clean Architecture** layering. This is the architecture officially recommended by Google for Android development and is well-supported by Jetpack libraries.

```
UI Layer (Compose)          Domain Layer              Data Layer
+------------------+    +------------------+    +------------------+
|                  |    |                  |    |                  |
|  Composable      |    |  Use Cases       |    |  Repository      |
|  Screens         +--->|  (business       +--->|  Implementations |
|                  |    |   rules)         |    |                  |
|  ViewModels      |    |                  |    |  Retrofit API    |
|  (StateFlow)     |    |  Repository      |    |  (remote)        |
|                  |    |  Interfaces      |    |                  |
+------------------+    +------------------+    |  Room Database   |
                                                |  (local cache)   |
                                                +------------------+

Dependency Direction: UI -> Domain <- Data
```

### 7.2 Layer Structure

#### Presentation Layer (Jetpack Compose UI, ViewModels)

```
app/src/main/java/com/boondi/android/
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   │   ├── RegisterScreen.kt
│   │   └── RegisterViewModel.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── TimelineState.kt
│   ├── post/
│   │   ├── PostDetailScreen.kt
│   │   ├── PostDetailViewModel.kt
│   │   ├── CreatePostScreen.kt
│   │   └── CreatePostViewModel.kt
│   ├── profile/
│   │   ├── ProfileScreen.kt
│   │   ├── ProfileViewModel.kt
│   │   └── EditProfileScreen.kt
│   ├── search/
│   │   ├── SearchScreen.kt
│   │   └── SearchViewModel.kt
│   ├── notifications/
│   │   ├── NotificationsScreen.kt
│   │   └── NotificationsViewModel.kt
│   └── components/                     # Shared Composable components
│       ├── PostCard.kt
│       ├── UserAvatar.kt
│       ├── TopBar.kt
│       └── LoadingIndicator.kt
```

ViewModels are the interface between Compose screens and the domain layer. They:
- Expose UI state as `StateFlow<UiState<T>>`
- Call use cases (domain layer)
- Handle UI events via event functions
- Never contain business logic or directly reference data sources

#### Domain Layer (Use Cases, Repository Interfaces)

```
├── domain/
│   ├── model/                          # Pure domain models (no Android/framework dependencies)
│   │   ├── User.kt
│   │   ├── Post.kt
│   │   ├── Comment.kt
│   │   └── Notification.kt
│   ├── repository/                     # Repository interfaces (contracts)
│   │   ├── AuthRepository.kt
│   │   ├── PostRepository.kt
│   │   ├── UserRepository.kt
│   │   └── NotificationRepository.kt
│   └── usecase/                        # One use case per feature action
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   └── RegisterUseCase.kt
│       ├── post/
│       │   ├── GetTimelineUseCase.kt
│       │   ├── CreatePostUseCase.kt
│       │   ├── LikePostUseCase.kt
│       │   └── GetPostDetailUseCase.kt
│       ├── user/
│       │   ├── GetUserProfileUseCase.kt
│       │   └── FollowUserUseCase.kt
│       └── search/
│           └── SearchUseCase.kt
```

Use cases encapsulate a single business action. They depend on repository interfaces (defined in the domain layer) and return `Result<T>` or `Flow<T>`.

#### Data Layer (Retrofit API, Room Cache, Repository Implementations)

```
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApiService.kt       # Retrofit interface
│   │   │   ├── PostApiService.kt
│   │   │   ├── UserApiService.kt
│   │   │   └── NotificationApiService.kt
│   │   ├── dto/                        # API response/request data classes
│   │   │   ├── PostDto.kt
│   │   │   ├── UserDto.kt
│   │   │   └── AuthResponseDto.kt
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt      # Adds Authorization header
│   │       └── TokenRefreshInterceptor.kt  # Handles 401, refreshes token
│   ├── local/
│   │   ├── db/
│   │   │   └── BoondiDatabase.kt       # Room database
│   │   ├── dao/
│   │   │   ├── PostDao.kt
│   │   │   └── UserDao.kt
│   │   └── entity/                     # Room entity data classes
│   │       ├── PostEntity.kt
│   │       └── UserEntity.kt
│   ├── repository/                     # Implements domain repository interfaces
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── PostRepositoryImpl.kt
│   │   └── UserRepositoryImpl.kt
│   └── mapper/                         # DTO <-> Domain model conversions
│       ├── PostMapper.kt
│       └── UserMapper.kt
```

### 7.3 Dependency Injection

Hilt (built on Dagger 2) provides compile-time verified dependency injection.

```
di/
├── NetworkModule.kt        # Retrofit, OkHttpClient, interceptors
├── DatabaseModule.kt       # Room database and DAOs
├── RepositoryModule.kt     # Binds repository interfaces to implementations
└── UseCaseModule.kt        # Use case bindings (if needed)
```

Hilt injects dependencies into:
- `@HiltViewModel` — ViewModels
- `@AndroidEntryPoint` — Activities, Fragments (minimal usage; Compose-based)

### 7.4 Offline Support

Room provides local caching for offline access:

- **Posts**: Recently fetched timeline posts are cached in Room's `post_cache` table with a `cached_at` timestamp
- **User profiles**: Viewed profiles are cached in Room's `user_cache` table
- Cache freshness: entries older than 1 hour are considered stale and trigger a background refresh

The `PostRepositoryImpl` follows the **offline-first** strategy for the home timeline:

```
GetTimelineUseCase
     |
     v
PostRepositoryImpl.getHomeTimeline()
     |
     +-- 1. Emit cached posts from Room immediately (if available)
     |
     +-- 2. Fetch fresh posts from API (Retrofit)
     |
     +-- 3. Store fresh posts in Room
     |
     +-- 4. Emit updated posts from Room (via Flow)
```

This pattern uses Room's `Flow<List<PostEntity>>` to reactively update the UI when the database changes.

### 7.5 Navigation

Jetpack Navigation Compose manages the navigation graph:

```
NavHost (NavController)
├── auth_graph/
│   ├── login_screen
│   └── register_screen
└── main_graph/ (authenticated)
    ├── home_screen           (Bottom Nav)
    ├── search_screen         (Bottom Nav)
    ├── create_post_screen
    ├── notifications_screen  (Bottom Nav)
    ├── profile_screen        (Bottom Nav)
    ├── post_detail_screen
    ├── user_profile_screen
    └── edit_profile_screen
```

Deep link support: Post URLs (`boondi://posts/{postId}`) navigate directly to `post_detail_screen`.

### 7.6 State Management

ViewModels expose state as `StateFlow<UiState<T>>` where `UiState` is a sealed class:

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: Int? = null) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}
```

Compose screens collect state with `collectAsStateWithLifecycle()`:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

when (uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> TimelineContent(posts = (uiState as UiState.Success).data)
    is UiState.Error   -> ErrorMessage(message = (uiState as UiState.Error).message)
    is UiState.Empty   -> EmptyState()
}
```

One-time side effects (navigation, snackbars) are delivered via `SharedFlow<UiEvent>` to avoid re-triggering on recomposition.

---

## 8. Web Frontend Architecture

### 8.1 Architectural Pattern

The web frontend uses a **component-based architecture** with **feature-based folder organization**. State management is stratified: server state (API data) is managed by React Query; UI state by component-local `useState` and `useReducer`; global UI state (theme, auth) by React Context.

### 8.2 Folder Structure

```
src/
├── main.tsx                    # Application entry point
├── App.tsx                     # Root component, router setup
│
├── api/                        # API layer
│   ├── client.ts               # Axios instance with interceptors
│   ├── auth.ts                 # Auth API functions
│   ├── posts.ts                # Posts API functions
│   ├── users.ts                # Users API functions
│   ├── notifications.ts        # Notifications API functions
│   └── types.ts                # Shared API response types (TypeScript interfaces)
│
├── features/                   # Feature-based modules
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── hooks/
│   │   │   └── useAuth.ts
│   │   └── components/
│   │       └── AuthForm.tsx
│   ├── timeline/
│   │   ├── TimelinePage.tsx
│   │   ├── hooks/
│   │   │   └── useTimeline.ts
│   │   └── components/
│   │       ├── PostCard.tsx
│   │       └── TimelineFeed.tsx
│   ├── post/
│   │   ├── PostDetailPage.tsx
│   │   ├── CreatePostPage.tsx
│   │   └── components/
│   │       ├── PostComposer.tsx
│   │       └── CommentList.tsx
│   ├── profile/
│   │   ├── ProfilePage.tsx
│   │   ├── EditProfilePage.tsx
│   │   └── components/
│   │       └── ProfileHeader.tsx
│   ├── search/
│   │   ├── SearchPage.tsx
│   │   └── hooks/
│   │       └── useSearch.ts
│   └── notifications/
│       ├── NotificationsPage.tsx
│       └── hooks/
│           └── useNotifications.ts
│
├── components/                 # Shared, reusable UI components
│   ├── layout/
│   │   ├── AppLayout.tsx       # Main layout with sidebar + content area
│   │   ├── Sidebar.tsx
│   │   └── TopBar.tsx
│   ├── ui/
│   │   ├── Button.tsx
│   │   ├── Avatar.tsx
│   │   ├── Modal.tsx
│   │   ├── Spinner.tsx
│   │   ├── InfiniteScroll.tsx
│   │   └── ErrorBoundary.tsx
│   └── forms/
│       ├── TextInput.tsx
│       └── FileUpload.tsx
│
├── context/                    # React Context providers
│   ├── AuthContext.tsx         # Current user, token management
│   └── ThemeContext.tsx        # Light/dark mode
│
├── hooks/                      # Global shared custom hooks
│   ├── useDebounce.ts
│   ├── useIntersectionObserver.ts  # Infinite scroll
│   └── useLocalStorage.ts
│
├── lib/                        # Utilities and helpers
│   ├── queryClient.ts          # React Query client configuration
│   ├── formatters.ts           # Date, number, text formatting
│   └── validators.ts           # Client-side validation helpers
│
├── types/                      # Global TypeScript types
│   └── index.ts
│
└── styles/
    └── index.css               # Tailwind CSS imports and global styles
```

### 8.3 State Management

#### Server State: React Query

All API data is managed by TanStack React Query (v5). Benefits: automatic caching, background refetching, loading/error states, and cache invalidation.

```typescript
// Example: Timeline query
const useHomeTimeline = (cursor?: string) => {
  return useInfiniteQuery({
    queryKey: ['timeline', 'home'],
    queryFn: ({ pageParam }) => fetchHomeTimeline({ cursor: pageParam }),
    getNextPageParam: (lastPage) => lastPage.nextCursor,
    staleTime: 1000 * 60 * 2,  // 2 minutes — data considered fresh
  });
};

// Example: Create post mutation with cache invalidation
const useCreatePost = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreatePostRequest) => createPost(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['timeline', 'home'] });
    },
  });
};
```

#### Local UI State: useState / useReducer

Form state, modal open/close, UI toggles use standard React state. Complex multi-field forms use `useReducer`.

#### Global App State: React Context

- `AuthContext`: Stores the authenticated user object and access token. Token is persisted in `localStorage` (with secure alternatives considered for production). Provides `login()`, `logout()`, and `refreshToken()` functions.
- `ThemeContext`: Light/dark mode preference, persisted to `localStorage`.

### 8.4 Routing

React Router v6 manages client-side routing:

```typescript
// App.tsx routing structure
<Routes>
  {/* Public routes */}
  <Route path="/login" element={<LoginPage />} />
  <Route path="/register" element={<RegisterPage />} />

  {/* Protected routes */}
  <Route element={<ProtectedRoute />}>
    <Route element={<AppLayout />}>
      <Route path="/" element={<TimelinePage />} />
      <Route path="/search" element={<SearchPage />} />
      <Route path="/notifications" element={<NotificationsPage />} />
      <Route path="/posts/:postId" element={<PostDetailPage />} />
      <Route path="/users/:username" element={<ProfilePage />} />
      <Route path="/settings/profile" element={<EditProfilePage />} />
    </Route>
  </Route>

  <Route path="*" element={<NotFoundPage />} />
</Routes>
```

`ProtectedRoute` checks `AuthContext` for a valid token; unauthenticated users are redirected to `/login`.

### 8.5 API Layer

All HTTP communication goes through a centralized Axios instance:

```typescript
// api/client.ts
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach access token
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor: handle 401, refresh token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true;
      const newToken = await refreshAccessToken();
      error.config.headers.Authorization = `Bearer ${newToken}`;
      return apiClient(error.config);
    }
    return Promise.reject(error);
  }
);
```

### 8.6 Styling

Tailwind CSS provides utility-first styling:

- **Design tokens**: Defined in `tailwind.config.js` — brand colors, font scales, spacing
- **Component classes**: Applied directly in JSX; no separate CSS files per component
- **Dark mode**: `class` strategy — toggling `dark` class on `<html>` switches color scheme
- **Responsive**: Mobile-first breakpoints (`sm:`, `md:`, `lg:`, `xl:`)

```javascript
// tailwind.config.js
theme: {
  extend: {
    colors: {
      brand: {
        primary:   '#1D4ED8',  // Main blue
        secondary: '#7C3AED',  // Accent purple
        surface:   '#F8FAFC',  // Background
      }
    },
    fontFamily: {
      sans: ['Inter', 'system-ui', 'sans-serif'],
    }
  }
}
```

---

## 9. API Architecture

### 9.1 REST API Design

The Boondi REST API follows these conventions:

| Convention | Rule |
|------------|------|
| Base path | `/api/v1/` |
| Resource names | Plural nouns: `/posts`, `/users`, `/comments` |
| HTTP verbs | `GET` read, `POST` create, `PUT` replace, `PATCH` update, `DELETE` remove |
| Content-Type | `application/json` for all requests and responses |
| Authentication | `Authorization: Bearer <access_token>` header |
| IDs | UUID in path parameters: `/posts/{postId}` |
| Pagination | Cursor-based (timelines) or page-based (search) |
| Status codes | Standard HTTP codes (200, 201, 400, 401, 403, 404, 409, 422, 500) |

### 9.2 Endpoint Catalog

#### Authentication — `/api/v1/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | None | Register a new user account |
| POST | `/auth/login` | None | Authenticate and receive tokens |
| POST | `/auth/refresh` | None | Refresh access token |
| POST | `/auth/logout` | Bearer | Revoke refresh token |
| POST | `/auth/forgot-password` | None | Initiate password reset (email) |
| POST | `/auth/reset-password` | None | Complete password reset with token |

#### Users — `/api/v1/users`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/users/{userId}` | Optional | Get user profile by UUID |
| GET | `/users/by-username/{username}` | Optional | Get user profile by username |
| PATCH | `/users/{userId}` | Bearer (owner/admin) | Update profile fields |
| DELETE | `/users/{userId}` | Bearer (admin) | Soft-delete user account |
| GET | `/users/{userId}/followers` | Optional | List followers |
| GET | `/users/{userId}/following` | Optional | List following |
| POST | `/users/{userId}/follow` | Bearer | Follow a user |
| DELETE | `/users/{userId}/follow` | Bearer | Unfollow a user |
| GET | `/users/{userId}/posts` | Optional | Get user's posts |
| GET | `/users/me` | Bearer | Get current authenticated user |

#### Posts — `/api/v1/posts`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/posts` | Bearer | Create a new post |
| GET | `/posts/{postId}` | Optional | Get a single post |
| PATCH | `/posts/{postId}` | Bearer (owner) | Edit post content |
| DELETE | `/posts/{postId}` | Bearer (owner/admin) | Soft-delete a post |
| GET | `/posts/{postId}/replies` | Optional | Get replies to a post |
| POST | `/posts/{postId}/replies` | Bearer | Create a reply |
| GET | `/posts/{postId}/likes` | Optional | Get users who liked a post |
| POST | `/posts/{postId}/like` | Bearer | Like a post |
| DELETE | `/posts/{postId}/like` | Bearer | Unlike a post |
| GET | `/posts/{postId}/bookmarks` | Bearer | Check bookmark status |
| POST | `/posts/{postId}/bookmark` | Bearer | Bookmark a post |
| DELETE | `/posts/{postId}/bookmark` | Bearer | Remove bookmark |

#### Timeline — `/api/v1/timeline`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/timeline/home` | Bearer | Home feed (following + self) |
| GET | `/timeline/trending` | Optional | Trending posts (global) |
| GET | `/timeline/user/{userId}` | Optional | User-specific timeline |

#### Notifications — `/api/v1/notifications`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/notifications` | Bearer | Get notifications (paginated) |
| GET | `/notifications/unread-count` | Bearer | Get unread notification count |
| PATCH | `/notifications/{notificationId}/read` | Bearer | Mark notification as read |
| PATCH | `/notifications/read-all` | Bearer | Mark all notifications as read |

#### Search — `/api/v1/search`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/search?q={query}&type={posts\|users\|hashtags}` | Optional | Search across resource types |
| GET | `/search/hashtags/{hashtag}` | Optional | Get posts by hashtag |

#### Media — `/api/v1/media`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/media/upload` | Bearer | Upload a media file (multipart) |
| DELETE | `/media/{mediaId}` | Bearer (owner/admin) | Delete a media file |

### 9.3 Request and Response Format

#### Standard Success Response

```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-07-02T10:30:00Z",
    "requestId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

#### Paginated Response

```json
{
  "data": [ { ... }, { ... } ],
  "pagination": {
    "nextCursor": "eyJpZCI6IjEyMzQ1IiwiY3JlYXRlZEF0IjoiMjAyNi0wNy0wMlQxMDowMDowMFoifQ==",
    "hasMore": true,
    "pageSize": 20
  },
  "meta": {
    "timestamp": "2026-07-02T10:30:00Z"
  }
}
```

#### Post Response Example

```json
{
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "content": "Hello from Boondi! #firstpost",
    "author": {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "username": "alice",
      "displayName": "Alice Smith",
      "avatarUrl": "https://cdn.boondi.app/avatars/660e8400.jpg"
    },
    "media": [
      {
        "id": "770e8400-e29b-41d4-a716-446655440002",
        "url": "https://cdn.boondi.app/post-media/770e8400.jpg",
        "contentType": "image/jpeg",
        "width": 1080,
        "height": 1080
      }
    ],
    "hashtags": ["firstpost"],
    "likeCount": 12,
    "commentCount": 3,
    "isLiked": true,
    "isBookmarked": false,
    "replyToPostId": null,
    "createdAt": "2026-07-02T10:00:00Z",
    "updatedAt": "2026-07-02T10:00:00Z"
  }
}
```

### 9.4 Authentication Flow

```
1. POST /api/v1/auth/login
   Request:  { "username": "alice", "password": "secret" }
   Response: {
     "data": {
       "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
       "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
       "tokenType": "Bearer",
       "expiresIn": 900,
       "user": { "id": "...", "username": "alice", "role": "USER" }
     }
   }

2. Authenticated Request:
   GET /api/v1/posts/123
   Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

3. Token Expired (401 received):
   POST /api/v1/auth/refresh
   Request:  { "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..." }
   Response: {
     "data": {
       "accessToken": "eyJhbGciOiJIUzI1NiJ9...[new]",
       "refreshToken": "aGVsbG8gdGhpcyBpcyBuZXc...[rotated]",
       "expiresIn": 900
     }
   }

4. Logout:
   POST /api/v1/auth/logout
   Headers: Authorization: Bearer <accessToken>
   Body:    { "refreshToken": "..." }
   Action:  Refresh token deleted from Redis. Access token expires naturally (15 min TTL).
```

### 9.5 Pagination Strategy

Two pagination strategies are employed:

#### Cursor-Based Pagination (Timelines)

Used for the home feed, trending feed, and user timelines. Prevents duplicate/missing items when new posts are created between page requests.

```
Request:  GET /api/v1/timeline/home?cursor=&limit=20
Response: { data: [...20 posts], pagination: { nextCursor: "base64encoded...", hasMore: true } }

Next page: GET /api/v1/timeline/home?cursor=base64encoded...&limit=20
```

The cursor encodes the `(created_at, id)` of the last returned item. The server uses this to query `WHERE (created_at, id) < (cursor.created_at, cursor.id)` for efficient keyset pagination, which avoids costly `OFFSET` clauses.

#### Page-Based Pagination (Search, User Lists)

Used for search results, follower lists, and following lists where random access is expected.

```
Request:  GET /api/v1/search?q=hello&type=posts&page=0&size=20
Response: {
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 142,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 9.6 Error Handling

All errors return a standard JSON error envelope:

```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Post with ID 550e8400... was not found",
    "status": 404,
    "timestamp": "2026-07-02T10:30:00Z",
    "requestId": "req-abc-123",
    "details": []
  }
}
```

For validation errors:

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "status": 422,
    "timestamp": "2026-07-02T10:30:00Z",
    "requestId": "req-abc-123",
    "details": [
      { "field": "content", "message": "must not be blank" },
      { "field": "content", "message": "size must be between 1 and 500" }
    ]
  }
}
```

#### HTTP Status Code Mapping

| Status | Code Constant | Usage |
|--------|---------------|-------|
| 200 | — | Successful GET, PATCH, DELETE |
| 201 | — | Successful POST (resource created) |
| 204 | — | Successful DELETE with no body |
| 400 | `BAD_REQUEST` | Malformed request body or parameters |
| 401 | `UNAUTHORIZED` | Missing or invalid access token |
| 403 | `FORBIDDEN` | Authenticated but insufficient permissions |
| 404 | `RESOURCE_NOT_FOUND` | Resource does not exist |
| 409 | `DUPLICATE_RESOURCE` | Conflict (e.g., already liked, username taken) |
| 422 | `VALIDATION_FAILED` | Request passed parsing but failed validation |
| 429 | `RATE_LIMIT_EXCEEDED` | Too many requests |
| 500 | `INTERNAL_SERVER_ERROR` | Unexpected server error |

### 9.7 API Versioning Strategy

| Strategy | Decision |
|----------|----------|
| Version scheme | URL path prefix: `/api/v1/`, `/api/v2/` |
| Breaking change definition | Removing fields, changing field types, changing semantics of existing endpoints |
| Non-breaking changes | Adding new optional fields, new endpoints — no version bump needed |
| Deprecation policy | Deprecated versions run for a minimum of 6 months with `Deprecation` response header |
| Current version | v1 (all MVP endpoints) |

---

## 10. Infrastructure and Deployment

### 10.1 Development Environment

Docker Compose provides a complete local development environment:

```yaml
# docker-compose.yml (development)
version: '3.9'
services:

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: boondi_dev
      POSTGRES_USER: boondi
      POSTGRES_PASSWORD: boondi_dev_secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"   # MinIO console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

  api:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DATABASE_URL: jdbc:postgresql://postgres:5432/boondi_dev
      DATABASE_USER: boondi
      DATABASE_PASSWORD: boondi_dev_secret
      REDIS_HOST: redis
      REDIS_PORT: 6379
      S3_ENDPOINT: http://minio:9000
      S3_ACCESS_KEY: minioadmin
      S3_SECRET_KEY: minioadmin123
      JWT_SECRET: dev-only-secret-replace-in-production
    depends_on:
      - postgres
      - redis
      - minio

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.dev.conf:/etc/nginx/nginx.conf:ro
      - ./web/dist:/usr/share/nginx/html:ro
    depends_on:
      - api

volumes:
  postgres_data:
  redis_data:
  minio_data:
```

**Developer Workflow**:
1. `docker compose up -d postgres redis minio` — start data services
2. `./gradlew bootRun --args='--spring.profiles.active=dev'` — run API locally (hot reload with Spring DevTools)
3. `npm run dev` — run Vite dev server for web (proxies `/api/*` to `localhost:8080`)
4. Android: point Retrofit base URL to `http://10.0.2.2:8080` (Android emulator localhost)

### 10.2 Production Architecture

```
                              +--------------+
                              |   DNS / CDN  |
                              |  (Route 53 / |
                              |  Cloudflare) |
                              +--------------+
                                     |
                                     | :443
                              +------v-------+
                              |    Nginx     |
                              |  (TLS term)  |
                              |  Static Web  |
                              +------+-------+
                                     |
                              +------v-------+
                              | Spring Boot  |
                              | API :8080    |
                              +--+-------+---+
                                 |       |
                    +------------+       +-----------+
                    |                               |
             +------v-----+              +----------v---+
             | PostgreSQL |              |    Redis     |
             | :5432      |              |    :6379     |
             +------------+              +--------------+
                    |
             +------v-----+              +-------------+
             | Backups    |              | S3 Object   |
             | (pg_dump + |              | Store       |
             |  WAL arch) |              | (Media)     |
             +------------+              +-------------+
```

### 10.3 Docker Configuration Overview

**Production multi-stage Dockerfile (backend)**:

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S boondi && adduser -S boondi -G boondi
COPY --from=builder /app/build/libs/*.jar app.jar
USER boondi
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

**Production multi-stage Dockerfile (web)**:

```dockerfile
# Build stage
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime: Nginx serves static files
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx/spa.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 10.4 Nginx Configuration Role

Nginx serves four purposes in production:

**1. SSL/TLS Termination** — Terminates HTTPS at the edge; internal communication is plain HTTP on the Docker network.

**2. Reverse Proxy** — Routes API requests to Spring Boot:

```nginx
server {
    listen 443 ssl http2;
    server_name app.boondi.social;

    ssl_certificate     /etc/letsencrypt/live/app.boondi.social/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/app.boondi.social/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
    add_header Content-Security-Policy "default-src 'self'; img-src 'self' data: https://cdn.boondi.social;";

    # API reverse proxy
    location /api/ {
        proxy_pass         http://api:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
        proxy_read_timeout 30s;
    }

    # React SPA — serve index.html for all routes
    location / {
        root  /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        expires 1h;
        add_header Cache-Control "public, max-age=3600";
    }

    # Static assets — long cache
    location ~* \.(js|css|png|ico|woff2)$ {
        root   /usr/share/nginx/html;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}

# HTTP redirect to HTTPS
server {
    listen 80;
    server_name app.boondi.social;
    return 301 https://$server_name$request_uri;
}
```

**3. Static File Serving** — Serves the built React SPA directly from the Nginx process without involving Spring Boot.

**4. Gzip Compression** — Compresses API responses and static assets before delivery.

### 10.5 Logging and Monitoring Strategy

#### Structured Logging (Backend)

Spring Boot uses Logback with JSON output in production:

```xml
<!-- logback-spring.xml (production profile) -->
<configuration>
  <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <includeMdcKeyName>requestId</includeMdcKeyName>
      <includeMdcKeyName>userId</includeMdcKeyName>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="JSON_STDOUT" />
  </root>
</configuration>
```

Log fields:
- `timestamp` — ISO 8601 UTC
- `level` — DEBUG / INFO / WARN / ERROR
- `logger` — Class name
- `message` — Human-readable description
- `requestId` — UUID injected via MDC per request
- `userId` — Authenticated user ID (if applicable)
- `duration_ms` — For timed operations

#### Monitoring Stack (MVP)

| Tool | Purpose |
|------|---------|
| **Spring Boot Actuator** | Health checks (`/actuator/health`), metrics, info endpoints |
| **Prometheus** | Metrics scraping from Actuator Micrometer endpoint |
| **Grafana** | Dashboard visualization (latency, error rate, JVM memory, DB pool) |
| **Loki** | Log aggregation (optional, replaces ELK for small scale) |
| **Uptime Kuma** | Simple uptime monitoring with alerts |

#### Key Metrics Tracked

- API request latency (p50, p95, p99) per endpoint
- HTTP error rate (4xx, 5xx)
- JVM heap usage, GC pause time
- Database connection pool utilization
- Redis cache hit/miss ratio
- Active user count (daily/weekly)

### 10.6 Future: Kubernetes Migration Path

The production Docker Compose setup is designed to be migrated to Kubernetes with minimal changes:

| Docker Compose Concept | Kubernetes Equivalent |
|------------------------|----------------------|
| `service` | `Deployment` + `Service` |
| `environment` | `ConfigMap` + `Secret` |
| `volumes` | `PersistentVolumeClaim` |
| `depends_on` | `readinessProbe` + `livenessProbe` |
| `ports` | `Ingress` + `Service` (`ClusterIP`) |

Migration steps:
1. Convert Docker Compose to Kubernetes manifests (Kompose tool)
2. Replace Nginx container with Kubernetes `Ingress` (NGINX Ingress Controller)
3. Move secrets to Kubernetes `Secret` objects (or external: Vault, AWS Secrets Manager)
4. Enable HPA for the Spring Boot `Deployment` (scale on CPU/memory)
5. Deploy PostgreSQL as StatefulSet or use managed service (AWS RDS, Google Cloud SQL)
6. Deploy Redis as StatefulSet or use managed service (ElastiCache)

---

## 11. Security Architecture

### 11.1 Authentication and Authorization

Authentication is handled via JWT as described in Section 5.4. Key security properties:

| Property | Implementation |
|----------|---------------|
| Token signing | HMAC-SHA256 with a 256-bit secret key stored in environment variable |
| Access token lifetime | 15 minutes — limits exposure window if token is intercepted |
| Refresh token lifetime | 7 days — stored in Redis; revocable on logout |
| Token claims | `sub` (userId), `username`, `role`, `iat`, `exp` — minimal, no sensitive data |
| Token storage (web) | Access token in memory (JavaScript variable); refresh token in HttpOnly cookie |
| Token storage (Android) | Access token in memory; refresh token in Android EncryptedSharedPreferences |
| Password hashing | BCrypt with cost factor 12 |
| Account lockout | Future: lock after 5 failed login attempts within 15 minutes |

Authorization uses RBAC as described in Section 5.4. All sensitive operations additionally verify resource ownership at the service layer.

### 11.2 Data Security

| Layer | Mechanism |
|-------|-----------|
| **In transit** | TLS 1.2+ enforced at Nginx; HSTS header with `max-age=31536000` |
| **At rest (DB)** | PostgreSQL data-at-rest encryption via OS-level disk encryption (LUKS on Linux) |
| **At rest (media)** | S3 server-side encryption (AES-256) enabled on all buckets |
| **At rest (Redis)** | Redis data stored on encrypted volume; AOF/RDB files on encrypted disk |
| **Passwords** | BCrypt hash only — plaintext passwords are never stored or logged |
| **Secrets** | Environment variables only — never in source code, config files, or Docker images |
| **PII in logs** | Email addresses, passwords, and tokens are never written to logs |

### 11.3 Input Validation and Sanitization

All input is validated at two layers:

**Client-side validation** (web and Android) provides immediate feedback but is not trusted for security.

**Server-side validation** (mandatory) is enforced via Jakarta Validation annotations on DTOs:

```java
public record CreatePostRequest(
    @NotBlank @Size(min = 1, max = 500) String content,
    @Size(max = 4) List<@NotNull UUID> mediaIds
) {}
```

Global validation policies:
- All string inputs are trimmed of leading/trailing whitespace
- HTML is stripped from all user-provided text (JSoup or HtmlSanitizer)
- File uploads: MIME type and extension validation; content sniffing with Apache Tika
- SQL injection: prevented by JPA parameterized queries (no string concatenation in SQL)
- Path traversal: object storage keys are generated server-side; user input is never used as a file path

### 11.4 Rate Limiting

Rate limiting is implemented at two layers:

**Nginx layer**: `limit_req_zone` directive limits connections per IP:

```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=100r/m;
location /api/ {
    limit_req zone=api burst=20 nodelay;
}
```

**Application layer** (Redis-backed): Per-user and per-endpoint limits as described in Section 6.4. A `RateLimitingFilter` (Spring `HandlerInterceptor`) checks Redis counters before processing requests.

### 11.5 OWASP Top 10 Mitigations

| OWASP Risk | Mitigation |
|------------|-----------|
| **A01 Broken Access Control** | RBAC via Spring Security; ownership checks on all mutations; `@PreAuthorize` on service methods |
| **A02 Cryptographic Failures** | TLS 1.2+ everywhere; BCrypt for passwords; AES-256 at rest; no weak ciphers |
| **A03 Injection** | JPA parameterized queries; input validation; HTML sanitization; no eval/raw SQL |
| **A04 Insecure Design** | Threat modeled design; principle of least privilege; RBAC; rate limiting |
| **A05 Security Misconfiguration** | Spring Security defaults hardened; Swagger UI disabled in prod; actuator endpoints restricted; security headers via Nginx |
| **A06 Vulnerable Components** | Dependabot alerts; `./gradlew dependencyCheckAnalyze`; regular dependency updates |
| **A07 Auth Failures** | Short-lived tokens; refresh token rotation; logout invalidation; rate limit on login |
| **A08 Software Integrity Failures** | Docker image digests pinned; dependency verification in Gradle; CI pipeline for builds |
| **A09 Logging Failures** | Structured logging; all auth events logged; no sensitive data in logs; centralized log storage |
| **A10 SSRF** | Outbound HTTP calls are restricted to known external services; no user-controlled URLs in server-side fetches |

---

## 12. Cross-Cutting Concerns

### 12.1 Logging

Logging follows these standards across the entire application:

#### Log Levels

| Level | Usage |
|-------|-------|
| `ERROR` | Unrecoverable errors, exceptions requiring immediate attention, data integrity issues |
| `WARN` | Recoverable issues, deprecated usage, failed external calls with fallback |
| `INFO` | Significant business events (user registered, post created, follow action), request/response summary |
| `DEBUG` | Detailed diagnostic information; disabled in production |
| `TRACE` | SQL query logging, cache operations; only for local debugging |

#### MDC Context

A `RequestLoggingFilter` populates the Mapped Diagnostic Context (MDC) at the start of each request:

```java
MDC.put("requestId", UUID.randomUUID().toString());
MDC.put("userId", extractUserId(request));  // null for unauthenticated
MDC.put("path", request.getRequestURI());
MDC.put("method", request.getMethod());
```

All log statements automatically include these fields, enabling log correlation.

#### What is Logged

| Event | Level | Fields |
|-------|-------|--------|
| Application startup | INFO | Version, active profiles, port |
| Authentication success | INFO | userId, username, ip |
| Authentication failure | WARN | username (not password), ip, reason |
| Request received | DEBUG | method, path, userId |
| Request completed | INFO | method, path, status, duration_ms |
| Database query slow (>100ms) | WARN | query, duration_ms |
| Cache miss | DEBUG | cacheKey |
| External service error | ERROR | service, endpoint, statusCode, message |
| Unhandled exception | ERROR | exception class, message, stack trace |

### 12.2 Error Handling

A single `@RestControllerAdvice` class (`GlobalExceptionHandler`) intercepts all exceptions thrown from controllers and services:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = extractFieldErrors(ex.getBindingResult());
        return buildValidationError(fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred");
    }
}
```

Importantly, the generic handler logs the full stack trace but returns a safe, non-revealing message to the client.

### 12.3 Validation

Input validation is applied at all entry points:

| Layer | Mechanism |
|-------|----------|
| Web client | HTML5 form validation + React hook form validation |
| Android client | Compose form state validation before API call |
| API controller | `@Valid` on `@RequestBody` triggers Jakarta Validation |
| Service layer | Business rule validation (e.g., cannot follow yourself) |
| Database | NOT NULL, UNIQUE, CHECK constraints as final safety net |

Common constraints used:
- `@NotBlank`, `@NotNull`, `@NotEmpty` — presence validation
- `@Size(min=, max=)` — length constraints
- `@Email` — email format
- `@Pattern(regexp=)` — username format (`^[a-zA-Z0-9_]{3,30}$`)
- `@Positive` — positive numbers
- Custom validators for cross-field validation (e.g., password confirmation match)

### 12.4 Internationalization

**MVP**: English only. All API error messages and response strings are in English.

**Future (i18n)**: Spring's `MessageSource` is prepared with externalized message keys. Response error codes (not messages) are returned to clients, allowing client-side localization. The `Accept-Language` header will be respected.

### 12.5 Configuration Management

Environment-specific configuration is managed via Spring profiles:

```yaml
# application.yml — shared base
spring:
  application:
    name: boondi-api
  jpa:
    hibernate:
      ddl-auto: validate   # Flyway manages schema; never auto-create in any env
  flyway:
    enabled: true
    locations: classpath:db/migration

logging:
  level:
    com.boondi: INFO

# application-dev.yml — development overrides
spring:
  jpa:
    show-sql: true
  flyway:
    enabled: true
logging:
  level:
    com.boondi: DEBUG
    org.hibernate.SQL: DEBUG

# application-prod.yml — production overrides
spring:
  jpa:
    show-sql: false
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /internal/actuator
```

Database schema is managed via **Flyway** migrations in `src/main/resources/db/migration/`, using the naming convention `V{version}__{description}.sql` (e.g., `V1__create_users_table.sql`).

---

## 13. Architecture Decision Records

### ADR-001: PostgreSQL as Primary Database

**Status**: Accepted

**Context**:
The Boondi data model is fundamentally relational: users follow users, users like posts, posts have comments, comments belong to users. The platform requires ACID transactions (a "like" must increment the count and create the record atomically), full-text search, and complex multi-table join queries for timeline generation. Alternative NoSQL databases (MongoDB, DynamoDB, Cassandra) were evaluated.

**Decision**:
Use PostgreSQL 15+ as the sole relational database for all persistent structured data.

**Consequences**:
- Positive: Strong ACID guarantees, native full-text search via GIN indexes and `tsvector`, JSON/JSONB support for flexible fields, rich querying capability with window functions and CTEs, mature tooling (Flyway, HikariCP, pgAdmin), excellent Spring Data JPA support
- Positive: Schema evolution is governed and auditable via Flyway migrations
- Negative: Horizontal write scaling requires partitioning or Citus (added complexity); mitigated by expected scale
- Negative: Relational schema changes require migrations; mitigated by Flyway's versioned approach
- Accepted: For the expected scale (< 100,000 users), PostgreSQL vertical scaling and read replicas are sufficient before any horizontal strategy is needed

---

### ADR-002: JWT-Based Stateless Authentication

**Status**: Accepted

**Context**:
Authentication strategy must support both Android and web clients consuming the same API. Traditional server-side sessions (stored in database or memory) require sticky sessions or a shared session store, complicating horizontal scaling. OAuth 2.0 delegated authentication was evaluated but deemed excessive for a private platform with no third-party logins required.

**Decision**:
Use JWT (JSON Web Tokens) with a dual-token strategy: short-lived access tokens (15 minutes) for API calls and long-lived refresh tokens (7 days) stored in Redis for reissuance.

**Consequences**:
- Positive: Stateless API — any server instance can validate any token without shared state, enabling horizontal scaling
- Positive: Single auth mechanism works for both Android and web clients
- Positive: Refresh token in Redis allows immediate revocation (logout, account suspension) despite JWT's inherent non-revocability
- Positive: JWT claims carry userId and role — no database lookup per request
- Negative: Access tokens cannot be individually revoked before expiry (15-minute maximum exposure window accepted)
- Negative: Refresh token rotation requires Redis availability; mitigated by Redis persistence and the short impact window
- Accepted: The 15-minute access token TTL provides acceptable security with minimal user disruption

---

### ADR-003: UUID Primary Keys

**Status**: Accepted

**Context**:
Primary key strategy affects security (ID enumeration), performance (index size), and distributed system compatibility. Sequential integer IDs (BIGSERIAL) are the default PostgreSQL choice but expose resource counts and enable enumeration attacks (e.g., iterating user IDs to scrape all profiles).

**Decision**:
Use UUID v4 (randomly generated) as primary keys for all entities. Generated in the Java application layer using `UUID.randomUUID()` and stored as native PostgreSQL UUID type (not VARCHAR).

**Consequences**:
- Positive: Eliminates sequential ID enumeration attacks — resource IDs cannot be guessed
- Positive: Application-layer generation means IDs are known before database insert (simplifies event publishing)
- Positive: PostgreSQL native UUID type is 16 bytes (same as a BIGINT index entry in a 16-byte UUID vs. 8-byte BIGINT comparison — difference is acceptable)
- Negative: UUID indexes have lower cache hit rates than sequential integer indexes due to random insertion order, causing more B-tree splits; mitigated at expected scale
- Negative: UUID v4 is not time-sortable (unlike ULIDs or UUID v7); `created_at` column is used for ordering instead
- Future: May migrate to UUID v7 (time-ordered) if index fragmentation becomes a measured problem

---

### ADR-004: Clean Architecture with Layered Monolith

**Status**: Accepted

**Context**:
Architectural scope options considered: (a) Microservices — independent services per domain (Auth, Posts, Notifications); (b) Modular monolith — separate modules within one deployable; (c) Layered monolith — single application with layered structure. Initial team is small; operational complexity of microservices is not justified at MVP.

**Decision**:
Build Boondi as a **layered monolith** following Clean Architecture principles (dependency inversion, clear layer boundaries). The application has a single deployable artifact. Architecture is designed for future microservices extraction via well-defined service interfaces.

**Consequences**:
- Positive: Simplest operational model (one process to deploy, monitor, scale)
- Positive: No network overhead between "services" (function calls instead)
- Positive: Single database transaction scope across all operations
- Positive: Clean Architecture layer boundaries preserve the option to extract microservices later without rewriting business logic
- Negative: Single point of failure if not deployed redundantly
- Negative: All features must use the same technology stack
- Accepted: At 10–10,000 users, a monolith is the correct choice. Microservices are listed as a future consideration (see Section 14.2)

---

### ADR-005: React 18 with TypeScript for Web Frontend

**Status**: Accepted

**Context**:
Web frontend framework options evaluated: (a) React 18 — large ecosystem, strong TypeScript support, React Query for server state; (b) Vue 3 — smaller ecosystem, good TypeScript support; (c) Next.js — server-side rendering, SEO capabilities; (d) SvelteKit — minimal boilerplate, smaller bundle.

**Decision**:
Use **React 18** with TypeScript, Vite (build tool), Tailwind CSS (styling), and TanStack React Query (server state management).

**Consequences**:
- Positive: Largest talent pool and ecosystem of the evaluated options
- Positive: TanStack React Query eliminates significant boilerplate for API data fetching, caching, and synchronization
- Positive: Vite provides sub-second HMR in development and optimized production builds
- Positive: Tailwind CSS eliminates CSS naming conflicts and enables rapid, consistent UI development
- Positive: TypeScript catches type errors at compile time, improving API contract adherence between frontend and backend
- Negative: React has a steeper learning curve for developers unfamiliar with hooks and functional patterns
- Negative: No built-in SSR (Boondi is a private platform; SEO is not a requirement, so this is accepted)
- Accepted: React's maturity, ecosystem, and team familiarity make it the strongest choice

---

### ADR-006: Redis for Caching and Rate Limiting

**Status**: Accepted

**Context**:
A caching layer is required to meet the <300ms API response time target without placing excessive read load on PostgreSQL, particularly for timeline generation which involves complex multi-join queries. Options evaluated: (a) In-process cache (Caffeine/Guava) — no network overhead but not shared across instances; (b) Redis — external shared cache; (c) Memcached — simpler key-value, no persistence.

**Decision**:
Use **Redis 7+** as the shared cache and rate limiting store. Redis also stores refresh token mappings (replacing a database tokens table).

**Consequences**:
- Positive: Shared cache works correctly when multiple API instances run behind a load balancer (unlike in-process cache)
- Positive: Redis data structures (Lists, Sorted Sets, Strings with TTL) are well-suited to timeline caching and rate limiting
- Positive: Refresh token storage in Redis provides O(1) lookup and immediate invalidation without database writes on every request
- Positive: Redis persistence (RDB + AOF) prevents complete cache loss on restart
- Negative: Adds an additional infrastructure dependency; mitigated by Docker and managed service options
- Negative: Cache invalidation logic adds code complexity; mitigated by Spring Cache abstraction
- Accepted: The performance benefits and horizontal scaling enablement justify the added operational component

---

## 14. Future Architecture Considerations

### 14.1 Scaling Strategy

Boondi's architecture is designed to scale incrementally without re-architecture:

#### Phase 1: Vertical Scaling (Current — 0 to ~1,000 users)
- Single server with ample CPU/RAM
- Single PostgreSQL instance
- Single Redis instance
- Expected to handle this phase comfortably

#### Phase 2: Read Replicas (1,000 to ~10,000 users)
- Add PostgreSQL read replica(s); route read-heavy queries (timelines, search) to replicas
- Spring's `AbstractRoutingDataSource` routes reads to replicas and writes to primary
- Redis remains single instance (Sentinel for HA)

#### Phase 3: Horizontal API Scaling (5,000+ users)
- Deploy multiple Spring Boot instances behind a load balancer (Nginx upstream or Kubernetes HPA)
- Stateless JWT design already supports this with zero changes
- Session affinity not required (Redis holds all shared state)

#### Phase 4: Database Partitioning (10,000+ users)
- Partition `posts` and `notifications` tables by `created_at` (range partitioning) for performance
- Evaluate read-heavy tables for CQRS: maintain denormalized read models
- Connection pooling via PgBouncer between API and PostgreSQL

```
Phase 2+ Production Architecture:

Load Balancer (Nginx / HAProxy)
    |           |
  API-1       API-2         (horizontal scaling)
    |           |
    +-----------+
          |
     +----+--------+
     |             |
 PG Primary    PG Replica    (reads go to replica)
     |
  Failover (Patroni or pg_auto_failover)
```

### 14.2 Microservices Migration Path

If scale or team size demands it, the monolith can be decomposed into microservices along domain boundaries. The Clean Architecture layer structure makes service boundaries explicit:

| Domain Service | Responsibilities | Data |
|----------------|-----------------|------|
| `auth-service` | Registration, login, token management | `users`, `tokens` |
| `post-service` | Post CRUD, media, hashtags | `posts`, `media`, `hashtags` |
| `social-service` | Follows, likes, bookmarks | `follows`, `likes`, `bookmarks` |
| `notification-service` | Notification creation and delivery | `notifications` |
| `timeline-service` | Feed generation and caching | Read-only from other services |
| `search-service` | Full-text search (Elasticsearch) | Materialized search index |

Migration sequence (strangler fig pattern):
1. Extract `notification-service` first (already event-driven, minimal coupling)
2. Extract `auth-service` (well-defined interface)
3. Introduce API Gateway (Kong or Spring Cloud Gateway)
4. Extract remaining services as team capacity allows

Inter-service communication: REST for synchronous (auth validation), message queue (Kafka) for async (notification events).

### 14.3 Message Queue

At MVP, notifications are created synchronously via Spring Application Events (in-process). As the platform scales, this must become truly asynchronous and durable:

**Recommended**: **Apache Kafka** for event streaming

| Use Case | Topic | Producers | Consumers |
|----------|-------|-----------|-----------|
| Post created | `boondi.posts.created` | PostService | TimelineService, NotificationService, SearchIndexer |
| Post liked | `boondi.posts.liked` | LikeService | NotificationService, TrendingService |
| User followed | `boondi.users.followed` | FollowService | NotificationService, TimelineService |
| Notification | `boondi.notifications` | NotificationService | EmailWorker, PushWorker |

Benefits:
- Decouples producers from consumers
- Durable message persistence — notifications not lost if consumer is down
- Fan-out to multiple consumers from a single event
- Event replay capability for rebuilding read models

**Alternative**: RabbitMQ — simpler to operate for smaller teams; lacks Kafka's event sourcing and replay capabilities.

### 14.4 Search Enhancement

PostgreSQL full-text search is adequate for MVP. As post volume grows:

**Elasticsearch Migration Plan**:

1. Deploy Elasticsearch cluster (3 nodes for HA)
2. Use **Debezium** (Change Data Capture) to stream PostgreSQL WAL changes to Kafka topics
3. An **Elasticsearch Sink Connector** consumes Kafka events and indexes documents
4. The `SearchService` interface (defined in the domain layer) is implemented by `ElasticsearchSearchService`
5. Feature flag switches traffic from PostgreSQL FTS to Elasticsearch

**Elasticsearch Capabilities Unlocked**:
- Fuzzy matching (typo tolerance)
- Relevance scoring with custom weights (recency, engagement)
- Autocomplete suggestions
- Aggregations (trending hashtags, related users)
- Real-time indexing with < 1 second lag

### 14.5 CDN Integration

**MVP**: Media served directly from S3 object store (acceptable for small user counts).

**Phase 2**: CDN in front of object store:

```
Client
  |
  v
CDN Edge (CloudFront / Cloudflare)
  |
  +-- Cache HIT: Return cached media from edge node
  |
  +-- Cache MISS: Fetch from S3 origin, cache at edge
        |
        v
      S3 Object Store
```

Implementation changes required:
- `MediaService` returns CDN base URL instead of S3 direct URL
- `CDN_BASE_URL` environment variable swaps origin without code changes
- S3 bucket origin access restricted to CDN only (no direct public access)
- Cache-Control headers set to `max-age=31536000, immutable` for media (content-addressed keys ensure cache-busting on update)
- Image transformation at CDN edge (resizing, WebP conversion) can be added via CloudFront Lambda@Edge or Cloudflare Workers

---

*End of Software Architecture Document — Boondi v1.0.0*

*This document is maintained by the Boondi Engineering Team. Updates are made when significant architectural decisions are made or reversed. See the ADR section for a history of key decisions.*
