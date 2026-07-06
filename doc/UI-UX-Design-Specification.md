# UI/UX Design Specification — Boondi

**Version:** 1.0  
**Date:** 2026-07-02  
**Status:** Draft — MVP  
**Platforms:** Android (Jetpack Compose) + Web (React + Tailwind CSS)

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Design Principles](#2-design-principles)
3. [Brand Identity](#3-brand-identity)
4. [Design System Overview](#4-design-system-overview)
5. [Typography](#5-typography)
6. [Color System](#6-color-system)
7. [Spacing & Layout Grid](#7-spacing--layout-grid)
8. [Component Library](#8-component-library)
9. [Screen Specifications — Web](#9-screen-specifications--web)
10. [Screen Specifications — Android](#10-screen-specifications--android)
11. [Navigation Architecture](#11-navigation-architecture)
12. [Interaction & Animation](#12-interaction--animation)
13. [Accessibility](#13-accessibility)
14. [Responsive Design](#14-responsive-design)
15. [Dark Mode](#15-dark-mode)
16. [Empty States & Error States](#16-empty-states--error-states)
17. [Iconography](#17-iconography)
18. [Design Handoff Notes](#18-design-handoff-notes)

---

## 1. Introduction

### 1.1 Purpose of This Document

This UI/UX Design Specification defines the complete visual language, interaction patterns, component behavior, and screen layouts for the Boondi platform. It serves as the single source of truth for all design decisions during the MVP development phase and beyond.

The document bridges the gap between product requirements and engineering implementation, providing designers and developers with unambiguous specifications for every UI element, screen, and interaction in the system.

### 1.2 Scope

This document covers the **MVP release** of Boondi across two platforms:

- **Web Application** — React 18 + TypeScript + Tailwind CSS, served as a responsive progressive web app
- **Android Application** — Kotlin + Jetpack Compose + Material 3

Features in scope for this specification:
- Authentication (login, registration, password reset)
- User profiles (view, edit, follow/unfollow)
- Posts (compose, view, like, reply, repost, bookmark)
- Timeline (Home, Latest, Trending tabs)
- Notifications
- Search (users, posts, hashtags)
- Settings (account, appearance, privacy)
- Admin panel (basic moderation)

Out of scope for this version:
- iOS native application
- Direct messaging / inbox
- Audio or video posts
- Third-party integrations

### 1.3 Design Tools

| Tool | Purpose |
|---|---|
| **Figma** | Primary design tool — wireframes, mockups, prototypes, component library |
| **Tokens Studio (Figma Plugin)** | Design token management and export |
| **Style Dictionary** | Token transformation for web (CSS variables) and Android (XML/Kotlin) |
| **Figma Dev Mode** | Developer inspection and handoff |
| **Zeplin** (optional) | Alternative handoff if team prefers |
| **Lottie / Rive** | Micro-animation assets |

### 1.4 Audience

| Audience | How to Use This Document |
|---|---|
| **UI/UX Designers** | Reference for component specifications, spacing, color tokens, and screen layouts |
| **Frontend (Web) Developers** | Tailwind config, component props, responsive breakpoints, interaction specs |
| **Android Developers** | Compose screen specs, Material 3 token mappings, navigation graph |
| **QA Engineers** | Acceptance criteria for visual states, accessibility checks, interaction correctness |
| **Product Managers** | Screen inventory, feature coverage, state documentation |

---

## 2. Design Principles

These six principles govern every design decision in Boondi. When trade-offs arise, refer to this list in order of priority.

### 2.1 Clarity Over Cleverness

**Description:** Every interface element must communicate its purpose immediately. Avoid metaphors, animations, or layouts that require explanation. If a user needs to think about what something does, it needs to be redesigned.

**Applied to Boondi:** The post action bar (like, reply, repost, bookmark) uses universally recognizable icons with optional labels. No custom iconography that requires onboarding. Navigation labels are always visible on desktop. Button text is direct: "Follow", not "Connect" or "Add".

### 2.2 Speed First

**Description:** Perceived and actual performance are both design concerns. UI should feel instantaneous. Optimistic UI updates (showing result before server confirms), skeleton loaders, and minimal layout shift are non-negotiable.

**Applied to Boondi:** Like/bookmark actions update instantly in the UI with a rollback on error. Feed loads skeletons within 100ms. Images use lazy loading with aspect-ratio placeholders to prevent layout shift. Compose button is always reachable within one tap/click.

### 2.3 Content Is King

**Description:** The interface exists to serve the content — not the other way around. Chrome (UI elements surrounding content) should be minimal and step back. Posts, images, and user writing should dominate the visual hierarchy.

**Applied to Boondi:** The main feed column on desktop is 600px wide with minimal border decoration. Post cards have no drop shadows or heavy borders — subtle dividers only. Navigation is collapsed to icons on smaller screens. Whitespace is used generously around text content.

### 2.4 Consistent Interactions

**Description:** The same action always looks and behaves the same way, regardless of where it appears. Users build muscle memory. Consistency reduces cognitive load.

**Applied to Boondi:** Every "Follow" button across feed, search results, profile pages, and suggestions uses the same component with identical states. The like animation is identical on the feed, post detail, and profile tabs. Every destructive action (delete, unfollow) always requires a confirmation dialog.

### 2.5 Privacy by Design

**Description:** The platform is private by nature. The UI should reinforce this — no public-facing discovery, no external sharing defaults, privacy controls are surfaced prominently rather than buried.

**Applied to Boondi:** Share actions produce an internal link, not a public URL. Profile pages show a clear "Private Community" indicator. Invite-only registration flow communicates exclusivity. Settings surfaces privacy controls in the primary navigation, not a sub-menu.

### 2.6 Progressive Disclosure

**Description:** Show only what the user needs for the current task. Advanced options, destructive actions, and secondary information are revealed on demand rather than cluttering the primary view.

**Applied to Boondi:** Post cards show the three-dot more menu only on hover (web) or long-press (Android). Edit and delete options appear only on the user's own posts. Profile stats (join date, website) are visible on the profile page but not in compact user cards. Compose screen shows image upload controls only after the user taps the image icon.

---

## 3. Brand Identity

### 3.1 Product Name

**Boondi** — pronounced /buːndi/. The name evokes small, round, bite-sized pieces (referencing the Indian snack), reflecting the product philosophy: small, tight-knit communities sharing bite-sized updates. The name is warm, memorable, and culturally grounded without being exclusionary.

### 3.2 Logo Guidelines

**Wordmark:**
- Set in a rounded, geometric sans-serif typeface (closest match: Nunito Bold or Poppins SemiBold)
- Lowercase preferred: `boondi`
- Letter-spacing: -0.02em (slightly tight, modern feel)
- Color: Primary brand color (`#4F46E5`) on light backgrounds; white on dark/colored backgrounds
- Never stretch, skew, or outline the wordmark

**Icon Mark:**
- Concept: A small filled circle (representing a boondi drop) with a subtle speech bubble tail at the bottom-right
- Shape: Fully rounded, bubbly — 40% corner radius relative to bounding box
- The circle represents community, wholeness, and the bite-sized nature of posts
- The speech tail communicates conversation and social connection
- Icon is used at small sizes (app icon, favicon, loading spinner) where wordmark is not legible

**Clear Space:**
- Minimum clear space around logo = 1x the height of the lowercase "b" in the wordmark
- Never place the logo on busy photographic backgrounds without a solid color backing

**Minimum Sizes:**
- Wordmark: 80px wide minimum (print: 25mm)
- Icon mark: 24px minimum (16px for favicon only)

**Logo Don'ts:**
- Do not use the icon mark without the wordmark in primary marketing contexts
- Do not recolor to unapproved colors
- Do not add drop shadows or gradients to the wordmark
- Do not place on backgrounds with less than 3:1 contrast ratio

### 3.3 Brand Voice

Boondi's voice is how the product speaks — in empty states, error messages, tooltips, and onboarding copy.

| Attribute | Description | Example |
|---|---|---|
| **Friendly** | Warm and approachable, like a trusted friend | "Welcome back!" not "Authentication successful" |
| **Direct** | No padding, no corporate euphemisms | "Delete post?" not "Are you sure you want to permanently remove this content item?" |
| **Lightweight** | Short sentences. White space in copy. | "Nothing here yet. Start following people." |
| **Honest** | Acknowledge errors plainly, no spin | "Something went wrong. Try again." not "We're experiencing technical difficulties" |
| **Unpretentious** | No marketing speak, no hype | "Your feed" not "Your personalized content experience" |

### 3.4 Brand Personality

Five adjectives that define Boondi as a product persona:

1. **Warm** — Feels like a group chat with people you trust, not a public stage
2. **Crisp** — Clean, minimal UI with no visual noise or unnecessary decoration
3. **Quick** — Snappy interactions, fast loads, no friction between thought and post
4. **Intimate** — Designed for small groups; never feels like shouting into a void
5. **Dependable** — Consistent behavior, no dark patterns, no algorithmic surprises

### 3.5 Tagline Examples

| Tagline | Tone |
|---|---|
| "Your circle, your feed." | Simple, possessive, community |
| "Small community. Real conversations." | Contrast-based, direct |
| "Post for people you actually know." | Conversational, intimate |
| "No ads. No algorithm. Just people." | Oppositional, anti-mainstream |
| "Tiny community. Loud enough." | Playful, compact |

**Recommended for MVP:** "Your circle, your feed."

---

## 4. Design System Overview

### 4.1 Token-Based Design System

Boondi uses a **design token** architecture as the foundation of its design system. Tokens are named variables that store design decisions (colors, spacing, typography, radii, shadows) and are consumed by both the web and Android codebases.

**Token Tiers:**

```
Tier 1 — Primitive Tokens (raw values)
  color-violet-600: #4F46E5
  space-4: 4px
  font-size-16: 1rem

Tier 2 — Semantic Tokens (purpose-named, reference primitives)
  color-primary: {color-violet-600}
  space-content-padding: {space-4}
  font-body-medium: {font-size-16}

Tier 3 — Component Tokens (component-specific, reference semantic)
  button-primary-background: {color-primary}
  post-card-padding: {space-content-padding}
```

**Token Format:**
- Web: CSS Custom Properties (`--color-primary: #4F46E5`)
- Android: Compose theme values via Material 3 `MaterialTheme.colorScheme`
- Source of truth: JSON via Tokens Studio → transformed by Style Dictionary

### 4.2 Atomic Design Structure

| Level | Description | Boondi Examples |
|---|---|---|
| **Atoms** | Single-purpose, indivisible UI elements | Button, Avatar, Icon, Badge, Input, Divider, Skeleton |
| **Molecules** | Small groups of atoms with a single function | Post Action Bar, User Name Row, Search Input with icon, Notification dot + Avatar |
| **Organisms** | Complex, self-contained UI sections | Post Card, User Card, Navigation Sidebar, Compose Box, Notification Item |
| **Templates** | Page-level layouts with placeholder content | Feed Layout (3-col), Profile Layout, Auth Layout |
| **Pages** | Templates filled with real content and state | Home Feed, User Profile, Login |

### 4.3 Component Naming Conventions

**Figma Components:**
```
[Category]/[ComponentName]/[Variant]/[State]

Examples:
  Button/Primary/Large/Default
  Button/Primary/Large/Hover
  PostCard/Default/Liked
  Avatar/Medium/Online
  Input/Text/Focused/Error
```

**React Components (Web):**
```
PascalCase for component names
camelCase for props
kebab-case for CSS classes (Tailwind utility classes)

Examples:
  <PostCard variant="reply" isLiked={true} />
  <Avatar size="md" showOnlineIndicator />
  <Button variant="primary" size="lg" isLoading />
```

**Jetpack Compose (Android):**
```
PascalCase for Composables
camelCase for parameters

Examples:
  PostCard(post = post, onLike = { ... })
  BoondiAvatar(size = AvatarSize.Medium, user = user)
  PrimaryButton(text = "Follow", onClick = { ... })
```

---

## 5. Typography

### 5.1 Font Families

| Platform | Primary Font | Fallback Stack |
|---|---|---|
| Web | Inter | `Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif` |
| Android | Roboto | System default (Roboto is Android system font) |

**Web Font Loading:**
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

### 5.2 Full Type Scale

| Style Name | Size (Web) | Size (Android) | Weight | Line Height | Letter Spacing | Usage |
|---|---|---|---|---|---|---|
| Display Large | 2.25rem / 36px | 36sp | 700 | 1.2 | -0.02em | Hero text, splash screens |
| Display Medium | 1.875rem / 30px | 30sp | 700 | 1.25 | -0.01em | Section heroes, empty state headlines |
| Heading 1 | 1.5rem / 24px | 24sp | 700 | 1.3 | -0.01em | Page titles (Profile name, page headers) |
| Heading 2 | 1.25rem / 20px | 20sp | 600 | 1.35 | 0 | Card titles, modal headings |
| Heading 3 | 1.125rem / 18px | 18sp | 600 | 1.4 | 0 | Section headers within pages |
| Heading 4 | 1rem / 16px | 16sp | 600 | 1.4 | 0 | Sub-section labels, sidebar section titles |
| Body Large | 1rem / 16px | 16sp | 400 | 1.6 | 0 | Post content (primary reading text) |
| Body Medium | 0.9375rem / 15px | 15sp | 400 | 1.6 | 0 | Secondary body text, bio text |
| Body Small | 0.875rem / 14px | 14sp | 400 | 1.5 | 0 | Helper text, descriptions, replies |
| Label Large | 0.9375rem / 15px | 15sp | 500 | 1.2 | 0.01em | Button labels (large/medium buttons) |
| Label Medium | 0.875rem / 14px | 14sp | 500 | 1.2 | 0.01em | Button labels (small), tab labels |
| Label Small | 0.8125rem / 13px | 13sp | 500 | 1.2 | 0.02em | Chips, badges, small interactive labels |
| Caption | 0.8125rem / 13px | 12sp | 400 | 1.4 | 0.01em | Timestamps, meta info, char counter |
| Overline | 0.75rem / 12px | 11sp | 600 | 1.2 | 0.08em | Section overlines (ALL CAPS), category labels |

### 5.3 Web Typography — Responsive Scaling

Typography scales down on mobile using a fluid approach:

```css
/* Base (mobile-first) */
:root {
  --font-display-large: 1.875rem;   /* 30px on mobile */
  --font-display-medium: 1.5rem;    /* 24px on mobile */
  --font-heading-1: 1.25rem;        /* 20px on mobile */
}

/* Desktop (768px+) */
@media (min-width: 768px) {
  :root {
    --font-display-large: 2.25rem;  /* 36px on desktop */
    --font-display-medium: 1.875rem;
    --font-heading-1: 1.5rem;
  }
}
```

Tailwind config extension:
```js
fontSize: {
  'display-lg': ['2.25rem', { lineHeight: '1.2', letterSpacing: '-0.02em', fontWeight: '700' }],
  'display-md': ['1.875rem', { lineHeight: '1.25', letterSpacing: '-0.01em', fontWeight: '700' }],
  'h1': ['1.5rem', { lineHeight: '1.3', letterSpacing: '-0.01em', fontWeight: '700' }],
  'h2': ['1.25rem', { lineHeight: '1.35', fontWeight: '600' }],
  'h3': ['1.125rem', { lineHeight: '1.4', fontWeight: '600' }],
  'h4': ['1rem', { lineHeight: '1.4', fontWeight: '600' }],
  'body-lg': ['1rem', { lineHeight: '1.6' }],
  'body-md': ['0.9375rem', { lineHeight: '1.6' }],
  'body-sm': ['0.875rem', { lineHeight: '1.5' }],
  'label-lg': ['0.9375rem', { lineHeight: '1.2', letterSpacing: '0.01em', fontWeight: '500' }],
  'label-md': ['0.875rem', { lineHeight: '1.2', letterSpacing: '0.01em', fontWeight: '500' }],
  'label-sm': ['0.8125rem', { lineHeight: '1.2', letterSpacing: '0.02em', fontWeight: '500' }],
  'caption': ['0.8125rem', { lineHeight: '1.4', letterSpacing: '0.01em' }],
  'overline': ['0.75rem', { lineHeight: '1.2', letterSpacing: '0.08em', fontWeight: '600' }],
}
```

### 5.4 Android Typography — Material 3 Mapping

| Material 3 Type Role | Boondi Style | sp Size | Weight |
|---|---|---|---|
| displayLarge | Display Large | 57sp | 400 (override: 700) |
| headlineLarge | Heading 1 | 32sp | 400 (override: 700) |
| headlineMedium | Heading 2 | 28sp | 400 (override: 600) |
| headlineSmall | Heading 3 | 24sp | 400 (override: 600) |
| titleLarge | Heading 4 | 22sp | 400 (override: 600) |
| titleMedium | Label Large | 16sp | 500 |
| titleSmall | Label Medium | 14sp | 500 |
| bodyLarge | Body Large | 16sp | 400 |
| bodyMedium | Body Medium | 14sp | 400 |
| bodySmall | Body Small | 12sp | 400 |
| labelLarge | Label Large | 14sp | 500 |
| labelMedium | Label Medium | 12sp | 500 |
| labelSmall | Caption / Overline | 11sp | 500 |

### 5.5 Truncation Rules

| Context | Max Lines | Overflow Behavior |
|---|---|---|
| Post content (feed card) | 6 lines | Fade + "Show more" link |
| Post content (detail page) | No limit | Full display |
| Display name | 1 line | Ellipsis (`text-overflow: ellipsis`) |
| Username (@handle) | 1 line | Ellipsis |
| Bio (profile page) | 3 lines | "Read more" toggle |
| Bio (compact user card) | 2 lines | Ellipsis |
| Notification text | 2 lines | Ellipsis |
| Hashtag chip | 1 line | Ellipsis at 120px max width |

---

## 6. Color System

### 6.1 Brand Colors — Primary Palette (Indigo/Violet)

The primary brand color is **Indigo** — a vibrant, deep blue-violet that conveys trust, creativity, and modernity. It avoids the overused Twitter-blue while remaining in the trustworthy blue family.

**Primary: Indigo**

| Shade | Hex | Usage |
|---|---|---|
| 50 | `#EEF2FF` | Tinted backgrounds, hover fills on light mode |
| 100 | `#E0E7FF` | Active state backgrounds, selected tab indicator fill |
| 200 | `#C7D2FE` | Disabled button fill (light mode) |
| 300 | `#A5B4FC` | Decorative use, progress indicators (light) |
| 400 | `#818CF8` | Secondary accents, link hover |
| 500 | `#6366F1` | Standard primary (body/forms) |
| 600 | `#4F46E5` | **Primary brand color** — buttons, links, key actions |
| 700 | `#4338CA` | Primary hover state |
| 800 | `#3730A3` | Primary pressed/active state |
| 900 | `#312E81` | Deep brand accent, dark mode primary |
| 950 | `#1E1B4B` | Near-black tint for gradients |

**Neutral (Gray) — Supporting Palette**

| Shade | Hex | Usage |
|---|---|---|
| 50 | `#F9FAFB` | App background (light mode) |
| 100 | `#F3F4F6` | Surface / Card background (light mode) |
| 200 | `#E5E7EB` | Borders (subtle, light mode) |
| 300 | `#D1D5DB` | Borders (default, light mode) |
| 400 | `#9CA3AF` | Placeholder text, disabled text |
| 500 | `#6B7280` | Secondary / tertiary text |
| 600 | `#4B5563` | Secondary text (stronger) |
| 700 | `#374151` | Primary text (accessible, not full black) |
| 800 | `#1F2937` | Primary text dark, headings |
| 900 | `#111827` | Near-black text |
| 950 | `#030712` | App background (dark mode) |

### 6.2 Semantic Color Tokens

| Token Name | Light Mode | Dark Mode | Usage |
|---|---|---|---|
| `color-primary` | `#4F46E5` | `#818CF8` | Primary actions, links, focused states |
| `color-primary-hover` | `#4338CA` | `#6366F1` | Hover state for primary elements |
| `color-primary-active` | `#3730A3` | `#4F46E5` | Pressed/active state |
| `color-primary-disabled` | `#C7D2FE` | `#312E81` | Disabled primary elements |
| `color-primary-subtle` | `#EEF2FF` | `#1E1B4B` | Tinted background for primary context |
| `color-background` | `#F9FAFB` | `#030712` | App-level background |
| `color-surface` | `#FFFFFF` | `#111827` | Card / panel surface |
| `color-surface-raised` | `#FFFFFF` | `#1F2937` | Elevated surface (modal, dropdown) |
| `color-surface-overlay` | `rgba(0,0,0,0.5)` | `rgba(0,0,0,0.7)` | Modal backdrop |
| `color-text-primary` | `#111827` | `#F9FAFB` | Primary readable text |
| `color-text-secondary` | `#4B5563` | `#9CA3AF` | Supporting text, timestamps |
| `color-text-tertiary` | `#9CA3AF` | `#6B7280` | Placeholder, disabled labels |
| `color-text-disabled` | `#D1D5DB` | `#374151` | Disabled input/button text |
| `color-text-inverse` | `#FFFFFF` | `#111827` | Text on primary-colored backgrounds |
| `color-border` | `#E5E7EB` | `#1F2937` | Default borders, dividers |
| `color-border-subtle` | `#F3F4F6` | `#111827` | Very subtle dividers |
| `color-border-strong` | `#9CA3AF` | `#374151` | Strong emphasis borders, focus rings |
| `color-success` | `#10B981` | `#34D399` | Success states, "copied", confirmation |
| `color-success-subtle` | `#ECFDF5` | `#064E3B` | Success banner background |
| `color-warning` | `#F59E0B` | `#FCD34D` | Warnings, rate limit alerts |
| `color-warning-subtle` | `#FFFBEB` | `#451A03` | Warning banner background |
| `color-error` | `#EF4444` | `#F87171` | Errors, validation failures, destructive |
| `color-error-subtle` | `#FEF2F2` | `#450A0A` | Error banner background |
| `color-info` | `#3B82F6` | `#60A5FA` | Info banners, tips |
| `color-info-subtle` | `#EFF6FF` | `#1E3A5F` | Info banner background |
| `color-like` | `#F43F5E` | `#FB7185` | Like button active state (rose/pink-red) |
| `color-like-subtle` | `#FFF1F2` | `#4C0519` | Like button hover background |
| `color-bookmark` | `#F59E0B` | `#FCD34D` | Bookmark active state (amber/gold) |
| `color-bookmark-subtle` | `#FFFBEB` | `#451A03` | Bookmark hover background |
| `color-repost` | `#10B981` | `#34D399` | Repost active state (emerald green) |
| `color-repost-subtle` | `#ECFDF5` | `#064E3B` | Repost hover background |

### 6.3 Tailwind Config Tokens

```typescript
// tailwind.config.ts
import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: 'class',
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#EEF2FF',
          100: '#E0E7FF',
          200: '#C7D2FE',
          300: '#A5B4FC',
          400: '#818CF8',
          500: '#6366F1',
          600: '#4F46E5',
          700: '#4338CA',
          800: '#3730A3',
          900: '#312E81',
          950: '#1E1B4B',
        },
      },
      backgroundColor: {
        'app': 'var(--color-background)',
        'surface': 'var(--color-surface)',
        'surface-raised': 'var(--color-surface-raised)',
      },
      textColor: {
        'primary': 'var(--color-text-primary)',
        'secondary': 'var(--color-text-secondary)',
        'tertiary': 'var(--color-text-tertiary)',
        'disabled': 'var(--color-text-disabled)',
        'inverse': 'var(--color-text-inverse)',
      },
      borderColor: {
        'default': 'var(--color-border)',
        'subtle': 'var(--color-border-subtle)',
        'strong': 'var(--color-border-strong)',
      },
    },
  },
  plugins: [],
}

export default config
```

```css
/* src/styles/tokens.css */
:root {
  --color-primary: #4F46E5;
  --color-primary-hover: #4338CA;
  --color-primary-active: #3730A3;
  --color-primary-disabled: #C7D2FE;
  --color-primary-subtle: #EEF2FF;
  --color-background: #F9FAFB;
  --color-surface: #FFFFFF;
  --color-surface-raised: #FFFFFF;
  --color-surface-overlay: rgba(0, 0, 0, 0.5);
  --color-text-primary: #111827;
  --color-text-secondary: #4B5563;
  --color-text-tertiary: #9CA3AF;
  --color-text-disabled: #D1D5DB;
  --color-text-inverse: #FFFFFF;
  --color-border: #E5E7EB;
  --color-border-subtle: #F3F4F6;
  --color-border-strong: #9CA3AF;
  --color-success: #10B981;
  --color-error: #EF4444;
  --color-warning: #F59E0B;
  --color-info: #3B82F6;
  --color-like: #F43F5E;
  --color-bookmark: #F59E0B;
  --color-repost: #10B981;
}

.dark {
  --color-primary: #818CF8;
  --color-primary-hover: #6366F1;
  --color-primary-active: #4F46E5;
  --color-primary-disabled: #312E81;
  --color-primary-subtle: #1E1B4B;
  --color-background: #030712;
  --color-surface: #111827;
  --color-surface-raised: #1F2937;
  --color-surface-overlay: rgba(0, 0, 0, 0.7);
  --color-text-primary: #F9FAFB;
  --color-text-secondary: #9CA3AF;
  --color-text-tertiary: #6B7280;
  --color-text-disabled: #374151;
  --color-text-inverse: #111827;
  --color-border: #1F2937;
  --color-border-subtle: #111827;
  --color-border-strong: #374151;
  --color-success: #34D399;
  --color-error: #F87171;
  --color-warning: #FCD34D;
  --color-info: #60A5FA;
  --color-like: #FB7185;
  --color-bookmark: #FCD34D;
  --color-repost: #34D399;
}
```

### 6.4 Android Material 3 Color Mapping

```kotlin
// ui/theme/Color.kt
import androidx.compose.ui.graphics.Color

// Brand primitives
val BoondiIndigo600 = Color(0xFF4F46E5)
val BoondiIndigo400 = Color(0xFF818CF8)
val BoondiIndigo100 = Color(0xFFE0E7FF)
val BoondiIndigo900 = Color(0xFF312E81)
val BoondiIndigo950 = Color(0xFF1E1B4B)

// Light scheme
val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF4F46E5),  // brand-600
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),  // brand-100
    onPrimaryContainer = Color(0xFF312E81), // brand-900
    secondary        = Color(0xFF6B7280),
    onSecondary      = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3F4F6),
    onSecondaryContainer = Color(0xFF111827),
    background       = Color(0xFFF9FAFB),
    onBackground     = Color(0xFF111827),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF111827),
    surfaceVariant   = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline          = Color(0xFFE5E7EB),
    error            = Color(0xFFEF4444),
    onError          = Color(0xFFFFFFFF),
)

// Dark scheme
val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF818CF8),  // brand-400
    onPrimary        = Color(0xFF1E1B4B),  // brand-950
    primaryContainer = Color(0xFF312E81),  // brand-900
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary        = Color(0xFF9CA3AF),
    onSecondary      = Color(0xFF111827),
    secondaryContainer = Color(0xFF1F2937),
    onSecondaryContainer = Color(0xFFF9FAFB),
    background       = Color(0xFF030712),
    onBackground     = Color(0xFFF9FAFB),
    surface          = Color(0xFF111827),
    onSurface        = Color(0xFFF9FAFB),
    surfaceVariant   = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline          = Color(0xFF1F2937),
    error            = Color(0xFFF87171),
    onError          = Color(0xFF450A0A),
)
```

---

## 7. Spacing & Layout Grid

### 7.1 Spacing Scale

Base unit: **4px**. All spacing values are multiples of 4px.

| Token Name | Value (px) | Value (rem) | Value (dp — Android) | Common Usage |
|---|---|---|---|---|
| `space-1` | 4px | 0.25rem | 4dp | Icon internal padding, tiny gaps |
| `space-2` | 8px | 0.5rem | 8dp | Icon-to-label gap, badge offset |
| `space-3` | 12px | 0.75rem | 12dp | Input internal padding (vertical), chip padding |
| `space-4` | 16px | 1rem | 16dp | Standard content padding, list item gaps |
| `space-5` | 20px | 1.25rem | 20dp | Card padding (compact) |
| `space-6` | 24px | 1.5rem | 24dp | Section spacing, card padding (standard) |
| `space-8` | 32px | 2rem | 32dp | Component group spacing, modal padding |
| `space-10` | 40px | 2.5rem | 40dp | Section breaks, large gaps |
| `space-12` | 48px | 3rem | 48dp | Page section spacing |
| `space-16` | 64px | 4rem | 64dp | Hero spacing, large layout gaps |
| `space-20` | 80px | 5rem | 80dp | Bottom nav clearance (Android) |
| `space-24` | 96px | 6rem | 96dp | Page top padding on large screens |

### 7.2 Web Layout Grid

**Mobile (< 768px) — Single Column**
- Layout: 1 column
- Horizontal padding: 16px (each side)
- Max content width: 100%
- Navigation: Bottom tab bar (fixed)
- Compose: Floating Action Button (bottom-right)

**Tablet (768px – 1279px) — Two Column**
- Layout: 2 columns
- Column 1 (left): Navigation sidebar — 72px (icon-only, collapsed)
- Column 2 (right): Main content — fills remaining width
- Gutter between columns: 0 (sidebar flush)
- Horizontal padding inside content: 24px

**Desktop (≥ 1280px) — Three Column**
- Layout: 3 columns, centered at max 1280px
- Column 1 — Left Sidebar (Navigation): 280px fixed
- Column 2 — Main Feed: 600px
- Column 3 — Right Panel: 340px
- Gap between columns: `flex gap`, visually 0 (borders separate)
- Outer padding: auto (centered)

```
|←— 280px nav —→|←———— 600px feed ————→|←— 340px panel —→|
```

**Max content width:** 1280px, `margin: 0 auto`

### 7.3 Android Layout

- **Design paradigm:** Edge-to-edge (full-bleed behind system bars)
- **Horizontal content padding:** 16dp (standard), 8dp (compact list items)
- **Bottom navigation bar height:** 80dp (includes system gesture inset via `WindowInsets`)
- **Top app bar height:** 64dp (Material 3 `TopAppBar`)
- **Content safe area:** Account for `WindowInsets.systemBars` using `Modifier.windowInsetsPadding`
- **FAB position:** Bottom-right, 16dp from bottom nav top, 16dp from right edge
- **List item minimum height:** 56dp (Material 3 standard)
- **Card elevation:** 0dp (flat design) — use background color difference instead

### 7.4 Border Radius Tokens

| Token | Value | Tailwind Class | Usage |
|---|---|---|---|
| `radius-none` | 0px | `rounded-none` | Full-bleed images, dividers |
| `radius-sm` | 4px | `rounded` | Tags, small badges |
| `radius-md` | 8px | `rounded-lg` | Buttons (default), input fields, chips |
| `radius-lg` | 12px | `rounded-xl` | Cards (PostCard, UserCard), modals |
| `radius-xl` | 16px | `rounded-2xl` | Bottom sheets, image containers |
| `radius-2xl` | 24px | `rounded-3xl` | Large modals, special cards |
| `radius-full` | 9999px | `rounded-full` | Avatars, pills, toggle switches, FAB |

**Android Material 3 Shape mapping:**
```kotlin
val BoondiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // radius-sm
    small      = RoundedCornerShape(8.dp),   // radius-md
    medium     = RoundedCornerShape(12.dp),  // radius-lg
    large      = RoundedCornerShape(16.dp),  // radius-xl
    extraLarge = RoundedCornerShape(24.dp),  // radius-2xl
)
```

### 7.5 Shadow / Elevation Tokens

**Web (box-shadow):**

| Token | CSS Value | Usage |
|---|---|---|
| `shadow-none` | `none` | Flat surfaces (cards on white bg) |
| `shadow-xs` | `0 1px 2px rgba(0,0,0,0.05)` | Subtle card lift |
| `shadow-sm` | `0 1px 3px rgba(0,0,0,0.1), 0 1px 2px rgba(0,0,0,0.06)` | Default card shadow |
| `shadow-md` | `0 4px 6px rgba(0,0,0,0.07), 0 2px 4px rgba(0,0,0,0.06)` | Dropdowns, popovers |
| `shadow-lg` | `0 10px 15px rgba(0,0,0,0.1), 0 4px 6px rgba(0,0,0,0.05)` | Modals |
| `shadow-xl` | `0 20px 25px rgba(0,0,0,0.1), 0 10px 10px rgba(0,0,0,0.04)` | Drawer, full-screen overlays |

**Dark mode shadows:** Reduce opacity by ~40% and shift to use border instead of shadow for separation.

**Android Material 3 Elevation:**
- Boondi uses **tonal elevation** (color tint) rather than drop shadows per Material 3 spec
- Surface tonal elevation values: Level 0 (0dp), Level 1 (1dp), Level 2 (3dp), Level 3 (6dp)
- Cards: Level 1 (subtle tint); Modals/Dialogs: Level 3; Bottom sheet: Level 2

---

## 8. Component Library

### 8.1 Buttons

**Purpose:** Trigger actions. Every interactive action in Boondi that submits data or navigates uses a button variant.

#### Variants

| Variant | Background | Text | Border | Use Case |
|---|---|---|---|---|
| Primary | `color-primary` | `color-text-inverse` | none | Main CTA: Post, Follow, Login, Register |
| Secondary | `color-surface` | `color-primary` | `color-primary` 1.5px | Secondary action: Edit Profile, Cancel |
| Tertiary | transparent | `color-primary` | none | Low-emphasis: "Load more", "Show replies" |
| Ghost | transparent | `color-text-secondary` | none | Utility actions in crowded UI: nav items |
| Destructive | `#FEF2F2` | `color-error` | `color-error` 1.5px | Delete post, Delete account, Unfollow |
| Icon Button | transparent | `color-text-secondary` | none | Action bar icons: like, bookmark, share |

#### Sizes

| Size | Height | Padding (H) | Font Style | Icon Size |
|---|---|---|---|---|
| sm | 32px | 12px | Label Small | 16px |
| md | 40px | 16px | Label Medium | 20px |
| lg | 48px | 24px | Label Large | 20px |

#### States

| State | Visual Change |
|---|---|
| Default | Base variant style |
| Hover | Darken background 8% OR show `color-primary-subtle` tint |
| Active/Pressed | Darken background 16% OR scale 0.97 |
| Focused | 2px offset ring in `color-primary`, 2px gap |
| Disabled | 50% opacity; `cursor: not-allowed`; no hover effect |
| Loading | Replace label with spinner (16px, same color as label); width locked |

#### Anatomy
```
[  [icon?]  [label]  ]
 ^padding^           ^padding^
```
- Icon + label gap: 8px (`space-2`)
- Minimum width: 80px (to avoid tiny pill buttons)
- Full-width variant: `w-full` — used in modals and auth forms

#### Usage Rules
- One Primary button per view section maximum
- Never use Destructive as the default CTA
- Loading state width must match default state width (no layout shift)
- Always pair Destructive button with a confirmation dialog

---

### 8.2 Input Fields

**Purpose:** Collect text input from users — post composition, search, forms.

#### Variants

| Variant | Description |
|---|---|
| Text Input | Single-line, standard form field |
| Textarea | Multi-line, used for post composer (auto-expanding) |
| Search Input | Single-line with leading search icon, clear button |

#### States

| State | Border | Background | Label |
|---|---|---|---|
| Default | `color-border` 1px | `color-surface` | Floated above or as placeholder |
| Focused | `color-primary` 2px | `color-surface` | Floated label in `color-primary` |
| Filled | `color-border` 1px | `color-surface` | Floated label in `color-text-secondary` |
| Error | `color-error` 2px | `color-error-subtle` tint | Label + error message in `color-error` |
| Disabled | `color-border-subtle` 1px | `color-surface-raised` | Label + value in `color-text-disabled` |

#### Anatomy
```
[Label text]
┌─────────────────────────────┐
│  placeholder / value text   │  [trailing icon?]
└─────────────────────────────┘
[Helper text / Error message]  [Character counter]
```

- Border radius: `radius-md` (8px)
- Height (single line): 44px
- Internal padding: 12px vertical, 16px horizontal
- Label: floats above on focus/fill (Material-style floating label)
- Helper text: 12px, `color-text-secondary`, below field
- Error message: 12px, `color-error`, replaces helper text
- Character counter: 13px Caption, right-aligned below field
  - Default: `color-text-tertiary`
  - At 80% capacity: `color-warning`
  - At 95%+ capacity: `color-error`
  - At limit: block further input, show max in `color-error`

#### Post Composer Textarea
- Min height: 80px; expands to max 300px, then scrolls
- No visible border in composer mode — uses background color separation
- Font: Body Large (16px), `color-text-primary`
- Placeholder: "What's on your mind?" in `color-text-tertiary`

#### Search Input
- Leading icon: magnifying glass (20px), `color-text-tertiary`
- On focus: icon turns `color-primary`
- Clear button (X): appears when value is non-empty, right-aligned
- Background: `color-surface-raised` (slightly elevated from page bg)
- Border radius: `radius-full` (pill shape)

---

### 8.3 Avatar

**Purpose:** Represent users visually with profile photo or initials fallback.

#### Sizes

| Size Name | Dimensions | Usage |
|---|---|---|
| xs | 24×24px | Inline in text (mention preview), notification dot |
| sm | 32×32px | Compact notification items, reply chains |
| md | 40×40px | Post cards, user list rows, action bars |
| lg | 48×48px | Compose box, sidebar user info |
| xl | 80×80px | Profile page (over banner) |
| 2xl | 120×120px | Profile edit screen |

#### States / Variants

| State | Description |
|---|---|
| Default | Circular profile image, `radius-full`, 2px solid `color-border` |
| Online | Green dot (10px) bottom-right, `color-success`, white ring |
| Verified | Indigo checkmark badge (16px) bottom-right (future feature) |
| Fallback | Colored circle with 1–2 letter initials (uppercase) |

#### Fallback Colors
Assign deterministically from username hash to one of 8 brand-consistent colors:
`#4F46E5` (indigo), `#10B981` (emerald), `#F59E0B` (amber), `#EF4444` (red), `#8B5CF6` (violet), `#EC4899` (pink), `#06B6D4` (cyan), `#84CC16` (lime)

Initial text: white, weight 600, size = avatar_height × 0.4

---

### 8.4 Post Card

**Purpose:** The primary content unit. Displays a single post with its author info, content, media, and action bar.

#### Anatomy

```
┌──────────────────────────────────────────────────┐
│  [Avatar md]  [Display Name] [·] [@username] [timestamp]  [···]  │
│               [Verified badge?]                             │
│                                                             │
│  [Post content text — Body Large, up to 6 lines]           │
│  [Show more link if truncated]                              │
│                                                             │
│  [Image grid — 1 to 4 images]                              │
│                                                             │
│  [Quote post embed — if quote post]                         │
│                                                             │
│  ─────────────────────────────────────────────────         │
│  [♡ 12]  [💬 4]  [↺ 2]  [🔖]  [↗ share]                  │
└──────────────────────────────────────────────────┘
```

#### Variants

| Variant | Description |
|---|---|
| Original Post | Standard post card as above |
| Reply | Small greyed "Replying to @username" line above content |
| Quote Post | Embedded original post card (no actions, just avatar + name + text) below content |
| Pinned Post | "📌 Pinned" label above avatar row; shown at top of profile |
| Skeleton | Animated shimmer placeholder — avatar circle + 3 text bars + action bar |

#### Action Bar Items

| Action | Icon | Active State | Count |
|---|---|---|---|
| Like | Heart (outline → filled) | `color-like` (#F43F5E) fill | Yes |
| Reply | Message circle | `color-primary` | Yes |
| Repost | Repeat/arrows | `color-repost` (#10B981) | Yes |
| Bookmark | Bookmark (outline → filled) | `color-bookmark` (#F59E0B) | No |
| Share | Share/Upload | No active state | No |

Action bar layout: `space-between`, items spaced evenly across card width.
Action item: icon (20px) + count label (Caption) with 4px gap.
Touch target: minimum 44×44px (web), 48×48dp (Android).

#### Card Spacing

- Card padding: 16px all sides
- Divider between cards: 1px `color-border-subtle`, no gap (divider only, no margin)
- Avatar-to-content gap: 12px
- Content-to-action-bar gap: 12px
- No card shadow or elevation — background color provides separation on feed

#### Post Card — Own Post (Extra States)

Three-dot menu (`···`) on own posts reveals:
- Edit post (if within edit window)
- Delete post → confirmation dialog
- Pin to profile / Unpin

Three-dot menu on others' posts reveals:
- Report post
- Block user (future)
- Copy link

---

### 8.5 User Card / Suggestion

#### Compact User Card (Follow Suggestion, Sidebar)

```
┌────────────────────────────────────────────────┐
│  [Avatar md]  [Display Name]     [Follow btn]  │
│               [@username]                       │
└────────────────────────────────────────────────┘
```

- Height: 64px
- Follow button: Secondary variant, size sm, 70px wide
- When followed: button changes to "Following" (Ghost variant, greyed)

#### Full User Card (Search Results, Followers/Following list)

```
┌────────────────────────────────────────────────┐
│  [Avatar md]  [Display Name]     [Follow btn]  │
│               [@username]                       │
│               [Bio — 2 lines max]              │
│               [X followers]                    │
└────────────────────────────────────────────────┘
```

- Height: auto (min 80px)
- Bio: Body Small, `color-text-secondary`
- Follower count: Caption, `color-text-tertiary`

---

### 8.6 Navigation

#### Web — Left Sidebar (Desktop, ≥ 1280px)

```
┌──────────────┐
│  [Boondi]    │  ← Wordmark logo (32px height)
│              │
│  🏠 Home     │  ← NavItem (icon 24px + label)
│  🔍 Search   │
│  🔔 Notifs   │  [Badge dot if unread]
│  🔖 Bookmarks│
│  👤 Profile  │
│              │
│  [+ Compose] │  ← Primary button, full width
│              │
│  ─────────── │
│  [Avatar xs] │  ← Logged-in user (bottom)
│  [Name]      │
│  [@handle]   │
│  [···]       │
└──────────────┘
```

NavItem anatomy:
- Height: 48px, `radius-lg`, padding: 12px horizontal
- Icon: 24px, `color-text-secondary`
- Label: Label Medium, `color-text-secondary`
- Active state: background `color-primary-subtle`, icon + label `color-primary`, weight 600
- Hover: background `color-border-subtle`

#### Web — Bottom Bar (Mobile, < 768px)

5 tabs: Home | Search | Compose (center, FAB-style) | Notifications | Profile

- Height: 56px + safe area inset
- Tab icon: 24px
- Tab label: Caption (10px), shown only on active tab
- Active: `color-primary` icon
- Compose tab: Filled circle (`color-primary` background, white + icon)

#### Android — Bottom Navigation Bar

Material 3 `NavigationBar` component.

| Tab | Icon | Label |
|---|---|---|
| Home | Home (filled when active) | Home |
| Search | Search | Search |
| Notifications | Notifications (with badge) | Activity |
| Profile | Person (filled when active) | Profile |

- 4 tabs only (compose is FAB)
- FAB: Extended on home screen ("Post"), icon-only on other screens
- Navigation bar height: 80dp (includes gesture inset)
- Active indicator: Pill shape behind icon, `primaryContainer` color

---

### 8.7 Notification Item

**Purpose:** Communicate social interactions (likes, replies, follows, etc.) to the user.

#### Anatomy

```
┌──────────────────────────────────────────────────┐
│  [Avatar sm]  [Notif Icon]  [Text]   [Timestamp] │
│                             [Post preview text]  │
└──────────────────────────────────────────────────┘
```

- Avatar: 40×40px
- Notification icon: 20px, positioned bottom-right of avatar (badge-style)
- Notification text: Body Medium — "**Jane** liked your post"
  - Actor name: Bold, `color-text-primary`
  - Action text: Regular, `color-text-secondary`
- Timestamp: Caption, `color-text-tertiary`, right-aligned
- Post preview: Body Small, `color-text-tertiary`, 2 lines max, indented
- Unread indicator: 8px `color-primary` dot on left edge of row
- Unread background: Subtle `color-primary-subtle` tint on row

#### Notification Types and Icons

| Type | Icon | Icon Color | Text Pattern |
|---|---|---|---|
| Like | Heart (filled) | `color-like` | "**{name}** liked your post" |
| Reply | Message circle | `color-primary` | "**{name}** replied to your post" |
| Follow | User+ | `color-repost` | "**{name}** followed you" |
| Mention | @ sign | `color-primary` | "**{name}** mentioned you in a post" |
| Repost | Repeat | `color-repost` | "**{name}** reposted your post" |
| Quote | Quote | `color-primary` | "**{name}** quoted your post" |

---

### 8.8 Chips / Tags

#### Hashtag Chip

```
[ # trending ]
```

- Background: `color-primary-subtle`
- Text: `color-primary`, Label Small
- Border radius: `radius-full`
- Padding: 4px 10px
- Click: navigates to `/search?q=%23hashtag&tab=hashtags`
- Max width: 140px with ellipsis

#### Filter Chip (Timeline Tabs)

Used for tab-bar style filtering: Home | Latest | Trending

```
[  Home  ]  [  Latest  ]  [  Trending  ]
```

- Default: transparent background, `color-text-secondary` label
- Active: `color-primary` underline (web) or filled pill (Android), `color-primary` label, weight 600
- Height: 40px
- Bottom border (web): 2px `color-primary` on active tab
- Android: Use `TabRow` with `Tab` components from Material 3

---

### 8.9 Modals / Dialogs

#### Confirmation Dialog

Used for: Delete post, Unfollow user, Delete account.

```
┌──────────────────────────────────┐
│  [Title — Heading 2]            │
│                                  │
│  [Body text — Body Medium]       │
│  [Supporting detail if needed]   │
│                                  │
│  [Cancel btn]    [Confirm btn]   │
└──────────────────────────────────┘
```

- Width: 400px (web), full-width minus 48dp margin (Android)
- Border radius: `radius-xl`
- Backdrop: `color-surface-overlay`, click-to-dismiss (except account delete)
- Confirm button: Destructive variant for delete actions
- Cancel: Tertiary or Secondary variant
- Keyboard: Escape dismisses; Enter triggers primary action

#### Bottom Sheet (Android)

Used for: Compose options, post more menu, image picker, share.

- Full-width, anchored to bottom
- Handle indicator: 32×4dp pill, `color-border`, centered at top
- Background: `color-surface-raised`
- Border radius: `radius-xl` (top corners only)
- Min height: 200dp; max height: 80% of screen
- Drag to dismiss; scrim dismisses on tap

---

### 8.10 Toast / Snackbar

**Purpose:** Non-blocking feedback for completed actions.

#### Variants

| Variant | Icon | Background | Text | Example |
|---|---|---|---|---|
| Success | Check circle | `color-success` | White | "Post published" |
| Error | X circle | `color-error` | White | "Failed to post. Try again." |
| Info | Info | `color-info` | White | "Link copied to clipboard" |

#### Behavior

- Position: Bottom-center (web), bottom of screen above nav (Android)
- Auto-dismiss: 3 seconds
- Manual dismiss: X button on right
- Max width: 400px (web); full-width minus 32dp margin (Android)
- Border radius: `radius-lg`
- Stacking: New toast replaces existing (no queue stack in MVP)
- Animation: Slide up from bottom on enter, fade out on dismiss

---

### 8.11 Skeleton Loaders

Animated shimmer effect — left-to-right gradient sweep at 1.5s loop.
Colors: `color-border-subtle` (base) → `color-border` (shimmer).

#### Post Card Skeleton

```
┌──────────────────────────────────────────────────┐
│  [○ 40px]  [████████ 120px]  [████ 60px]        │
│             [██████ 80px]                        │
│                                                   │
│  [████████████████████████████████████ 100%]     │
│  [████████████████████████ 70%]                  │
│  [████████████████ 50%]                          │
│                                                   │
│  [██ 40px]  [██ 40px]  [██ 40px]  [██ 40px]    │
└──────────────────────────────────────────────────┘
```

#### User Card Skeleton

```
[○ 40px]  [████████ 100px]  [██████████████ 80px btn]
          [██████ 70px    ]
```

#### Notification Skeleton

```
[○ 40px]  [████████████████████ 160px]  [████ 40px]
          [██████████ 100px]
```

- Show 5 skeleton items on initial feed load
- Append 3 skeleton items when infinite scroll triggers

---

### 8.12 Empty States

**Purpose:** Guide users when there is no content, rather than showing a blank screen.

#### Structure

```
         [Illustration — 160×160px SVG]

         [Headline — Heading 2]

         [Supporting text — Body Medium, color-text-secondary]
         [max 2 lines, centered]

         [CTA Button — Primary, optional]
```

- Container: centered vertically and horizontally within the content area
- Top margin from nav: 80px minimum
- Illustration: Simple, line-art style SVG in brand color tones

---

## 9. Screen Specifications — Web

### 9.1 Landing / Login Page (`/login`)

**Purpose:** Entry point for returning users. Authenticate with email and password.

**Layout:** Split two-column on desktop; single column (auth form only) on mobile.

```
Desktop:
┌─────────────────────┬──────────────────────┐
│                     │                      │
│   Brand visual      │    Auth form         │
│   Tagline           │                      │
│   Illustration      │                      │
│                     │                      │
└─────────────────────┴──────────────────────┘
  50% viewport width    50% viewport width
```

**Left panel (desktop only):**
- Background: gradient from `#4F46E5` to `#312E81`
- Boondi logo (wordmark, white) — top-left, 32px height
- Tagline: Display Medium, white, centered vertically
- Supporting text: Body Large, `#E0E7FF`, below tagline
- Subtle geometric pattern or illustration SVG in white at low opacity

**Right panel / Full screen (mobile):**
- Background: `color-background`
- Logo centered at top (mobile) or hidden (desktop, shown in left panel)
- Heading: "Welcome back" — Heading 1
- Subtext: "Sign in to your Boondi account" — Body Medium, `color-text-secondary`
- Form fields (top to bottom):
  - Email input (`type=email`, `autocomplete=email`)
  - Password input (`type=password`, show/hide toggle eye icon as trailing icon)
  - "Forgot password?" link — right-aligned below password field, Body Small, `color-primary`
  - Sign In button — Primary variant, full width, lg size
  - Divider: horizontal line with centered "or" label in `color-text-tertiary`
  - "Don't have an account? **Register**" — centered, Body Medium

**States:**
- Loading: Sign In button shows spinner, form disabled, cursor not-allowed
- Error: Inline error message below form — "Invalid email or password." in `color-error`, Body Small
- Success: Redirect to `/home` (or `?redirect=` param destination)

**Interactions:**
- Enter key on any focused field submits the form
- "Forgot password?" — opens a modal with a single email input and "Send reset link" button
- "Register" link — `<a>` navigates to `/register`

---

### 9.2 Registration Page (`/register`)

**Purpose:** Create a new Boondi account. MVP may require an invite token or go to an admin-approval queue.

**Layout:** Centered single-column card on all breakpoints. Card max-width: 480px, `margin: 0 auto`, padding 32px.

**Form fields (in order):**
1. **Display Name** — text input, placeholder "Your name or nickname", max 50 chars
2. **Username** — text input, placeholder "@handle", max 20 chars; lowercase letters, numbers, underscores only
   - Real-time availability check: debounced 500ms after keystroke
   - Availability states: checking (spinner in trailing), available (green check icon + "Available" helper), taken (red X icon + "Already taken" error)
3. **Email** — email input, `autocomplete=email`
4. **Password** — password input, show/hide toggle; min 8 characters
   - Password strength meter: 4-segment bar below field (Weak / Fair / Good / Strong)
5. **Confirm Password** — password input; validated on blur against password field
6. **Terms checkbox** — "I agree to the [Terms of Service] and [Privacy Policy]" with inline links
7. **Register button** — Primary, full width, disabled until all fields valid + terms checked

**Validation rules:**
- Username: `^[a-z0-9_]{3,20}$`
- All fields validated on blur (not on keystroke) to avoid premature errors
- Submit validates all fields again before API call

**After submit success:** Navigate to a "Check your email" screen — centered email icon + "Verify your email to continue" message.

**After submit error (duplicate email):** Inline error on email field.

---

### 9.3 Home Feed (`/home`)

**Purpose:** The primary daily-use screen. Shows posts from users the logged-in user follows.

**Layout (desktop, ≥ 1280px):**
```
┌──────────────┬─────────────────────┬──────────────────┐
│  Navigation  │    Main Feed        │   Right Panel    │
│  Sidebar     │    (600px)          │   (340px)        │
│  (280px)     │                     │                  │
└──────────────┴─────────────────────┴──────────────────┘
```

**Main Feed (center column):**
- Sticky header bar (64px, `position: sticky; top: 0; z-index: 10`, `color-surface` background with bottom border):
  - Left: "Home" — Heading 2
  - Below title (or inline on mobile): timeline tab switcher
- Timeline tabs: `Home | Latest | Trending` — Filter chip row, horizontally scrollable on mobile if needed
- Compose box (desktop only, below tabs, above first post):
  ```
  ┌────────────────────────────────────────────────────┐
  │  [Avatar md]  [Textarea: "What's on your mind?"]  │
  │               ──────────────────────────────────   │
  │               [🖼 Image]  [# Hashtag]  [Post btn] │
  └────────────────────────────────────────────────────┘
  ```
- Post list: `PostCard` components, `color-border-subtle` (1px) dividers between cards, no card margin
- **Infinite scroll:** Triggers at 200px before bottom; appends 3 `PostCardSkeleton` items, then replaces with real data
- **Pull-to-refresh (mobile):** Spinner appears; refreshes feed from top

**Right Panel (desktop):**
- Sticky from top of viewport
- Search bar (navigates to `/search` on interaction — not an inline search)
- Section "Trending on Boondi": top 5 hashtag chips with post count (Body Small, `color-text-secondary`)
- Section "Who to follow": 3 compact `UserCard` components with Follow buttons
- "Show more suggestions" — Tertiary button → `/search?tab=users`

**Floating Compose Button (mobile, < 768px):**
- Position: `fixed`, bottom 80px (above bottom nav), right 16px
- Size: 56×56px circle, `color-primary` background, white edit icon (24px)
- On tap: opens Post Composer as a full-screen modal overlay

---

### 9.4 Post Composer (Modal + `/compose`)

**Purpose:** Write and publish a new post.

**Web (desktop):** Modal dialog, centered, max-width 600px, `radius-xl`.
**Web (mobile):** Full-screen overlay.

**Modal layout:**
```
┌─────────────────────────────────────────────────┐
│  [✕ Close]          [New Post]                  │
│  ─────────────────────────────────────────────  │
│  [Avatar md]  [Textarea — auto-expanding]        │
│                                                  │
│  [Image preview grid — up to 4 images]           │
│  [each has ✕ remove button, top-right corner]   │
│                                                  │
│  [Quote post embed card — if composing quote]   │
│  ─────────────────────────────────────────────  │
│  [🖼 Image]  [# Tag]    [420/500]  [Post btn]  │
└─────────────────────────────────────────────────┘
```

**Specifications:**
- Textarea: `min-height: 120px`; expands to `max-height: 400px`, then scrolls; no visible border
- Character counter: always visible, format `{current}/500`
  - Default: `color-text-tertiary`
  - At 400+: `color-warning`
  - At 476+: `color-error`
  - At 500: block further input
- Image upload: click image icon → OS file picker; also supports drag-and-drop onto the entire composer
  - Accepted MIME types: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
  - Max file size: 5MB per image
  - Max images: 4
  - Preview grid layout: 1 image = full width; 2 = 50/50 side by side; 3 = one full top + two half bottom; 4 = 2×2 grid
  - Each preview: `aspect-ratio: 16/9` (or native aspect capped at 4:5); remove button is absolute-positioned circle (24px, `color-surface` bg, X icon)
- Post button: Primary, sm, disabled until textarea has at least 1 non-whitespace character or at least 1 image
- Keyboard shortcut: `Ctrl+Enter` / `Cmd+Enter` submits
- Close (✕): if content or images exist → show "Discard post?" confirmation dialog with "Discard" (Destructive) and "Keep editing" (Secondary) buttons

---

### 9.5 Post Detail Page (`/post/{id}`)

**Purpose:** Show a single post in its entirety with full reply thread.

**Layout:** Uses the standard 3-column layout; content in center column.

**Center column structure (top to bottom):**
1. Back navigation: `← Back` (arrow-left icon + "Back" label, Tertiary button style, navigates `history.back()`)
2. "Post" — Heading 2 (page section title)
3. Full `PostCard` (no content truncation; images shown at full width)
4. Post stats bar: `{n} Likes · {n} Replies · {n} Reposts` — Body Small, `color-text-secondary`, 12px top margin
5. `color-border` divider
6. Reply composer (shown only when logged in):
   ```
   [Avatar md]  [Textarea: "Post your reply..."]
                ─────────────────────────────────
                             [{n}/500]  [Reply btn]
   ```
7. `color-border` divider
8. Replies list — sorted chronologically (oldest first)
   - Each reply is a `PostCard` with the "Replying to @username" label above
   - Nested replies (replies to replies): indented 32px left, with a 2px vertical `color-border` connector line from parent avatar center to child avatar center

---

### 9.6 User Profile Page (`/profile/{username}`)

**Purpose:** View a user's public information and their content.

**Header (full-width, above 3-column grid):**
```
┌────────────────────────────────────────────────────────┐
│  [Banner image — full width, 200px tall, 3:1 ratio]   │
│                                  [Edit Profile / Follow │
│  [Avatar xl — 80px, 3px white   button — top-right]   │
│   ring, positioned -40px from   ]                      │
│   bottom of banner]                                    │
└────────────────────────────────────────────────────────┘
```

**Profile info section (below header, 16px padding):**
- Display Name: Heading 1
- Username: `@handle` — Body Large, `color-text-secondary`
- Bio: Body Medium, max 3 lines; "Read more" text button if truncated
- Join date: calendar icon (16px) + "Joined {Month Year}" — Caption, `color-text-tertiary`
- Follower / Following: `**{n} Followers** · **{n} Following**` — Body Medium; numbers bold; labels regular; both are clickable links to follower/following pages

**Action button (top-right of profile section):**
- **Own profile:** "Edit Profile" — Secondary button
- **Other (not following):** "Follow" — Primary button
- **Other (following):** "Following" — Secondary button (hover reveals "Unfollow" in `color-error` tone)
- **Other (following you):** "Follows you" badge shown near username as a small chip

**Profile tabs:**
`Posts | Replies | Media | Likes | Bookmarks`
- Bookmarks tab visible only on own profile
- Media tab: 3-column image grid; each image is a link to the parent post; `aspect-ratio: 1`
- Active tab: `color-primary` underline indicator, 2px, weight 600 label

---

### 9.7 Followers / Following Page (`/profile/{username}/followers`)

**Purpose:** Browse the list of users who follow or are followed by a profile.

**Layout:** Center column, max 600px.

**Header:** Back arrow + "`@{username}`'s Followers" or "Following" — Heading 2

**Tab switcher:** `Followers | Following` — underline style, directly below header

**Content:** List of Full `UserCard` components with follow/following buttons. Paginated (load more on scroll).

**Empty state:**
- Followers empty: "No followers yet" + "Be the first to follow @{username}"
- Following empty: "@{username} isn't following anyone yet"

---

### 9.8 Notifications Page (`/notifications`)

**Purpose:** See all social interactions directed at the logged-in user.

**Layout:** 3-column desktop, center column.

**Center column:**
- Sticky header (64px): "Notifications" (Heading 2, left) + "Mark all as read" (Tertiary button, right)
- Filter tabs: `All | Mentions | Likes | Follows | Reposts` — underline tab style
- Notification item list — see component 8.7 for anatomy
- Unread items: subtle `color-primary-subtle` row background, 8px `color-primary` dot on left edge
- Read all: clicking "Mark all as read" clears all dots and backgrounds

**Notification badge (nav icon):** Cleared on page entry (`useEffect` on mount marks all read via API).

---

### 9.9 Search Page (`/search`)

**Purpose:** Discover people, posts, and trending content.

**URL patterns:**
- `/search` — empty, shows recent + trending
- `/search?q={query}` — shows results, defaults to Top tab
- `/search?q={query}&tab=users` — shows Users tab

**Layout:** 3-column desktop. Center column has search input + results. Right panel has trending suggestions.

**Center column:**
- Search input (full width, always auto-focused on page load)
- Tabs (shown after query entered): `Top | Users | Posts | Hashtags`
- Results per tab:
  - **Top:** Mixed — 2–3 user cards first, then post cards; most relevant
  - **Users:** Full `UserCard` list
  - **Posts:** `PostCard` list (trimmed variant — no compose reply bar inline)
  - **Hashtags:** Row per hashtag: hashtag chip + post count + "View posts" Tertiary link

**Empty / pre-search state:**
- Section "Recent searches": last 5 queries from `localStorage`, each with ✕ remove button
- Section "Trending now": 5 hashtag chips with post count

**No results:**
- Illustration: magnifying glass with question mark
- Headline: `No results for "{query}"`
- Subtext: "Try different keywords or search for a hashtag like #topic"

---

### 9.10 Settings Page (`/settings`)

**Purpose:** Manage account, profile, privacy, appearance, and account lifecycle.

**Layout (desktop):** Two-column — left settings nav (200px fixed), right content area. On mobile: single column, sections as accordion groups.

**Settings navigation items (left):**
- Account, Profile, Notifications, Privacy, Appearance, Sessions, Danger Zone

**Section: Account**
- Email address (display + "Change" link → modal with current password + new email)
- Username (display + "Change" link → inline form with availability check)

**Section: Profile**
- Display Name (text input)
- Bio (textarea, 160 char max)
- Avatar (current image + "Upload new" button + "Remove" option)
- Banner (current image + "Upload new" + "Remove")
- Save Changes button (Primary, full width)

**Section: Notifications**
- Toggle rows: Likes on my posts, Replies to my posts, New followers, Mentions, Reposts
- Each toggle: label (Body Medium) + description (Body Small, `color-text-secondary`) + Toggle switch (right)

**Section: Privacy**
- "Who can reply to my posts": Radio group — Everyone / People I follow / No one
- "Make my profile private": Toggle switch (hides posts from non-followers)

**Section: Appearance**
- Theme: Segmented button — Light | System | Dark
- (Future: font size preference)

**Section: Sessions**
- Table: Device / Browser + Location + Last active + "Sign out" button per session
- "Sign out all other sessions" — Destructive Tertiary button

**Section: Danger Zone**
- "Delete Account" — Destructive button
- Click → opens confirmation dialog: "Type DELETE to confirm" input + "Permanently delete my account" Destructive button
- On confirm: session cleared, account queued for deletion, redirected to `/login` with "Account deleted" toast

**Sign Out (bottom of nav):** Tertiary button, confirms via dialog, then redirects to `/login`.

---

### 9.11 Admin Panel (`/admin`)

**Purpose:** Platform moderation and management. Accessible only to users with `role: admin`.

**Access control:** Protected route — any non-admin accessing `/admin/*` is redirected to `/home`.

**Layout:** Left admin nav sidebar (200px) + content area.

**Admin nav sections:**
- Dashboard, Users, Reports, Announcements, Invites

**Dashboard:**
- Stat cards: Total Users, Posts Today, Open Reports, New Signups (7d)
- Each stat card: large number (Display Medium) + label (Caption) + trend indicator

**User Management (`/admin/users`):**
- Searchable, sortable table: Avatar + Name + @Username + Email + Joined + Status + Actions
- Status: Active / Suspended / Pending verification
- Actions per row: Suspend / Unsuspend / Delete (each behind confirmation dialog)
- Pagination: 50 users per page

**Reports Queue (`/admin/reports`):**
- List items: Reporter avatar + "Reported {user/post}" + Reason + Date + [Content preview accordion] + Actions: Dismiss / Remove Content / Suspend User
- Filter by: Status (Open / Resolved), Type (Post / User)

**Announcements (`/admin/announcements`):**
- Textarea: compose an announcement (pinned to top of all feeds until dismissed)
- "Post Announcement" Primary button
- List of past announcements with delete option

**Invite Management (`/admin/invites`):**
- "Generate invite link" button → creates single-use or multi-use invite URL
- List of active invites: URL + uses remaining + expiry + Revoke button

---

## 10. Screen Specifications — Android

### 10.1 Splash Screen

**Composable:** `SplashScreen`
**Navigation route:** Initial destination before auth check resolves
**Top App Bar:** None
**Bottom Bar:** Hidden

**Implementation:**
- Use Android 12+ `SplashScreen API` (`androidx.core:core-splashscreen`) for system-native splash
- Set `windowSplashScreenBackground` to `#4F46E5` (brand primary)
- Set `windowSplashScreenAnimatedIcon` to the Boondi icon mark (animated or static)
- After splash: navigate based on auth token presence:
  - Token valid → `HomeGraph`
  - No token / expired → `AuthGraph/LoginScreen`

**Fallback Composable (API < 31):**
```kotlin
Box(
    modifier = Modifier.fillMaxSize().background(Color(0xFF4F46E5)),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ic_boondi_logo), contentDescription = "Boondi", modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("boondi", style = MaterialTheme.typography.headlineLarge, color = Color.White)
    }
}
```

---

### 10.2 Login Screen

**Composable:** `LoginScreen(viewModel: LoginViewModel, navController: NavController)`
**Route:** `auth/login`
**Top App Bar:** None
**Bottom Bar:** Hidden

**Layout (vertically scrollable Column, padding 24dp horizontal):**
```
Spacer(40dp)
BoondiLogo (icon 64dp + wordmark, centered)
Spacer(40dp)
Text("Welcome back", titleLarge, centered)
Text("Sign in to your Boondi account", bodyMedium, colorSecondary, centered)
Spacer(32dp)
OutlinedTextField(email, keyboardType=Email, imeAction=Next)
Spacer(16dp)
OutlinedTextField(password, keyboardType=Password, imeAction=Done, trailingIcon=EyeToggle)
TextButton("Forgot password?", Modifier.align(End))
Spacer(24dp)
Button("Sign In", Modifier.fillMaxWidth(), enabled=!isLoading)
Spacer(16dp)
HorizontalDivider with "or" label
Spacer(16dp)
TextButton("Don't have an account? Register", centered)
```

**UX details:**
- `ImeAction.Next` on email field moves focus to password field
- `ImeAction.Done` on password field triggers login
- `isLoading = true`: button shows `CircularProgressIndicator` (size 20dp, strokeWidth 2dp), button disabled
- API error: `Snackbar` at bottom with error message (not inline form error on Android — use snackbar pattern)

---

### 10.3 Register Screen

**Composable:** `RegisterScreen(viewModel: RegisterViewModel, navController: NavController)`
**Route:** `auth/register`
**Top App Bar:** `TopAppBar(title = {}, navigationIcon = { BackArrow })` — back to Login
**Bottom Bar:** Hidden

**Layout:** Vertically scrollable `Column`, padding 24dp, `verticalScroll(rememberScrollState())`

Fields (each `OutlinedTextField`):
1. Display Name — `imeAction=Next`
2. Username — `imeAction=Next`, `supportingText` shows availability state
3. Email — `keyboardType=Email`, `imeAction=Next`
4. Password — `keyboardType=Password`, `imeAction=Next`; `LinearProgressIndicator` below for strength
5. Confirm Password — `keyboardType=Password`, `imeAction=Done`
6. `Row { Checkbox(checked, onChecked); Text("I agree to Terms and Privacy Policy") }`
7. `Button("Create Account", Modifier.fillMaxWidth(), enabled=formValid && !isLoading)`

---

### 10.4 Home Feed Screen

**Composable:** `HomeFeedScreen(viewModel: HomeFeedViewModel, navController: NavController)`
**Route:** `main/home`
**Top App Bar:** `CenterAlignedTopAppBar` — center: Boondi wordmark; end: `IconButton(NotificationsBell)` with `BadgedBox` if unread
**Bottom Bar:** Visible

```kotlin
Scaffold(
    topBar = { HomeFeedTopBar(onNotificationsClick = { navController.navigate("main/notifications") }) },
    bottomBar = { BoondiBottomNav(navController, currentRoute) },
    floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text("Post") },
            icon = { Icon(Icons.Rounded.Edit, null) },
            onClick = { navController.navigate("main/compose") },
            expanded = !listState.isScrollingUp()  // collapses on scroll down
        )
    }
) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
        TimelineTabRow(selectedTab = uiState.tab, onTabSelected = viewModel::onTabChange)
        PullToRefreshBox(isRefreshing = uiState.isRefreshing, onRefresh = viewModel::refresh) {
            LazyColumn(state = listState) {
                items(uiState.posts, key = { it.id }) { post ->
                    PostCard(post = post, onLike = viewModel::onLike, onReply = { navController.navigate("main/post/${post.id}") }, ...)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                if (uiState.isLoadingMore) {
                    items(3) { PostCardSkeleton() }
                }
            }
        }
    }
}
```

**Scroll behavior:**
- `listState.isScrollingUp()` extension to collapse/expand FAB
- Load more: `LazyListState.reachedBottom` derived state triggers `viewModel.loadMore()`

---

### 10.5 Post Composer Screen

**Composable:** `PostComposerScreen(viewModel: PostComposerViewModel, navController: NavController)`
**Route:** `main/compose`
**Top App Bar:** Custom — Left: `TextButton("Cancel")`; Center: `Text("New Post")`; Right: `Button("Post", enabled=contentValid)`
**Bottom Bar:** Hidden; replaced by `ComposerBottomBar`

```kotlin
Scaffold(
    topBar = { ComposerTopBar(onCancel = { showDiscardDialog = true }, onPost = viewModel::submitPost, canPost = uiState.canPost) },
    bottomBar = { ComposerBottomBar(onImagePick = viewModel::onImagePick, charCount = uiState.charCount, maxChars = 500) }
) { padding ->
    Column(modifier = Modifier.padding(padding).fillMaxSize()) {
        Row(modifier = Modifier.padding(16.dp)) {
            BoondiAvatar(currentUser, size = AvatarSize.Medium)
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = uiState.text,
                onValueChange = viewModel::onTextChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                decorationBox = { inner ->
                    if (uiState.text.isEmpty()) Text("What's on your mind?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    inner()
                }
            )
        }
        if (uiState.images.isNotEmpty()) {
            ImagePreviewGrid(images = uiState.images, onRemove = viewModel::removeImage)
        }
    }
}
```

**Back press handling:**
```kotlin
BackHandler(enabled = uiState.hasContent) {
    showDiscardDialog = true
}
```

---

### 10.6 Post Detail Screen

**Composable:** `PostDetailScreen(postId: String, viewModel: PostDetailViewModel, navController: NavController)`
**Route:** `main/post/{postId}`
**Top App Bar:** `TopAppBar(title = { Text("Post") }, navigationIcon = { BackArrow })`
**Bottom Bar:** Hidden; reply composer bar pinned to bottom

```kotlin
Scaffold(
    topBar = { PostDetailTopBar(onBack = navController::popBackStack) },
    bottomBar = {
        ReplyComposerBar(
            currentUser = currentUser,
            onReply = viewModel::submitReply,
            charCount = replyText.length
        )
    }
) { padding ->
    LazyColumn(modifier = Modifier.padding(padding)) {
        item { FullPostCard(uiState.post) }
        item { PostStatsRow(uiState.post) }
        item { HorizontalDivider() }
        items(uiState.replies, key = { it.id }) { reply ->
            PostCard(reply, isReply = true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
```

**`ReplyComposerBar`:** `Surface(tonalElevation = 2.dp)` with `Row`: `BoondiAvatar` + `BasicTextField("Post your reply…")` + `TextButton("Reply", enabled = replyText.isNotBlank())`

---

### 10.7 User Profile Screen

**Composable:** `UserProfileScreen(username: String, viewModel: UserProfileViewModel, navController: NavController)`
**Route:** `main/profile/{username}`
**Top App Bar:** Transparent at top; becomes opaque with username title as user scrolls past header
**Bottom Bar:** Visible

**Collapsing header pattern:**
```kotlin
val listState = rememberLazyListState()
val showTitleInBar by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 200 }
}

Scaffold(
    topBar = {
        TopAppBar(
            title = { AnimatedVisibility(showTitleInBar) { Text("@${uiState.user.username}") } },
            navigationIcon = { BackArrow(navController::popBackStack) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = if (showTitleInBar) MaterialTheme.colorScheme.surface else Color.Transparent)
        )
    }
) { padding ->
    LazyColumn(state = listState, modifier = Modifier.padding(padding)) {
        item { ProfileHeaderCard(user = uiState.user, isOwnProfile = uiState.isOwnProfile, onFollowToggle = viewModel::toggleFollow, onEditProfile = { navController.navigate("settings/profile") }) }
        stickyHeader { ProfileTabRow(uiState.selectedTab, viewModel::onTabChange) }
        items(uiState.tabContent, key = { it.id }) { post ->
            PostCard(post)
            HorizontalDivider()
        }
    }
}
```

---

### 10.8 Notifications Screen

**Composable:** `NotificationsScreen(viewModel: NotificationsViewModel, navController: NavController)`
**Route:** `main/notifications`
**Top App Bar:** `TopAppBar(title = { Text("Notifications") }, actions = { TextButton("Mark all read", onClick = viewModel::markAllRead) })`
**Bottom Bar:** Visible

```kotlin
Column {
    NotificationFilterTabRow(uiState.filter, viewModel::onFilterChange)
    LazyColumn {
        items(uiState.notifications, key = { it.id }) { notification ->
            NotificationItem(
                notification = notification,
                onClick = { navController.navigate("main/post/${notification.postId}") }
            )
            HorizontalDivider()
        }
    }
}
```

**Unread row styling:**
```kotlin
val bgColor = if (!notification.isRead)
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
else Color.Transparent
ListItem(modifier = Modifier.background(bgColor), ...)
```

---

### 10.9 Search Screen

**Composable:** `SearchScreen(viewModel: SearchViewModel, navController: NavController)`
**Route:** `main/search`
**Top App Bar:** Replaced by Material 3 `SearchBar` at top of screen (full-width)
**Bottom Bar:** Visible

```kotlin
Column {
    SearchBar(
        query = uiState.query,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        active = uiState.isSearchBarActive,
        onActiveChange = viewModel::onActiveChange,
        placeholder = { Text("Search Boondi") },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (uiState.query.isNotEmpty()) {
                IconButton(onClick = viewModel::clearQuery) { Icon(Icons.Rounded.Close, "Clear") }
            }
        }
    ) {
        // Suggestions inside SearchBar while active
        RecentSearches(uiState.recentSearches, onSelect = viewModel::onSearchSelect, onRemove = viewModel::removeRecentSearch)
    }
    if (uiState.query.isNotEmpty()) {
        SearchResultTabRow(uiState.tab, viewModel::onTabChange)
        SearchResultsList(uiState.results, uiState.tab, navController)
    } else {
        TrendingSection(uiState.trending)
    }
}
```

---

### 10.10 Settings Screen

**Composable:** `SettingsScreen(viewModel: SettingsViewModel, navController: NavController)`
**Route:** `settings/main`
**Top App Bar:** `TopAppBar(title = { Text("Settings") }, navigationIcon = { BackArrow })`
**Bottom Bar:** Hidden

```kotlin
LazyColumn {
    item { SettingsSectionHeader("Account") }
    item { SettingsNavigationItem("Email", uiState.user.email) { navController.navigate("settings/account/email") } }
    item { SettingsNavigationItem("Username", "@${uiState.user.username}") { navController.navigate("settings/account/username") } }
    item { SettingsNavigationItem("Change Password") { navController.navigate("settings/password") } }

    item { SettingsSectionHeader("Appearance") }
    item {
        ThemeSetting(currentTheme = uiState.theme, onThemeChange = viewModel::setTheme)
        // ThemeSetting uses SegmentedButton: Light | System | Dark
    }

    item { SettingsSectionHeader("Notifications") }
    // Toggle items for each notification type

    item { SettingsSectionHeader("Privacy") }
    // Radio group for reply permissions

    item { Spacer(Modifier.height(24.dp)) }
    item {
        OutlinedButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Sign Out") }
    }
    item { Spacer(Modifier.height(16.dp)) }
    item {
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("Delete Account") }
    }
}
```

---

## 11. Navigation Architecture

### 11.1 Web Navigation

**Full Route Tree:**

```
/ (root) → redirect to /home if authenticated, else /login

PUBLIC ROUTES (unauthenticated access only):
├── /login
├── /register
├── /forgot-password
└── /reset-password?token={token}

PROTECTED ROUTES (authentication required):
├── /home                            ← Home Feed (default post-login)
├── /compose                         ← Post Composer (modal on desktop, full-page on mobile)
├── /post/{id}                       ← Post Detail
├── /post/{id}/likes                 ← Users who liked this post
│
├── /profile/{username}              ← User Profile
├── /profile/{username}/followers    ← Followers list
├── /profile/{username}/following    ← Following list
│
├── /notifications                   ← Notifications
├── /search                          ← Search (+ query params)
├── /bookmarks                       ← Saved posts
│
├── /settings                        ← Settings root (redirects to /settings/profile)
│   ├── /settings/profile            ← Edit profile (name, bio, avatar, banner)
│   ├── /settings/account            ← Email, username
│   ├── /settings/password           ← Change password
│   ├── /settings/notifications      ← Notification preferences
│   ├── /settings/privacy            ← Privacy controls
│   ├── /settings/appearance         ← Theme selection
│   └── /settings/sessions           ← Active sessions
│
├── /404                             ← Not found (also shown on unknown routes)
│
└── /admin                           ← ADMIN ONLY (role check)
    ├── /admin/users
    ├── /admin/reports
    ├── /admin/announcements
    └── /admin/invites
```

**Auth Redirect Logic:**

| Scenario | Behavior |
|---|---|
| Unauthenticated → protected route | Redirect to `/login?redirect={encodedUrl}` |
| Successful login with `redirect` param | Redirect to decoded `redirect` URL |
| Successful login without `redirect` param | Redirect to `/home` |
| Authenticated → `/login` or `/register` | Redirect to `/home` |
| Non-admin → `/admin/*` | Redirect to `/home` |
| Any route not in tree | Show `/404` page (no redirect) |

**React Router v6 Implementation Pattern:**

```tsx
// ProtectedRoute.tsx
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />;
  }
  return <Outlet />;
}

// PublicOnlyRoute.tsx (redirect already-authenticated users)
export function PublicOnlyRoute() {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) return <Navigate to="/home" replace />;
  return <Outlet />;
}

// AdminRoute.tsx
export function AdminRoute() {
  const { user } = useAuth();
  if (user?.role !== 'admin') return <Navigate to="/home" replace />;
  return <Outlet />;
}

// App.tsx router structure
<BrowserRouter>
  <Routes>
    <Route element={<PublicOnlyRoute />}>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
    </Route>
    <Route element={<ProtectedRoute />}>
      <Route element={<MainLayout />}>
        <Route path="/home" element={<HomeFeedPage />} />
        <Route path="/post/:id" element={<PostDetailPage />} />
        <Route path="/profile/:username" element={<UserProfilePage />} />
        <Route path="/profile/:username/followers" element={<FollowersPage />} />
        <Route path="/profile/:username/following" element={<FollowingPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/bookmarks" element={<BookmarksPage />} />
        <Route path="/settings/*" element={<SettingsPage />} />
      </Route>
      <Route element={<AdminRoute />}>
        <Route path="/admin/*" element={<AdminPanel />} />
      </Route>
    </Route>
    <Route path="/" element={<Navigate to="/home" replace />} />
    <Route path="*" element={<NotFoundPage />} />
  </Routes>
</BrowserRouter>
```

---

### 11.2 Android Navigation

**Navigation Graph Overview:**

```
AppNavHost
│
├── AuthGraph  (startDestination = "auth/login")
│   ├── auth/login         → LoginScreen
│   ├── auth/register      → RegisterScreen
│   └── auth/forgot        → ForgotPasswordScreen
│
└── MainGraph  (startDestination = "main/home")
    ├── main/home                          → HomeFeedScreen
    ├── main/compose                       → PostComposerScreen
    ├── main/post/{postId}                 → PostDetailScreen
    ├── main/profile/{username}            → UserProfileScreen
    ├── main/profile/{username}/followers  → FollowersScreen
    ├── main/profile/{username}/following  → FollowingScreen
    ├── main/notifications                 → NotificationsScreen
    ├── main/search                        → SearchScreen
    ├── main/bookmarks                     → BookmarksScreen
    └── SettingsGraph (startDestination = "settings/main")
        ├── settings/main       → SettingsScreen
        ├── settings/profile    → EditProfileScreen
        ├── settings/password   → ChangePasswordScreen
        └── settings/sessions   → SessionsScreen
```

**Bottom Navigation Destinations:**

| Index | Destination | Route | Icon |
|---|---|---|---|
| 0 | Home | `main/home` | `Icons.Rounded.Home` |
| 1 | Search | `main/search` | `Icons.Rounded.Search` |
| 2 | Activity | `main/notifications` | `Icons.Rounded.Notifications` |
| 3 | Profile | `main/profile/{currentUsername}` | `Icons.Rounded.Person` |

**Back Stack Behavior:**

```kotlin
// Bottom nav item click with proper back stack management
fun NavController.navigateBottomNav(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
```

- `saveState = true`: Preserves scroll position, loaded data when switching tabs
- `launchSingleTop = true`: Prevents duplicate instances on re-tap of current tab
- `restoreState = true`: Restores saved state when returning to a tab
- Back press on any root bottom nav destination:

```kotlin
// In each root screen composable
BackHandler(enabled = true) {
    // If on home tab → exit app
    // If on other tab → navigate to home tab
    if (currentRoute == "main/home") {
        (context as? Activity)?.finish()
    } else {
        navController.navigateBottomNav("main/home")
    }
}
```

**Full NavGraph Definition:**

```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Auth Graph
        navigation(startDestination = "auth/login", route = "auth") {
            composable("auth/login") {
                LoginScreen(onLoginSuccess = { navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }})
            }
            composable("auth/register") { RegisterScreen(navController) }
            composable("auth/forgot") { ForgotPasswordScreen(navController) }
        }

        // Main Graph
        navigation(startDestination = "main/home", route = "main") {
            composable("main/home") { HomeFeedScreen(navController) }
            composable("main/search") { SearchScreen(navController) }
            composable("main/notifications") { NotificationsScreen(navController) }
            composable(
                "main/profile/{username}",
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { backStack ->
                UserProfileScreen(
                    username = backStack.arguments!!.getString("username")!!,
                    navController = navController
                )
            }
            composable(
                "main/post/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStack ->
                PostDetailScreen(
                    postId = backStack.arguments!!.getString("postId")!!,
                    navController = navController
                )
            }
            composable("main/compose") { PostComposerScreen(navController) }
            composable("main/bookmarks") { BookmarksScreen(navController) }

            // Settings nested graph
            navigation(startDestination = "settings/main", route = "settings") {
                composable("settings/main") { SettingsScreen(navController) }
                composable("settings/profile") { EditProfileScreen(navController) }
                composable("settings/password") { ChangePasswordScreen(navController) }
            }
        }
    }
}
```

---

## 12. Interaction & Animation

### 12.1 Transition Principles

1. **Purposeful:** Every animation communicates a state change, spatial relationship, or hierarchy. No decorative motion that serves only aesthetics.
2. **Fast:** Default UI feedback completes in under 200ms. Page transitions under 400ms. Users should never wait for an animation.
3. **Consistent:** Identical elements animate identically regardless of context. Like buttons always spring. Modals always fade+scale.
4. **Interruptible:** All animations can be cancelled mid-flight by user input (tap elsewhere, scroll, navigate back).
5. **Reduced-motion aware:** All animations degrade gracefully when the user has enabled reduced motion in system settings.

### 12.2 Animation Timing Reference

| Category | Duration | Easing Curve | Material 3 Equivalent | Usage |
|---|---|---|---|---|
| Instant | 0ms | — | — | Radio/checkbox state, text value updates |
| Micro | 100ms | ease-out | `FastOutLinearInEasing` | Button press feedback, hover background |
| Fast | 150ms | ease-out | `FastOutLinearInEasing` | Icon fill/color transitions, badge appear |
| Normal | 250ms | ease-in-out | `FastOutSlowInEasing` | Tab switch, card expand, follow button |
| Slow | 400ms | ease-in-out | `FastOutSlowInEasing` | Page transitions, drawer, shared element |
| Enter | 200ms | ease-out (decelerate) | `LinearOutSlowInEasing` | Elements entering viewport |
| Exit | 150ms | ease-in (accelerate) | `FastOutLinearInEasing` | Elements leaving viewport |

### 12.3 Specific Interaction Specifications

#### Like Button

**Trigger:** Single tap/click on heart icon.

**Web animation:**
```css
.like-button {
    transition: color 150ms ease-out, transform 300ms cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.like-button:active {
    transform: scale(0.85);
}
.like-button.liked {
    color: #F43F5E;
    transform: scale(1.0);
}
/* Keyframe for the pop */
@keyframes likeSpring {
    0%   { transform: scale(1.0); }
    40%  { transform: scale(1.3); }
    70%  { transform: scale(0.9); }
    100% { transform: scale(1.0); }
}
.like-button.just-liked {
    animation: likeSpring 350ms cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
```

**Android:**
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isLiked) 1f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)
val color by animateColorAsState(
    targetValue = if (isLiked) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant,
    animationSpec = tween(durationMillis = 150)
)
Icon(
    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
    tint = color,
    modifier = Modifier.scale(scale)
)
```

**Count:** Increments/decrements immediately (optimistic update). Rolls back with error `Snackbar` if API fails.

#### Follow Button

**Trigger:** Single tap/click "Follow" button.

**States and transitions:**
- Follow → Following: Primary (filled) → Secondary (outlined) with checkmark icon; 200ms cross-fade
- Following → Follow (unfollow): hover on web reveals "Unfollow" text in `color-error` tone; tap opens confirmation
- Optimistic update: button state changes immediately

**Web:**
```tsx
<button className={cn(
    "transition-all duration-200",
    isFollowing
        ? "border border-border text-text-secondary hover:border-error hover:text-error hover:bg-error-subtle"
        : "bg-primary text-white hover:bg-primary-hover"
)}>
    {isFollowing ? 'Following' : 'Follow'}
</button>
```

**Android:** `AnimatedContent(isFollowing)` with `fadeIn + fadeOut` transition spec.

#### Post Card Press Feedback

**Android:** Material 3 `Modifier.clickable` with `rememberRipple(color = MaterialTheme.colorScheme.primary, bounded = true)`. Ripple is confined to card bounds.

**Web:**
```css
.post-card {
    transition: background-color 100ms ease-out;
    cursor: pointer;
}
.post-card:hover {
    background-color: var(--color-border-subtle);
}
.post-card:active {
    background-color: var(--color-border);
}
```

#### Page Transitions — Web

All route changes use a consistent fade+slide pattern via React:
```tsx
// Using Framer Motion or CSS transitions
const pageVariants = {
    initial: { opacity: 0, y: 8 },
    animate: { opacity: 1, y: 0, transition: { duration: 0.2, ease: 'easeOut' } },
    exit:    { opacity: 0, transition: { duration: 0.15, ease: 'easeIn' } }
};
```

Modal open/close:
- Backdrop: fade 0 → 0.5 opacity, 200ms
- Dialog: scale 0.95 → 1.0 + fade, 200ms ease-out
- Close: reverse, 150ms

#### Page Transitions — Android

```kotlin
NavHost(
    navController = navController,
    enterTransition = {
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350, easing = LinearOutSlowInEasing)) +
        fadeIn(animationSpec = tween(200))
    },
    exitTransition = {
        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(350)) +
        fadeOut(animationSpec = tween(150))
    },
    popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(350)) +
        fadeIn(animationSpec = tween(200))
    },
    popExitTransition = {
        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350)) +
        fadeOut(animationSpec = tween(150))
    }
) { /* routes */ }
```

Profile screen: use `fadeIn`/`fadeOut` only (no slide) to avoid directional confusion since profile can be reached from multiple places.

#### Pull-to-Refresh

- **Web:** Custom component — spinner appears at 60px pull distance, action triggers at 80px. Spinner color: `color-primary`. Auto-hides after 500ms on data return.
- **Android:** Material 3 `PullToRefreshBox` with `indicator = { PullToRefreshDefaults.Indicator(state, isRefreshing, color = MaterialTheme.colorScheme.primary) }`

#### Infinite Scroll

1. User scrolls within 3 list items of the bottom (`LazyListState.reachedBottom` derived state)
2. `isLoadingMore` state set to true → 3 skeleton items appended to list
3. API call fetches next page
4. On success: skeleton items replaced with real items; `isLoadingMore` false
5. On failure: skeletons removed; `Snackbar` "Failed to load more posts. Retry."
6. If no more data: `isLoadingMore` never triggered again; optional "You're all caught up" footer item

#### Image Expand (Lightbox)

**Web:**
- Click any post image → modal overlay with full-resolution image centered
- Background: `rgba(0,0,0,0.9)`, `backdrop-filter: blur(4px)`
- Image: `max-width: 90vw; max-height: 90vh; object-fit: contain`
- Navigation arrows for multi-image posts (previous / next)
- Close: click outside image, or press `Escape`
- Enter animation: image fades + scales from 0.9 → 1.0, 200ms

**Android:**
- Tapping a post image navigates to a fullscreen `ImageViewerScreen`
- Use `SharedTransitionLayout` + `SharedTransitionScope` for shared element transition
- The image animates from its position in the card to fullscreen
- Pinch-to-zoom supported via `rememberTransformableState`

### 12.4 Micro-Interactions

**Web — Hover States:**
- All interactive elements: `transition: background-color 100ms ease-out, color 100ms ease-out`
- Navigation items: background `color-border-subtle` on hover
- Post card row: background `color-border-subtle` on hover (whole card)
- Action icons hover: icon color shifts toward active color at 60% opacity (e.g., heart icon goes slightly pink on hover before clicking)
- Buttons: `transform: translateY(-1px)` on hover for Primary buttons (subtle lift)
- Links: `text-decoration: underline` on hover + `color-primary`

**Android — Haptic Feedback:**

| User Action | Haptic Type |
|---|---|
| Like / Unlike post | `HapticFeedbackType.LongPress` (short pulse) |
| Follow / Unfollow | `HapticFeedbackType.TextHandleMove` (light tick) |
| Successful post submit | `HapticFeedbackType.Confirm` |
| Post submit failure | `HapticFeedbackType.Reject` |
| Long press on post (context menu) | `HapticFeedbackType.LongPress` |
| Delete confirmation | `HapticFeedbackType.Reject` |

```kotlin
val haptic = LocalHapticFeedback.current
IconButton(onClick = {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onLike()
}) {
    Icon(Icons.Rounded.Favorite, contentDescription = "Like")
}
```

---

## 13. Accessibility

### 13.1 WCAG 2.1 AA Compliance Targets

Boondi targets full **WCAG 2.1 Level AA** compliance on both platforms. The following criteria receive specific implementation attention:

| Criterion | Level | Implementation in Boondi |
|---|---|---|
| 1.1.1 Non-text Content | A | All images have descriptive alt text; decorative images have `alt=""`; icons have `aria-label` or `contentDescription` |
| 1.3.1 Info and Relationships | A | Semantic HTML5 elements (`<nav>`, `<main>`, `<article>`, `<aside>`, `<header>`, `<footer>`); correct heading hierarchy (h1 → h2 → h3) |
| 1.3.3 Sensory Characteristics | A | "Like button" never described as "the red heart" — always uses text label or icon + text |
| 1.4.1 Use of Color | A | Error states always pair color with an icon (✕) and text message; like/bookmark state uses icon fill + color (not color alone) |
| 1.4.3 Contrast (Minimum) | AA | All normal text ≥ 4.5:1; large text and UI components ≥ 3:1 (verified with automated tools per CI) |
| 1.4.4 Resize Text | AA | All text in rem/sp; tested at 200% browser zoom without horizontal scroll |
| 1.4.11 Non-text Contrast | AA | Input borders, icon buttons, focus rings all ≥ 3:1 against their backgrounds |
| 1.4.13 Content on Hover | AA | Tooltips persist on hover; dismissible with `Escape`; not triggered by keyboard focus alone |
| 2.1.1 Keyboard | A | All functionality reachable by Tab, arrow keys, Enter, Escape; no mouse-only interactions |
| 2.1.2 No Keyboard Trap | A | Modal dialogs trap focus internally but always have an accessible close mechanism (`Escape`) |
| 2.4.3 Focus Order | A | Tab order follows visual reading order (left-to-right, top-to-bottom); no focus jumps |
| 2.4.7 Focus Visible | AA | Custom `:focus-visible` ring — 2px `color-primary`, 2px offset — visible on all interactive elements |
| 2.5.3 Label in Name | A | All buttons and links whose visible text is a label have that exact label in their accessible name |
| 3.2.2 On Input | A | No page navigation triggered by focus alone; all navigation requires explicit action (click/enter) |
| 4.1.2 Name, Role, Value | A | All custom components expose correct ARIA roles, states, and properties |
| 4.1.3 Status Messages | AA | Toast/snackbar messages use `role="status"` (web) or announced via `AccessibilityEvent` (Android) |

### 13.2 Color Contrast Requirements

**Verified combinations for Boondi's palette:**

| Foreground | Background | Contrast Ratio | Text Type | Pass/Fail |
|---|---|---|---|---|
| `#111827` (text-primary) | `#FFFFFF` (surface) | 17.1:1 | Normal | AA ✓ |
| `#4B5563` (text-secondary) | `#FFFFFF` | 7.0:1 | Normal | AA ✓ |
| `#4F46E5` (primary) | `#FFFFFF` | 5.0:1 | Normal | AA ✓ |
| `#FFFFFF` (text-inverse) | `#4F46E5` (primary) | 5.0:1 | Normal | AA ✓ |
| `#9CA3AF` (text-tertiary) | `#FFFFFF` | 2.9:1 | Normal | FAIL — use for decorative only |
| `#F9FAFB` (text-primary dark) | `#030712` (bg dark) | 16.0:1 | Normal | AA ✓ |
| `#9CA3AF` (text-secondary dark) | `#111827` (surface dark) | 5.8:1 | Normal | AA ✓ |
| `#818CF8` (primary dark) | `#111827` (surface dark) | 5.5:1 | Normal | AA ✓ |
| `#EF4444` (error) | `#FFFFFF` | 4.5:1 | Normal | AA ✓ (borderline — verify) |
| `#F43F5E` (like color) | `#FFFFFF` | 4.1:1 | Icon (large) | AA (large text only) ✓ |

**Rule:** `color-text-tertiary` (`#9CA3AF` light, `#6B7280` dark) must NEVER be used for text that conveys required information. Use only for timestamps, placeholder text, and decorative metadata.

### 13.3 Touch Target Minimums

| Platform | Minimum Target Size | Method |
|---|---|---|
| Android | 48×48dp | `Modifier.minimumInteractiveComponentSize()` (Compose) |
| Web desktop | 24×24px (WCAG 2.1 AA) | Ensure with CSS `min-width`/`min-height` |
| Web mobile | 44×44px (Apple HIG) / 48px recommended | Use `min-h-[44px] min-w-[44px]` Tailwind classes |

For small visual icons (24px) that are tap targets, wrap in a larger hit area:
```tsx
// Web
<button className="p-3 -m-3 rounded-full">  {/* visual 24px icon inside 48px touch area */}
    <Heart size={20} />
</button>
```

```kotlin
// Android
Box(
    modifier = Modifier
        .size(48.dp)  // minimum touch target
        .clickable(onClick = onLike),
    contentAlignment = Alignment.Center
) {
    Icon(Icons.Rounded.FavoriteBorder, modifier = Modifier.size(24.dp), contentDescription = "Like")
}
```

### 13.4 Screen Reader Support

**Web — Semantic Structure:**
```html
<body>
  <a href="#main" class="skip-link">Skip to main content</a>
  <nav aria-label="Main navigation">
    <ul>
      <li><a href="/home" aria-current="page">Home</a></li>
      <li><a href="/notifications" aria-label="Notifications, 3 unread">Notifications</a></li>
    </ul>
  </nav>
  <main id="main">
    <h1>Home</h1>
    <section aria-label="Timeline">
      <article aria-label="Post by Jane Smith, 2 hours ago">
        <header>
          <img src="..." alt="Jane Smith's profile photo" />
          <h2><a href="/profile/jane">Jane Smith</a></h2>
          <p><a href="/profile/jane">@jane</a> · <time datetime="2026-07-02T10:00:00Z">2h</time></p>
        </header>
        <p>Post content text here.</p>
        <footer>
          <button aria-label="Like post by Jane Smith, currently 12 likes" aria-pressed="false">
            <svg aria-hidden="true" focusable="false">...</svg>
            <span aria-hidden="true">12</span>
          </button>
          <button aria-label="Reply to Jane Smith's post, 4 replies">...</button>
        </footer>
      </article>
    </section>
  </main>
  <aside aria-label="Trending and suggestions">...</aside>
</body>
```

**Android — Content Descriptions:**
```kotlin
// Post card action bar
Row {
    IconButton(
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLike() },
        modifier = Modifier.semantics {
            contentDescription = "${if (isLiked) "Unlike" else "Like"} post. Currently $likeCount likes."
            role = Role.Button
        }
    ) {
        Icon(if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = null)
    }
    // Count label is separate, hidden from accessibility (number already in button description)
    Text(likeCount.toString(), modifier = Modifier.semantics { invisibleToUser() })
}

// Avatar
AsyncImage(
    model = user.avatarUrl,
    contentDescription = "${user.displayName}'s profile photo",
    modifier = Modifier.clip(CircleShape)
)

// Notification badge
BadgedBox(
    badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } },
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = if (unreadCount > 0) "$unreadCount unread notifications" else "Notifications"
    }
) { Icon(Icons.Rounded.Notifications, contentDescription = null) }
```

### 13.5 Keyboard Navigation (Web)

**Skip Link (first focusable element on every page):**
```html
<a
  href="#main-content"
  class="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-surface focus:text-primary focus:rounded-lg focus:ring-2 focus:ring-primary"
>
  Skip to main content
</a>
```

**Focus Style (global):**
```css
:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
    border-radius: 4px;
}
/* Remove default outline (replaced by :focus-visible) */
:focus:not(:focus-visible) {
    outline: none;
}
```

**Tab Order per page:**
1. Skip-to-main link
2. Navigation links (in DOM order: left sidebar top-to-bottom)
3. Main content area in document flow
4. Right panel (if present)

**Modal Focus Management:**
```tsx
// Trap focus in modal while open
useEffect(() => {
    if (isOpen) {
        const focusable = modalRef.current?.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const first = focusable?.[0] as HTMLElement;
        const last = focusable?.[focusable.length - 1] as HTMLElement;
        first?.focus();
        // Handle Tab / Shift+Tab to cycle within modal
    }
}, [isOpen]);
```

**Keyboard Shortcuts (documented, not hidden):**
| Key | Action |
|---|---|
| `Ctrl+Enter` / `Cmd+Enter` | Submit post/reply in composer |
| `Escape` | Close modal, dialog, or dropdown |
| `/` | Focus search input (when not in a text field) |
| `?` | Show keyboard shortcuts help (future) |

### 13.6 Reduced Motion Support

**Web:**
```css
@media (prefers-reduced-motion: reduce) {
    *,
    *::before,
    *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
        scroll-behavior: auto !important;
    }
}
```

**Exceptions — still need to work without motion:**
- Loading spinners: replace animated spinner with static "Loading…" text label when reduced-motion is on
- Like animation: instant color change (no spring pop)
- Page transitions: instant show/hide (no fade/slide)
- Skeleton shimmer: static gray block instead of animated gradient

**React detection:**
```tsx
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
```

**Android:**
```kotlin
val animationsEnabled = remember {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
}
// Use animationsEnabled to conditionally apply animationSpec vs snap()
```

### 13.7 Font Scaling Support (Android)

- All text dimensions use `sp` units — never hardcode text in `dp`
- All vertically-constrained containers (list items, chips, buttons) use `wrapContentHeight()` — no fixed `dp` height for any container that holds text
- Test all screens at **150% font scale** (Settings → Accessibility → Font Size) — no text truncation of essential information, no button text overflow
- Exception: PostCard action bar icons — these are always 20dp regardless of font scale; their counts (small numbers) may truncate at very large font scales, which is acceptable since the icon itself conveys the action
- Ensure `contentDescription` values on all interactive elements (font scaling does not affect icons, so the text description is the only accessible label)

---

## 14. Responsive Design

### 14.1 Breakpoints

| Token | Range | Layout Model | Navigation Pattern |
|---|---|---|---|
| `xs` | 0 – 374px | 1 column, 12px padding | Bottom tab bar (4 items), FAB compose |
| `sm` | 375px – 767px | 1 column, 16px padding | Bottom tab bar (4 items), FAB compose |
| `md` | 768px – 1023px | 1.5 col (icon sidebar + content) | Collapsed sidebar (72px, icons only) |
| `lg` | 1024px – 1279px | 2 col (sidebar 220px + content) | Sidebar with icons + labels (220px) |
| `xl` | ≥ 1280px | 3 col (280px + 600px + 340px) | Full sidebar (280px) + right panel |

**Tailwind screen config (in `tailwind.config.ts`):**
```js
screens: {
    'sm': '375px',
    'md': '768px',
    'lg': '1024px',
    'xl': '1280px',
    '2xl': '1536px',
}
```

### 14.2 Component Behavior at Each Breakpoint

**Navigation Component:**

| Breakpoint | Behavior |
|---|---|
| xs/sm | Fixed bottom tab bar (56px + safe area); compose = FAB (56px circle, fixed bottom-right above nav) |
| md | Left sidebar, icons only (72px wide), labels shown on hover via tooltip |
| lg | Left sidebar with icon + label (220px); compose button below nav items |
| xl | Full sidebar (280px); compose button full-width `Primary` style |

**Post Card:**

| Breakpoint | Behavior |
|---|---|
| xs/sm | Full viewport width; 16px left/right padding applied to text only (images bleed edge-to-edge at xs) |
| md | Contained within 100% of content column |
| lg/xl | Constrained to 600px feed column; centered |
| All sizes | Action bar stays single row; icons only at xs (no counts); counts shown from sm+ |

**Profile Page:**

| Breakpoint | Banner Height | Avatar Size | Stats Layout |
|---|---|---|---|
| xs/sm | 120px | 64px | Stacked (1 column) |
| md | 160px | 72px | 2-column grid |
| lg/xl | 200px | 80px | Inline row |

**Search Page:**

| Breakpoint | Search Input | Results |
|---|---|---|
| xs/sm | Full width, sticky top | Full width results below |
| md | Within content column | Results in content column; sidebar hidden |
| lg/xl | Within 600px center column | Trending/suggestions in right panel |

### 14.3 Image Handling

**Responsive images (web):**
```html
<picture>
    <source
        media="(min-width: 1280px)"
        srcset="image-1200w.webp 1200w, image-800w.webp 800w"
        type="image/webp"
    />
    <source
        srcset="image-800w.webp 800w, image-400w.webp 400w"
        type="image/webp"
    />
    <img
        src="image-800w.jpg"
        srcset="image-400w.jpg 400w, image-800w.jpg 800w"
        sizes="(max-width: 767px) 100vw, (max-width: 1279px) calc(100vw - 72px), 600px"
        alt="Descriptive alt text"
        loading="lazy"
        decoding="async"
    />
</picture>
```

**Aspect ratio placeholders (prevent layout shift):**
```css
.post-image-container {
    aspect-ratio: 16 / 9;
    background-color: var(--color-border-subtle);
    overflow: hidden;
}
.post-image-container img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
```

**Accepted aspect ratios for post images:**
- Landscape: 16:9 (enforced max)
- Square: 1:1
- Portrait: up to 4:5 (taller images capped at 4:5 ratio in display)

**Android image loading (Coil):**
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .placeholder(R.drawable.image_placeholder)
        .error(R.drawable.image_error)
        .build(),
    contentDescription = alt,
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(RoundedCornerShape(8.dp))
)
```

### 14.4 Mobile-First Approach

All CSS and component logic is written assuming the smallest screen first, with overrides added for larger breakpoints.

**Principle:** Default styles target `xs`/`sm`. Use `md:`, `lg:`, `xl:` Tailwind prefixes to enhance.

```tsx
// Example: Main layout structure
<div className="
    flex flex-col min-h-screen              /* mobile: single column, full height */
    md:flex-row                              /* tablet+: horizontal layout */
">
    {/* Sidebar — hidden mobile, visible desktop */}
    <nav className="
        hidden                               /* mobile: hidden */
        md:flex md:flex-col md:w-18          /* tablet: 72px icon sidebar */
        lg:w-56                              /* desktop: 220px */
        xl:w-70                              /* large desktop: 280px */
        md:fixed md:inset-y-0 md:left-0     /* sticky sidebar */
    ">
        <NavigationSidebar />
    </nav>

    {/* Main content */}
    <main className="
        flex-1 pb-16                         /* mobile: padding for bottom nav */
        md:ml-18 md:pb-0                     /* tablet: offset for sidebar */
        lg:ml-56                             /* desktop */
        xl:ml-70                             /* large desktop */
    ">
        <Outlet />
    </main>

    {/* Right panel — desktop only */}
    <aside className="
        hidden                               /* mobile + tablet: hidden */
        xl:block xl:w-85 xl:fixed xl:right-0 xl:inset-y-0  /* large desktop */
    ">
        <RightPanel />
    </aside>
</div>
```

---

## 15. Dark Mode

### 15.1 Toggle Mechanism

Boondi supports three theme options, user-controlled in **Settings → Appearance**:

| Option | Behavior | Storage Value |
|---|---|---|
| **System** | Follows OS `prefers-color-scheme` (default for new users) | `"system"` |
| **Light** | Forces light mode regardless of OS | `"light"` |
| **Dark** | Forces dark mode regardless of OS | `"dark"` |

**Web — Implementation:**

Preference stored in `localStorage` key `boondi-theme`. A small inline `<script>` in `<head>` applies the theme class before React hydrates to prevent Flash Of Unstyled Content (FOUC):

```html
<!-- index.html — inside <head>, before any CSS -->
<script>
  (function() {
    var theme = localStorage.getItem('boondi-theme') || 'system';
    var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (theme === 'dark' || (theme === 'system' && prefersDark)) {
      document.documentElement.classList.add('dark');
    }
  })();
</script>
```

**React Theme Context:**
```tsx
type Theme = 'light' | 'dark' | 'system';

const ThemeContext = createContext<{ theme: Theme; setTheme: (t: Theme) => void }>({
    theme: 'system',
    setTheme: () => {}
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
    const [theme, setThemeState] = useState<Theme>(
        () => (localStorage.getItem('boondi-theme') as Theme) || 'system'
    );

    const setTheme = (t: Theme) => {
        setThemeState(t);
        localStorage.setItem('boondi-theme', t);
        const root = document.documentElement;
        const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        const applyDark = t === 'dark' || (t === 'system' && systemDark);
        root.classList.toggle('dark', applyDark);
    };

    return <ThemeContext.Provider value={{ theme, setTheme }}>{children}</ThemeContext.Provider>;
}
```

**Settings UI (Appearance section):**
```tsx
<SegmentedControl
    options={[
        { label: 'Light', value: 'light', icon: <Sun size={16} /> },
        { label: 'System', value: 'system', icon: <Monitor size={16} /> },
        { label: 'Dark', value: 'dark', icon: <Moon size={16} /> },
    ]}
    value={theme}
    onChange={setTheme}
/>
```

**Android — Implementation:**
```kotlin
// ThemePreference stored in DataStore<Preferences>
enum class AppTheme { LIGHT, DARK, SYSTEM }

// Apply on startup and preference change
fun applyTheme(theme: AppTheme) {
    val mode = when (theme) {
        AppTheme.LIGHT  -> AppCompatDelegate.MODE_NIGHT_NO
        AppTheme.DARK   -> AppCompatDelegate.MODE_NIGHT_YES
        AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

// In MainActivity.onCreate(), observe DataStore:
lifecycleScope.launch {
    settingsRepository.themeFlow.collect { theme ->
        applyTheme(theme)
    }
}
```

**Android Settings (Appearance):**
```kotlin
// SegmentedButton for theme selection
SingleChoiceSegmentedButtonRow {
    AppTheme.entries.forEachIndexed { index, appTheme ->
        SegmentedButton(
            selected = currentTheme == appTheme,
            onClick = { viewModel.setTheme(appTheme) },
            shape = SegmentedButtonDefaults.itemShape(index, AppTheme.entries.size),
            icon = { SegmentedButtonDefaults.ActiveIcon(selected = currentTheme == appTheme) }
        ) {
            Text(appTheme.name.lowercase().replaceFirstChar { it.uppercase() })
        }
    }
}
```

### 15.2 Color Token Mapping in Dark Mode

All color tokens are defined in two sets in `tokens.css` (see Section 6.3). The `dark` class on `<html>` switches the entire token set simultaneously. Key distinctions in dark mode:

**Surface hierarchy (dark):**
```
color-background:     #030712   ← True app background (deepest, behind everything)
color-surface:        #111827   ← Cards, feed background (slightly lighter)
color-surface-raised: #1F2937   ← Modals, dropdowns, bottom sheets (highest elevation)
```

This tonal layering creates depth without shadows, following Material 3's tonal surface system.

**Primary color in dark mode:** The primary brand color shifts from `#4F46E5` (light) to `#818CF8` (dark) — a lighter shade that achieves similar contrast ratios on the dark backgrounds.

**Ensure these pairs meet contrast requirements in dark mode:**
- `color-text-primary` (`#F9FAFB`) on `color-surface` (`#111827`): **16.0:1** ✓
- `color-primary` (`#818CF8`) on `color-surface` (`#111827`): **5.5:1** ✓
- `color-text-secondary` (`#9CA3AF`) on `color-surface` (`#111827`): **5.8:1** ✓

### 15.3 Image Handling in Dark Mode

- **User-uploaded images (posts, avatars, banners):** Never filtered, inverted, or dimmed. Display exactly as uploaded.
- **SVG illustrations (empty states, placeholders):** Must use `currentColor` for all strokes. Background fills in illustrations must not be hardcoded to white. Test each SVG in both modes.
- **Logo / wordmark:** Provide a `dark:` variant — white text on dark backgrounds. Use CSS class swap:
  ```tsx
  <img src="/logo-dark.svg" className="hidden dark:block" alt="Boondi" />
  <img src="/logo-light.svg" className="block dark:hidden" alt="Boondi" />
  ```
- **Transparent PNGs:** Set explicit `background-color: var(--color-surface)` on image containers to ensure transparent PNGs don't show through to the dark background unexpectedly.
- **Favicon:** Use `<link rel="icon" href="/favicon.svg">` with a single SVG favicon that uses `prefers-color-scheme` media query internally, or swap `href` dynamically via JS.

### 15.4 Android — Material 3 Dynamic Color

Material 3 on Android 12+ can generate a color scheme from the user's wallpaper using `DynamicColors`. For Boondi MVP:

**Decision: Fixed brand colors only (MVP)**
- Provides a consistent, recognizable brand experience
- Avoids accessibility risks from unpredictable wallpaper-derived colors
- Simplifies testing — only two color schemes to QA (light + dark)

**Do NOT call** `DynamicColors.applyToActivityIfAvailable(this)` in MVP.

**Future consideration:** Add a user toggle in Settings → Appearance: "Use dynamic colors (Android 12+)" — off by default.

```kotlin
// BoondiTheme.kt — explicitly using fixed color schemes
@Composable
fun BoondiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Note: Dynamic color is intentionally disabled in MVP
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BoondiTypography,
        shapes = BoondiShapes,
        content = content
    )
}
```

---

## 16. Empty States & Error States

All empty and error states follow the same structural pattern:

```
[Illustration SVG — 160×160px, centered]

[Headline — Heading 2, centered, color-text-primary]

[Subtext — Body Medium, centered, color-text-secondary, max-width 300px]

[CTA Button — Primary or Tertiary, centered, optional]
```

Container: `flex flex-col items-center justify-center gap-4 px-8 py-16 text-center`

---

### 16.1 Empty Feed (New User / No Follows)

**When shown:** User has zero follows, or their follows have posted nothing.

**Illustration:** Two overlapping circles (Venn diagram style) with small person silhouettes inside each, joined by a dotted line — representing community yet to form. Brand indigo tones.

**Headline:** "Your feed is quiet"

**Subtext:** "Follow people to see their posts here. Start with who's active in the community."

**CTA:** "Find people to follow" → Primary button → navigates to `/search?tab=users`

---

### 16.2 Empty Notifications

**When shown:** User has no notifications of any kind, or the active filter has no results.

**Illustration:** Outlined bell with small sparkles radiating outward — friendly, anticipatory rather than sad.

**Headline:** "No notifications yet"

**Subtext:** "When someone likes or replies to your posts, you'll see it here."

**CTA:** None. (No meaningful action the user can take to generate notifications themselves.)

---

### 16.3 Empty Search Results

**When shown:** A search query returns zero results across all tabs.

**Illustration:** Magnifying glass with a question mark inside the glass lens. Neutral, not discouraging.

**Headline:** `No results for "{query}"`

**Subtext:** "Try different keywords, or search for a hashtag like #topic"

**CTA:** "Clear search" — Tertiary button — clears query and returns to recent searches view

---

### 16.4 Empty Bookmarks

**When shown:** User has saved no posts yet.

**Illustration:** Bookmark ribbon shape with a small star. Warm amber tone (matching bookmark action color).

**Headline:** "Nothing saved yet"

**Subtext:** "Tap the bookmark icon on any post to save it here for later."

**CTA:** "Go to your feed" → Tertiary button → `/home`

---

### 16.5 No Internet / Offline State

**When shown:** Device has no network connectivity; detected via `navigator.onLine` (web) or `ConnectivityManager` (Android).

**Illustration:** Cloud shape with a disconnected power plug underneath. Neutral, factual.

**Headline:** "You're offline"

**Subtext:** "Check your connection and try again."

**CTA:** "Retry" — Primary button — triggers a network check and data reload

**Additional behavior:**
- Web: A persistent banner at the top of the feed (not a full-page overlay) when cached content is available: "You're offline — showing cached content"
- Android: `ConnectivityManager.NetworkCallback` detects reconnection → auto-retries in background → dismisses banner and refreshes silently
- Do not block the UI entirely if local cache exists; show stale content with an offline indicator

---

### 16.6 Server Error (500 / Unexpected Errors)

**When shown:** API returns 5xx status, or an unhandled exception occurs in data fetching.

**Illustration:** Abstract server/cloud shape with a caution triangle overlay. Dark red/orange accent on triangle; brand-toned cloud.

**Headline:** "Something went wrong"

**Subtext:** "Our servers hit a snag. We're already looking into it."

**CTA:** "Try again" — Primary button — retries the most recent failed request

**Technical handling:**
- Do not expose error codes, stack traces, or technical details in the user-facing message
- Log full error details to Sentry / Crashlytics silently
- If error persists after 2 retries: add "If this keeps happening, contact support" below the CTA

---

### 16.7 Not Found (404)

**When shown:** Route does not exist, post was deleted, user account does not exist, or content was removed.

**Illustration:** A simple crossroads sign with one of the signs missing (broken signpost). Neutral gray tones with brand accent on the remaining sign.

**Headline:** "This page doesn't exist"

**Subtext:** "The link might be broken, or this content was removed."

**CTA:** "Go to home feed" — Primary button → `/home`

**Also applies to:**
- `/post/{id}` where post was deleted: "This post was removed or doesn't exist."
- `/profile/{username}` where account was deleted: "This account no longer exists."

---

### 16.8 Account Suspended

**When shown:** User attempts to log in but account has `status: suspended`; or a currently-logged-in user's account gets suspended mid-session (detected on next API call returning 403).

**Illustration:** Padlock with the Boondi icon mark embedded in the shackle. Neutral dark tones; not aggressive.

**Headline:** "Your account has been suspended"

**Subtext:** "Your account was suspended for violating Boondi's community guidelines. Reach out to a community admin for more information."

**CTA:** Admin contact email shown as a `mailto:` link — not a button (prevents abuse loop). No self-service appeal in MVP.

**Session behavior:**
- Immediately invalidate the user's auth token
- Clear all local storage / DataStore
- Redirect to this screen; block re-login attempts while suspended
- API layer returns 403 with `code: "ACCOUNT_SUSPENDED"` — client handles this error code specifically to show this screen

---

## 17. Iconography

### 17.1 Icon Libraries

| Platform | Library | Style | Install |
|---|---|---|---|
| Web | **Lucide React** | Outline (default), Filled (active states where available) | `npm install lucide-react` |
| Android | **Material Icons Extended** | Rounded (consistent with Boondi's rounded design language) | `implementation("androidx.compose.material:material-icons-extended")` |

**Why these choices:**
- Lucide Icons: Open source, MIT license, consistent 24px grid, 2px stroke, highly legible, actively maintained
- Material Icons Rounded: Native to the Android/Compose ecosystem, perfectly consistent with Material 3 components, available on-device (no download)

**Web icon usage:**
```tsx
import { Heart, MessageCircle, Repeat2, Bookmark, Share2 } from 'lucide-react'

// Standard usage
<Heart size={20} strokeWidth={2} className="text-text-secondary" />

// Active / liked state
<Heart size={20} strokeWidth={2} fill="currentColor" className="text-like" />
```

**Android icon usage:**
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

Icon(
    imageVector = Icons.Rounded.Favorite,
    contentDescription = null, // described by parent semantics
    tint = if (isLiked) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.size(20.dp)
)
```

### 17.2 Icon Sizes

| Size | Value | Tailwind Class | Usage |
|---|---|---|---|
| Extra Small | 16px / 16dp | `size-4` | Inline in text, badge icons, caption-level contexts |
| Small | 20px / 20dp | `size-5` | Post action bar, form field leading/trailing icons, notification type indicators |
| Medium | 24px / 24dp | `size-6` | Navigation icons, toolbar icons, primary action icons |
| Large | 32px / 32dp | `size-8` | Empty state supporting icons, feature illustrations |

**Stroke width (Lucide web):** Always `strokeWidth={2}`. Never `1` (too thin on low DPI) or `2.5` (too heavy).

**Tint rules:**
- Default / inactive: `color-text-secondary` (`#4B5563` / `#9CA3AF` dark)
- Active / selected: semantic color for the action (`color-like`, `color-primary`, `color-repost`, `color-bookmark`)
- Disabled: `color-text-disabled`
- On primary background: `color-text-inverse` (white)

### 17.3 Icon Usage Table

| Action / Concept | Lucide (Web) | Material Rounded (Android) | Active Color | Notes |
|---|---|---|---|---|
| Home (nav) | `Home` | `Icons.Rounded.Home` | `color-primary` | Filled style when active (use `fill="currentColor"` web) |
| Search | `Search` | `Icons.Rounded.Search` | `color-primary` | — |
| Notifications (bell) | `Bell` | `Icons.Rounded.Notifications` | `color-primary` | Badge variant when unread; Filled when active |
| Bookmarks (nav) | `Bookmark` | `Icons.Rounded.Bookmarks` | `color-primary` | — |
| Like / Heart | `Heart` | `Icons.Rounded.FavoriteBorder` / `Icons.Rounded.Favorite` | `color-like` (#F43F5E) | Filled when liked |
| Reply / Comment | `MessageCircle` | `Icons.Rounded.ChatBubbleOutline` | `color-primary` | — |
| Repost | `Repeat2` | `Icons.Rounded.Repeat` | `color-repost` (#10B981) | Filled when reposted |
| Bookmark (action) | `Bookmark` | `Icons.Rounded.BookmarkBorder` / `Icons.Rounded.Bookmark` | `color-bookmark` (#F59E0B) | Filled when saved |
| Share / Upload | `Share2` | `Icons.Rounded.Share` | none | No active state |
| Compose / Write | `PenSquare` | `Icons.Rounded.Edit` | — | Used in FAB |
| Edit post | `Pencil` | `Icons.Rounded.Edit` | — | In post context menu |
| Delete / Trash | `Trash2` | `Icons.Rounded.Delete` | `color-error` | Always red; destructive |
| Settings / Gear | `Settings` | `Icons.Rounded.Settings` | `color-primary` | — |
| Add / Plus | `Plus` | `Icons.Rounded.Add` | — | FAB, compose image add |
| Close / Dismiss | `X` | `Icons.Rounded.Close` | — | Modal close, clear search, remove image |
| Confirm / Done | `Check` | `Icons.Rounded.Check` | `color-success` | Success state, confirm action |
| Back / Arrow Left | `ArrowLeft` | `Icons.Rounded.ArrowBack` | — | Navigation back in app bar |
| More Options (···) | `MoreHorizontal` | `Icons.Rounded.MoreHoriz` | — | Post context menu trigger |
| Profile / User | `User` | `Icons.Rounded.Person` | `color-primary` | Nav, fallback avatar |
| Image / Photo | `Image` | `Icons.Rounded.Image` | — | Compose attach image |
| Link / URL | `Link` | `Icons.Rounded.Link` | — | Share link, bio URL |
| Pin | `Pin` | `Icons.Rounded.PushPin` | `color-primary` | Pinned post indicator |
| Report / Flag | `Flag` | `Icons.Rounded.Flag` | `color-error` | Report content; shown red |
| Mention / At | `AtSign` | `Icons.Rounded.AlternateEmail` | `color-primary` | Mention in notification |
| Trending | `TrendingUp` | `Icons.Rounded.TrendingUp` | `color-primary` | Trending tab, trending section |
| Log Out | `LogOut` | `Icons.Rounded.Logout` | `color-error` | Settings; destructive context |
| Follow (add user) | `UserPlus` | `Icons.Rounded.PersonAdd` | `color-primary` | Notification type icon |
| Verified badge | `BadgeCheck` | `Icons.Rounded.Verified` | `color-primary` | On verified usernames |
| Calendar / Date | `Calendar` | `Icons.Rounded.CalendarToday` | — | Profile join date |
| Eye (show) | `Eye` | `Icons.Rounded.Visibility` | — | Show password in input |
| Eye Off (hide) | `EyeOff` | `Icons.Rounded.VisibilityOff` | — | Hide password in input |
| Warning / Alert | `AlertTriangle` | `Icons.Rounded.Warning` | `color-warning` | Warning banners |
| Info | `Info` | `Icons.Rounded.Info` | `color-info` | Info toasts, tooltips |
| Error circle | `XCircle` | `Icons.Rounded.Error` | `color-error` | Error toasts, validation |
| Success circle | `CheckCircle` | `Icons.Rounded.CheckCircle` | `color-success` | Success toasts |
| Send (reply) | `Send` | `Icons.Rounded.Send` | `color-primary` | Reply submit button (icon-only on mobile) |
| Quote post | `Quote` | `Icons.Rounded.FormatQuote` | `color-primary` | Quote repost action |

### 17.4 Custom Icon Guidelines

If a custom icon is needed beyond the standard libraries (e.g., the Boondi brand icon mark):

**Design rules:**
- Design on a **24×24px** artboard with a 1px minimum clear space from the artboard edge
- Stroke weight: **2px** (matching Lucide) for outline icons; solid fill for filled variants
- Corner radius: **2px** on sharp path corners to soften (matching Lucide's style)
- Use `currentColor` for ALL fill and stroke attributes — never hardcode hex values in the SVG
- Keep path count minimal — SVG icons should be under 5 paths for clarity at small sizes

**Web export:**
1. Export SVG from Figma: "Include 'id' attribute" = OFF; "Simplify stroke" = ON; "Outline text" = ON
2. Run through SVGO: `npx svgo --config svgo.config.js icon.svg -o icon.min.svg`
3. Add `aria-hidden="true"` and `focusable="false"` to SVG element (handled by Lucide automatically)
4. Store in `/src/assets/icons/` directory
5. Create a React wrapper component:
   ```tsx
   export function BoondiIcon({ size = 24, className }: { size?: number; className?: string }) {
       return (
           <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
               aria-hidden="true" focusable="false" className={className}>
               {/* paths */}
           </svg>
       )
   }
   ```

**Android export:**
1. Export SVG from Figma
2. In Android Studio: `Resource Manager → + → Vector Asset → Local file (SVG)`
3. Android Studio auto-converts to `VectorDrawable` XML — review output for correctness
4. Naming: `ic_{name}.xml` (e.g., `ic_boondi.xml`, `ic_boondi_logo.xml`)
5. Reference in Compose:
   ```kotlin
   val boondiIcon = ImageVector.vectorResource(R.drawable.ic_boondi)
   Icon(imageVector = boondiIcon, contentDescription = "Boondi")
   ```

---

## 18. Design Handoff Notes

### 18.1 Figma File Structure

Organize all Boondi design assets in a **single Figma file** with the following page structure:

```
📁 Boondi — Design System & UI
│
├── 📄 00 · Cover
│       Project title, last updated date, version, owner contact
│
├── 📄 01 · Design Tokens
│       Color styles (all semantic tokens, light + dark)
│       Text styles (all 13 type styles)
│       Effect styles (shadows, blur)
│       Grid styles (1-col, 2-col, 3-col)
│
├── 📄 02 · Icons
│       All icons at 16, 20, 24, 32px
│       Organized in a labeled grid
│       Both outline (default) and fill (active) variants
│
├── 📄 03 · Components
│       Atoms:     Button, Avatar, Badge, Divider, Chip, Skeleton, Toast
│       Molecules: ActionBar, UserRow, NotificationIcon, SearchInputRow
│       Organisms: PostCard, UserCard, NavigationSidebar, ComposeBox, NotificationItem, BottomNav
│
├── 📄 04 · Screens — Web
│       Login, Register, Home Feed (desktop + mobile), Post Composer
│       Post Detail, User Profile, Followers/Following
│       Notifications, Search, Settings, Admin Panel
│       Each screen shown at Desktop (1440px) and Mobile (375px)
│
├── 📄 05 · Screens — Android
│       All 10 Android screens at Pixel 7 (412×917px)
│       Light and Dark variants for each screen
│
├── 📄 06 · Prototype — Web
│       Connected frames showing primary user flows:
│       - Login → Home → Compose → Post
│       - Home → Profile → Follow
│       - Home → Post Detail → Reply
│
├── 📄 07 · Prototype — Android
│       Connected frames for Android flows
│       Matching the web prototype flows
│
└── 📄 08 · Archive
        Previous explorations, deprecated components, rejected directions
```

### 18.2 Component Naming in Figma

Follow this **consistent naming convention** for all Figma components to enable clean Dev Mode inspection:

**Pattern:**
```
[Category]/[ComponentName], [Variant]=[Value], [State]=[Value], [Size]=[Value]
```

**Examples:**
```
Button/Primary, Size=Large, State=Default
Button/Primary, Size=Large, State=Hover
Button/Primary, Size=Large, State=Loading
Button/Icon, Size=Medium, State=Default
PostCard/Default, State=Default
PostCard/Default, State=Liked
PostCard/Reply, State=Default
Avatar/Circle, Size=Medium, Indicator=None
Avatar/Circle, Size=Medium, Indicator=Online
Input/Text, State=Default
Input/Text, State=Focused
Input/Text, State=Error
Input/Textarea, State=Default
Navigation/Sidebar/Item, State=Default
Navigation/Sidebar/Item, State=Active
Notification/Item, Type=Like, State=Unread
Notification/Item, Type=Follow, State=Read
```

**Frame naming for screen designs:**
```
[Platform] / [Screen Name] / [Breakpoint or Device] / [Theme]

Web / Home Feed / Desktop 1440 / Light
Web / Home Feed / Mobile 375 / Light
Web / Home Feed / Desktop 1440 / Dark
Android / Home Feed / Pixel 7 / Light
Android / Home Feed / Pixel 7 / Dark
```

### 18.3 Design Token Export

**Recommended token workflow:**

1. Define all tokens in **Tokens Studio** Figma plugin (JSON structure matching Section 6)
2. Commit `tokens/*.json` files to the repository
3. Run **Style Dictionary** build to output platform-specific token files
4. Import generated files into web (CSS variables) and Android (Kotlin/XML) codebases

**Token JSON structure:**
```json
{
  "color": {
    "primary": { "value": "#4F46E5", "type": "color", "description": "Brand primary — indigo-600" },
    "primary-hover": { "value": "#4338CA", "type": "color" },
    "background": { "value": "#F9FAFB", "type": "color" },
    "surface": { "value": "#FFFFFF", "type": "color" },
    "text-primary": { "value": "#111827", "type": "color" },
    "like": { "value": "#F43F5E", "type": "color" }
  },
  "spacing": {
    "1": { "value": "4px", "type": "spacing" },
    "2": { "value": "8px", "type": "spacing" },
    "4": { "value": "16px", "type": "spacing" },
    "6": { "value": "24px", "type": "spacing" }
  },
  "borderRadius": {
    "sm": { "value": "4px", "type": "borderRadius" },
    "md": { "value": "8px", "type": "borderRadius" },
    "lg": { "value": "12px", "type": "borderRadius" },
    "xl": { "value": "16px", "type": "borderRadius" },
    "full": { "value": "9999px", "type": "borderRadius" }
  }
}
```

**Style Dictionary config:**
```js
// style-dictionary.config.js
module.exports = {
    source: ['tokens/global.json', 'tokens/semantic.json'],
    platforms: {
        css: {
            transformGroup: 'css',
            prefix: 'boondi',
            buildPath: 'src/styles/',
            files: [{
                destination: 'tokens.css',
                format: 'css/variables',
                options: { outputReferences: true }
            }]
        },
        android: {
            transformGroup: 'android',
            buildPath: 'android/app/src/main/res/values/',
            files: [{
                destination: 'tokens.xml',
                format: 'android/resources'
            }]
        }
    }
};
```

### 18.4 Developer Handoff Checklist

Before a design spec is marked **"Dev Ready"**, the designer must verify:

**Visual completeness:**
- [ ] All interactive component states documented: Default, Hover, Active, Focused, Disabled, Loading, Error, Success
- [ ] Dark mode variants provided for every screen
- [ ] Empty states provided for every list/feed screen
- [ ] Error states provided for every form (field-level + form-level)
- [ ] Skeleton loader states provided for every async content area
- [ ] All responsive breakpoints shown: xs/sm (375px), md (768px), lg (1024px), xl (1280px)
- [ ] Animation / transition behavior noted in component comments or annotation overlays

**Specification completeness:**
- [ ] All spacing values reference design token names (e.g., `space-4` not `16px`)
- [ ] All colors reference semantic token names (e.g., `color-primary` not `#4F46E5`)
- [ ] All text uses named Figma text styles that match the type scale
- [ ] Interactive elements have all state variations in Figma component set
- [ ] Exact content for empty states / error messages confirmed with Product

**Accessibility:**
- [ ] Every icon-only button has a documented accessible label
- [ ] Color contrast verified for all text/icon combinations using a contrast checker plugin
- [ ] All touch targets are ≥ 44px (web) / 48dp (Android) — annotated on Figma if not visually obvious
- [ ] Keyboard focus flow documented for all forms and modals
- [ ] Screen reader traversal order noted for complex components (notification item, post card action bar)

**Assets ready:**
- [ ] All exportable icons marked for export in Figma (export settings applied)
- [ ] Illustration SVGs finalized, using `currentColor`, tested in both light and dark
- [ ] App icon provided at all required sizes
- [ ] OG image (1200×630px) provided for link preview

### 18.5 Asset Export Settings

**Web — SVG Icons:**
| Setting | Value |
|---|---|
| Format | SVG |
| Include "id" attributes | OFF |
| Outline text | ON |
| Simplify stroke | ON |
| Post-processing | SVGO — remove comments, merge paths, remove `<title>` |
| Storage location | `src/assets/icons/{icon-name}.svg` |

**Web — Raster Images (illustrations):**
| Setting | Value |
|---|---|
| Format | PNG (Figma export) → WebP (build-time conversion) |
| Scale | 1× and 2× (for Retina displays) |
| Naming | `{name}.png`, `{name}@2x.png` |
| Conversion | `npx sharp-cli --input *.png --output *.webp` or Vite plugin |

**Android — Vector Icons:**
| Step | Action |
|---|---|
| 1. Export from Figma | SVG format, same settings as web SVG |
| 2. Import to Android Studio | `Resource Manager → + → Vector Asset → Local file (SVG)` |
| 3. Output | `res/drawable/ic_{name}.xml` (VectorDrawable format) |
| 4. Naming convention | `ic_{action_or_concept}.xml` — all lowercase, underscores |

**Android — Raster Images:**
| Density | Scale | Directory |
|---|---|---|
| mdpi | 1× | `res/drawable-mdpi/` |
| hdpi | 1.5× | `res/drawable-hdpi/` |
| xhdpi | 2× | `res/drawable-xhdpi/` |
| xxhdpi | 3× | `res/drawable-xxhdpi/` |
| xxxhdpi | 4× | `res/drawable-xxxhdpi/` |

Use PNG for images requiring transparency; WebP for all others (supported from Android 4.2+, target min is higher anyway).

**Android App Icon:**
| Density | Size | File |
|---|---|---|
| mdpi | 48×48px | `mipmap-mdpi/ic_launcher.png` |
| hdpi | 72×72px | `mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96×96px | `mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144×144px | `mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192×192px | `mipmap-xxxhdpi/ic_launcher.png` |
| Adaptive foreground | 108×108dp canvas, icon in 66×66dp safe zone | `mipmap-anydpi-v26/ic_launcher_foreground.xml` |
| Adaptive background | Solid `#4F46E5` (brand color) | `mipmap-anydpi-v26/ic_launcher_background.xml` |

**Web Favicon package:**
| File | Size | Purpose |
|---|---|---|
| `favicon.ico` | 16×16 + 32×32 (multi-size ICO) | Legacy browsers |
| `favicon.svg` | Scalable | Modern browsers (preferred) |
| `apple-touch-icon.png` | 180×180px | iOS home screen |
| `icon-192.png` | 192×192px | Android PWA |
| `icon-512.png` | 512×512px | Android PWA splash |
| `og-image.png` | 1200×630px | Open Graph link previews |

---

*End of UI/UX Design Specification — Boondi v1.0*

*Maintained by the Boondi design and product team.*
*For questions or amendments, open a discussion in the project repository or contact the lead designer.*
