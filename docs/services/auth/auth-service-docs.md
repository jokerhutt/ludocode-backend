<h1 align="center">The Auth Service</h1>

## Overview

The Auth Service handles user authentication through Google OAuth, Demo login, and JWT cookie
issuance. It delegates user creation to the User Service, then attaches coins and streak state
before returning a login response.

It exposes a minimal API surface—authentication happens once, state is stored in cookies, and
subsequent requests rely on the JWT contained in the cookie.

---

## Responsibilities

- Exchange OAuth codes with Google
- Identify or create users through the User Service
- Issue JWT cookies for authenticated sessions
- Return user profile, coin state, and streak state on login
- Extract `userId` from valid JWTs for downstream services

---

## Boundaries & Non-Responsibilities

The Auth Service does **not**:

- store user accounts itself ([User Service](../user/user-service.md) owns persistence)
- compute coins or streak changes
- run OAuth UI flows or redirect management
- manage catalog or course data

It only authenticates the user, gives them an identity, and places a signed token in a cookie.

---

## Data Models

```
AuthUser
    userId: UUID                 // injected into Spring Security context

UserLoginResponse
    user: UserResponse
    userStats: UserCoinsResponse
    userStreak: UserStreakResponse

GoogleTokenResponse
    access_token
    refresh_token
    id_token
    ...
```

The `id_token` is parsed for identity claims and passed to the User Service.

---

## Core Operations

- `loginWithGoogle(code, res)`  
  Exchange Google auth code → parse claims → find/create user → issue JWT → return login payload

- `loginWithDemo(res)`  
  Same as above, but uses a static demo account

- `getAuthenticatedUser(id)`  
  Return the profile for the currently authenticated user

JWT extraction happens at the filter layer:

```
JwtCookieAuthenticationFilter
→ read JWT cookie
→ validate token
→ extract userId
→ populate Spring Security context with AuthUser(userId)
```

---

## Public API

| Method | Path                 | Returns             | Purpose                                  |
|------: |----------------------|---------------------|------------------------------------------|
|   GET  | `/auth/me`           | `UserResponse`      | Return authenticated user                |
|   GET  | `/auth/login/google` | `UserLoginResponse` | Google OAuth login                       |
|   GET  | `/auth/login/demo`   | `UserLoginResponse` | Demo login without external provider     |

A successful login returns:

```
{
  user: UserResponse,
  userStats: UserCoinsResponse,
  userStreak: UserStreakResponse
}
```

plus a JWT cookie.

---

## JWT Cookie Behavior

JWTs are set as HTTP-only cookies:

```
setJwt(response, jwt)
→ cookie name, lifetime, secure flag, path, SameSite mode from config
```

Cookies are cleared with:

```
clearJwt(response)
```

JWT parsing:

```
requireUserId(token) → throws if token missing, invalid, or expired
```


---

## Workflows

### Google Login
```
Frontend → /auth/login/google?code=xyz
→ exchange code for tokens
→ parse id_token claims
→ findOrCreate user
→ initialize coins & streak
→ set JWT cookie
→ return UserLoginResponse
```

### Demo Login
```
/auth/login/demo
→ skip OAuth
→ find/create demo user
→ set JWT cookie
→ return UserLoginResponse
```

### Authenticated Request
```
Request arrives with JWT cookie
→ JwtCookieAuthenticationFilter reads token
→ create AuthUser(userId)
→ SecurityContext populated
→ downstream services know who the caller is
```

---

## Integration Points

**Inbound**
- Frontend login flows

**Outbound**
- **User Service** → `findOrCreate`
- **Coins Service** → `findOrCreateCoins`
- **Streak Service** → `getStreak`

---

## Future Work / Known Gaps

- Logout endpoint that clears the cookie
- Multiple provider logins per same user