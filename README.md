# Ludocode Backend

## Table of Contents
1. [Overview](#overview)
2. [Project Setup](#running-the-project)
3. [Microservice Structure](#microservice-structure)
4. [Microservices](#microservices)

## Overview
This repository contains the backend code for Ludocode, a code learning website intended primarily as a showcase project for Mimo.

The project is written using Kotlin 1.9.25 and Java 21. It uses PostgreSQL as a database.

## Features
- Spring Authentication
- Ports and adapters style architecture
- Timezone based streak system
- Ability to create and modify coding projects with files
- Diffing system for saving files
- Code execution using Piston API
- Diffing and versioning system for modifying courses and their contents
- Coins system for users
- Attempts are stored in an analytics friendly way
- Deduplication with request hashes

## Planned Features
- Analytics System
- AI chatbot assistant
- Backend caching

## Requirements
- Kotlin 1.9+
- Java 21+
- Docker
- Google Cloud Project **(Optional - Only for auth and code projects)**

## Project Setup

1. Clone the project
```
git@github.com:jokerhutt/ludocode-backend.git
```
2. Navigate to project directory
```
cd /path/to/ludocode-backend
```
3. Run the Application
```
./mvnw spring-boot:run
```
4. Set up a PostgreSQL container and fill in `DB_URL`, `DB_USER`, and `DB_PASSWORD` with their respective values in your environment variables

### Setting up Google OAuth (Optional)
**This only affects authentication. Leaving the `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` blank will override authentication checks.**
1. Create a Google Cloud account
2. Create a Google Cloud project
3. Go to Google Cloud Console → APIs & Services → Credentials
4. Create OAuth 2.0 Client (type: Web application)
Required fields:
- Authorized JavaScript origins:
  https://your-frontend-domain.com
- Authorized redirect URIs:
  http://your-frontend-domain.com/auth/google/callback
5. Set `GOOGLE_CLIENT_ID` to your Google client ID and `GOOGLE_CLIENT_SECRET` to your Google client secret.
6. Generate a JWT secret and set `JWT_SECRET` to its value in your environment variables

---
### Setting up GCS (Optional)
**This only affects the playground service, as files are stored in the bucket.**
1. Create a Google Cloud account
2. Create a Google Cloud project
3. Create a Cloud storage bucket
4. Set `GCS_BUCKET` to your GCS bucket name and `GCS_PROJECT_ID` to your GCS project ID in your environment variables.

---
### Setting up Piston API (Optional)
**Note: It's best to have a linux environment for this, the MacOS ARM versions have trouble with dependencies. I have not tested it on Windows or Intel MacOS**

1. Start the Piston Docker container (see: https://github.com/engineer-man/piston).
2. Set `PISTON_BASE` to `http://{PISTON_IP}:{PISTON_PORT}/api/v2` in your environment variables.

---
### Setting up Tests
- The test suite uses **Test containers**. Ensure Docker is installed and running before executing tests. (Ensure Docker Desktop on MacOS / Windows)
- All containers (Postgres, etc.) will be started automatically during the test run.
- The tests use a PostgreSQL schema found in `/test/kotlin/resources/schema.sql`.

---
## Microservice Structure
```
api/
  controllers/
    internal/    # endpoints for internal services
    external/    # endpoints exposed to frontend
  dto/           # request/response DTOs

app/
  mapper/        # entity -> DTO mapping
  port/
    in/          # interfaces for other services to call
    out/         # interfaces for calling other services
  service/       # application business logic

domain/
  entity/        # core repository entities
    embeddable/  # embeddable composite keys for entities
  enums/         # domain related enums

infra/
  projection/    # repository projections
  repository/    # jpa repositories for domain entities
  http/          # HTTP clients for calling external APIs (google, piston, etc.)
  client/        # HTTP clients for calling other internal services
```

---
## Microservices

### Auth
Handles user authentication, Google OAuth onboarding, and issuing JWT tokens + cookies.

### User
Manages user creation, retrieval, updates, and user preference data.

### Catalog
Stores and manages all static course content: courses, modules, lessons, exercises, and exercise options.

### GCS
Handles all interactions with Google Cloud Storage: uploading, fetching, and managing stored files.

### Playground
Manages user code projects and their files, saves snapshots, and executes code submissions via the Piston runtime client.

### Progress
Tracks all progress-related data:

- User coins
- User streaks
- User lesson completions
- Per-course progress