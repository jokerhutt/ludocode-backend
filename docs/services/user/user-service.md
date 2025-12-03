<h1 align="center">The User Service</h1>

## Overview

The User Service manages core user identity, onboarding preferences, and timezone resolution.
It is the entry point for user creation during authentication and is responsible for returning
user-facing profile data.

It also bridges authentication and progress by returning onboarding selections and setting the user's
initial course progress after the onboarding step. (For course progress, see the [Course Progress Service](../progress/course-progress-service.md))

---

## Responsibilities

- Create users on first login based on OAuth provider identity
- Map external provider accounts (e.g. Google, Demo) to internal users
- Return user profiles by ID
- Store onboarding preferences and chosen learning path
- Provide timezone information for streak calculations

---

## Boundaries & Non-Responsibilities

The User Service does **not**:

- handle authentication tokens (Auth Service owns JWT)
- store progress, coins, or streak values
- manage lesson or catalog content

It hands off to other services once identity and onboarding are resolved.

---

## Data Models

### Entity: `User`
```
User
    id: UUID
    firstName: String
    lastName: String
    pfpSrc: String?
    email: String
    createdAt: OffsetDateTime
    timeZone: String      // defaults to UTC
```

### Entity: `ExternalAccount`
```
ExternalAccount
    id: UUID
    userId: UUID          // FK to User
    provider: AuthProvider
    providerUserId: String  // unique per provider
    createdAt: Instant
```

### Entity: `UserPreferences`
```
UserPreferences
    userId: UUID          // PK and FK to User
    hasExperience: Boolean
    chosenPath: DesiredPath
```

### DTOs
```
UserResponse
    id
    firstName
    lastName
    pfpSrc
    email
    createdAt

FindOrCreateUserRequest
    provider
    providerUserId
    firstName
    lastName
    email
    name
    avatarUrl

OnboardingSubmission
    chosenPath
    chosenCourse
    hasProgrammingExperience

OnboardingResponse
    preferences: UserPreferences
    courseProgressResponse: CourseProgressResponseWithEnrolled
```

---

## Core Operations

- `findOrCreate(req)`  
  Creates the user if they do not exist for the given OAuth provider, then returns profile data.

- `getById(id)`  
  Returns a mapped `UserResponse`.

- `getUserTimezone(userId)`  
  Used by streak logic to convert UTC timestamps to local dates.

- `createPreferences(submission, userId)`  
  Saves onboarding preferences and initializes course progress.

- `getPreferences(userId)`  
  Returns stored onboarding selections.

- `getUsersByIds(userIds)`  
  Batched profile fetch for feeds or leaderboards.

---

## Public API

| Method | Path                       | Returns              | Purpose                          |
|------: |----------------------------|----------------------|----------------------------------|
|   GET  | `/users/ids`               | `List<UserResponse>` | Fetch multiple users by ID       |
|   GET  | `/users/preferences`       | `OnboardingResponse` | Retrieve onboarding preferences  |
|   POST | `/users/onboarding/submit` | `UserPreferences`    | Save onboarding preferences      |

---

## Workflows

### First Login
```
AuthService → findOrCreate(req)
→ create User if missing
→ create ExternalAccount link
→ return UserResponse
```

### Onboarding Submit
```
Client → submit chosen path + course
→ createPreferences(...)
→ store preferences
→ initialize course progress via CourseProgress Service
→ return OnboardingResponse
```

### Progress and Streak Interop
```
getUserTimezone(userId)
→ Streak Service converts timestamps for daily goal detection
```

---

## Integration Points

**Inbound**
- Auth Service → `findOrCreate` on login
- Lesson/Streak Services → `getUserTimezone`

**Outbound**
- CourseProgress Service → invoked during onboarding to enroll the user

---

## Future Work / Known Gaps

- Support for changing the user's timezone
- Preference changes after onboarding