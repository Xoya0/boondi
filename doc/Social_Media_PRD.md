# Product Requirements Document (PRD)

## Project: Social Media Platform (Android + Web)

------------------------------------------------------------------------

# 1. Product Overview

## Product Name

**(Working Name)**

A private social networking platform designed for a small community of
friends. The platform allows users to share text and image posts,
interact through likes and replies, follow other users, and receive
real-time notifications through Android and Web applications.

The goal is to provide a fast, simple, and modern social experience
without advertisements or unnecessary complexity while maintaining
scalability for future growth.

# 2. Vision

Create a modern social media platform that combines the simplicity of
Twitter/X with the privacy of a closed community, giving users complete
control over their content and interactions.

# 3. Problem Statement

Existing social media platforms suffer from: - Excessive
advertisements - Complex algorithms - Privacy concerns - Information
overload - Lack of customization - Feature bloat

This platform aims to provide a lightweight, private alternative for
personal communities while remaining scalable enough for future public
use.

# 4. Objectives

## Primary Goals

-   Allow users to create accounts
-   Share text and image posts
-   Follow other users
-   View a personalized timeline
-   Like and reply to posts
-   Receive notifications
-   Support Android and Web from a shared backend

## Secondary Goals

-   High performance
-   Modern UI
-   Secure authentication
-   Scalable architecture
-   Clean API documentation
-   Easy deployment

# 5. Target Users

-   Initial: 10--20 users
-   Beta: 100+ users
-   Long-term: 10,000+ users

# 6. Platforms

-   Android App
-   Responsive Web App
-   REST API Backend
-   Future iOS App

# 7. User Roles

## User

-   Register/Login
-   Create, edit, delete posts
-   Upload images
-   Like, reply, bookmark
-   Follow users
-   Update profile
-   Receive notifications

## Administrator

-   Manage users
-   Moderate posts
-   Suspend users
-   View reports
-   Publish announcements

# 8. Functional Requirements

## Authentication

-   Register
-   Login
-   Logout
-   Refresh Token
-   Forgot Password
-   Reset Password
-   Email Verification
-   Change Password

## User Profile

-   Username
-   Display Name
-   Email
-   Bio
-   Profile Picture
-   Banner Image
-   Followers/Following Counts

## Posts

-   Create/Edit/Delete
-   Text (500 chars max)
-   Image Upload
-   Quote/Repost
-   Pin Posts

## Timeline

-   Home
-   Latest
-   User Profile
-   Trending

## Social Features

-   Like/Unlike
-   Replies
-   Follow/Unfollow
-   Bookmarks
-   Search (Users, Posts, Hashtags)

## Notifications

-   Likes
-   Replies
-   Mentions
-   Follows
-   Quote Posts

# 9. Non-Functional Requirements

-   JWT Authentication
-   BCrypt Password Hashing
-   HTTPS
-   PostgreSQL
-   Redis Cache
-   REST API \<300ms average response
-   99.5% uptime target

# 10. MVP Scope

Included: - Authentication - Profiles - Posts - Images - Likes -
Replies - Follow System - Timeline - Notifications - Search - Android
App - Web App

Excluded: - Chat - Stories - Polls - Videos - Live Streaming - AI
Features

# 11. Technical Stack

## Backend

-   Java 21
-   Spring Boot
-   Spring Security
-   JWT
-   Hibernate
-   PostgreSQL
-   Redis
-   Swagger/OpenAPI

## Android

-   Kotlin
-   Jetpack Compose
-   Retrofit
-   Room
-   Hilt

## Web

-   React
-   TypeScript
-   Vite
-   Tailwind CSS
-   React Query

# 12. Core Entities

-   User
-   Profile
-   Post
-   Media
-   Comment
-   Like
-   Follow
-   Notification
-   Bookmark
-   Hashtag

# 13. API Overview

## Authentication

-   POST /auth/register
-   POST /auth/login
-   POST /auth/refresh
-   POST /auth/logout

## Users

-   GET /users/{id}
-   PUT /users/me

## Posts

-   POST /posts
-   GET /posts
-   PUT /posts/{id}
-   DELETE /posts/{id}

## Social

-   POST /posts/{id}/like
-   POST /posts/{id}/reply
-   POST /users/{id}/follow

## Notifications

-   GET /notifications

## Search

-   GET /search/users
-   GET /search/posts

# 14. Success Metrics

-   API error rate \<1%
-   Timeline loads \<2 seconds
-   Crash-free sessions \>99%
-   DAU/MAU \>30%
-   30-day retention \>50%

# 15. Future Roadmap

-   Direct Messaging
-   Stories
-   Communities
-   Polls
-   Video Uploads
-   Live Streaming
-   AI Moderation
-   Recommendation Engine
