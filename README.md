# Ludocode Backend

## Table of Contents
1. [Overview](#overview)
2. [Project Setup](#project-setup)
3. [Tests Setup](#setting-up-tests)
4. [Documentation](#)
3. [Services Structure](#directory-structure)
4. [Services](#services)

## Overview

This repository contains the backend for **Ludocode**, a modular code-learning platform.

The application is built with **Kotlin 1.9.25** (running on **Java 21**) and uses **PostgreSQL** as its primary datastore.

Comprehensive, feature-level documentation is available in the [documentation](docs/index.md).

Docker Compose configurations are included to run the core system locally without external credentials. Optional integrations — such as AI features and OAuth providers — are fully feature-toggled and degrade gracefully when disabled.

---

## Features

### Learning & Content Management
- Course, module, lesson, and exercise management
- Versioned course authoring with diff-based change tracking
- Dynamic language and subject configuration
- Lesson submission and completion workflows

### User Progress & Engagement
- Timezone-aware streak tracking
- Virtual coin reward system
- Per-course and per-lesson progress tracking
- User onboarding and preference management

### Authentication & Access
- Firebase-based authentication (optional)
- Demo user authentication mode
- JWT-based session management

### AI & Code Execution
- AI chatbot integration with server-side streaming (SSR)
- Credit-based AI usage limits (optional)
- Code execution via Piston runtime
- Project creation and modification with file snapshot diffing

### Storage & Infrastructure
- Pluggable blob storage (Local, GCS, S3)
- Redis-based caching (optional)
- Subscription plans with Stripe integration (optional)

### Quality & Testing
- Integration test suite with 100+ tests

---

## Requirements

- **Docker**

### Feature-Specific Configuration

The application runs without external credentials. If not provided, the corresponding features are disabled or fall back to development-safe defaults.

- **AI Features**  
  Requires a Gemini API key.

- **Authentication (OAuth)**  
  Requires a Firebase service account JSON.  
  If omitted, authentication falls back to demo mode.

- **Code Execution**  
  Requires a self-hosted Piston runtime.  
  The previously available public API is no longer maintained.

- **Stripe Payments**  
  Requires a Stripe secret key and webhook secret.  
  If disabled, the application operates in development plan mode.

---

### Optional Storage Providers

These integrations are optional. If not configured, local storage is used by default.

- **AWS S3**  
  Requires region, bucket name, access key ID, and secret access key.

- **Google Cloud Storage (GCS)**  
  Requires bucket name and project ID.

---

## Project Setup

### Simple Setup

If you are only interested in running the project locally, I have provided a setup that creates a local PostgreSQL instance with the schema & pre-seeded with a demo user / course. With this, you'll have access to all features except AI and Google authentication.

1. Clone the project
```
git clone git@github.com:jokerhutt/ludocode-backend.git
```
2. Navigate to project directory
```
cd ludocode-backend
```
3. Create the env file from the example env
```
cp .env.example .env
```

4. Run the PostgreSQL Container
```
 docker compose -f docker-compose.db.yml up -d
```
5. Run the Application Container (Might take ~60sec)
```
docker compose -f docker-compose.ludocode.yml build ludocode-backend
docker compose -f docker-compose.ludocode.yml up -d ludocode-backend
```

After you have your Postgres & Application containers running, you will have a pre-seeded demo user & lessons. On the frontend, simply visit `your-frontend-url/demo` to bypass the google authentication stage.

For example, `http://localhost:5173/demo`

### Enabling AI features (Optional)
To enable AI features, you need a Gemini API Key & enable the feature in the environment variables.

1. Go to https://aistudio.google.com/ -> Sign in with Google -> Get API Key.
2. Set `AI_ENABLED=true` in your environment variables.
3. Set `GEMINI_API_KEY` to your Gemini API Key

## Setting up Firebase (Optional)

**This only affects authentication.**  
If `FIREBASE_SERVICE_ACCOUNT_JSON` is not provided, Firebase login will be disabled and you must use the demo account.

---

### 1. Create a Firebase Project

1. Go to https://console.firebase.google.com
2. Click **Create Project**
3. Navigate to **Authentication → Sign-in method**
4. Enable **Google** (or any provider you plan to use)

---

### 2. Generate a Service Account Key (Backend)

1. Go to **Project Settings → Service Accounts**
2. Click **Generate new private key**
3. Download the JSON file

---

### 3. Set Environment Variable

Set the following environment variable:

```bash
FIREBASE_SERVICE_ACCOUNT_JSON={...full service account JSON...}
```

The value must contain the entire contents of the downloaded service account file.

If using Docker or `.env`, ensure it is either:

• A properly escaped single-line JSON string  
or  
• Injected as a multiline environment variable (recommended for Docker secrets)

If omitted or invalid, Firebase authentication will be disabled.


### Setting the environment variables (Optional)

**YOU CAN LEAVE ALL OF THESE AS-IS. ONLY CHANGE THESE IF YOU HAVE DIFFERENT PORTS / WANT A SPECIFIC FEATURE**

Below is a list of all possible environment variables. Unless you want some specific customization, the only things worth changing here are `AI_ENABLED`, `GEMINI_API_KEY`, `GOOGLE_CLIENT_ID`, and `GOOGLE_CLIENT_SECRET`. You can might want to change the frontend origin, the port, the profile, or the database port (if needed).

The remaining ones you should only change if you know what you are doing. E.g. if for whatever reason you want to change the demo user id, make sure you also change the `db/init.sql` insert for that user.

```
# === === CHANGE IF NEEDED === ===

# === ACTIVE PROFILE === 
SPRING_PROFILES_ACTIVE=admin

# === FRONTEND ORIGIN ===
# Change this to your frontend url if needed
FRONTEND_ORIGINS=http://localhost:5173

# === SERVER PORT ===
# Change this if your server isn't running on 8080
SERVER_PORTS=8080:8080

# === Database ===
# Leave as is unless your local postgres is on a different port or you are using your own external DB
DB_URL=jdbc:postgresql://postgres:5432/ludocode
DB_NAME=ludocode
DB_USER=ludo
DB_PASSWORD=password

# === GOOGLE AUTH ===
# Fill this in for Google OAUTH, leave as-is if you want to use demo users
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# === GEMINI ===
# Set this to true and fill out GEMINI_API_KEY if you want AI features
AI_ENABLED=false
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.5-flash-lite


# === === ONLY CHANGE BELOW IF YOU KNOW WHAT YOU ARE DOING === ===

# === COOKIE ===
# Leave this as is unless you want extra security
COOKIE_DOMAIN=localhost
COOKIE_SAME_SITE=Lax
COOKIE_SECURE=false

# === DEMO ===
# Leave this untouched if want demo users
# If you change the demo user ID, make sure to change it in /db/init.sql as well
DEMO_ENABLED=true
DEMO_TOKEN=demo-user-token
DEMO_USER_ID=598ccbea-4957-4569-81cb-ea901b62c329

# === JWT ===
# You dont need to change this
JWT_SECRET=9d5df235a21c67fb4238d23abd61d9a8

# === Local Storage ===
# Don't change this unless you know what you're doing or have set up GCS
LOCAL_BUCKET_NAME=/data/local-bucket

# === GCS ===
# Leave this false unless you want to use GCS. If you set GCS_ENABLED=true the local storage option won't work.
GCS_ENABLED=false
GCS_BUCKET_NAME=your-gcs-bucket-name
GCS_PROJECT_ID=your-gcs-project-id

# === PISTON ===
# Leave this as is for code execution, it will contact the public piston API
PISTON_ENABLED=true
PISTON_BASE=https://emkc.org/api/v2/piston

```
---
### Setting up Tests
- The test suite uses **Test containers**. Ensure Docker is installed and running before executing tests. (Ensure Docker Desktop on MacOS / Windows)
- All containers (Postgres, etc.) will be started automatically during the test run.
- The tests use a PostgreSQL schema found in `/test/kotlin/resources/schema.sql`.

---

### OpenAPI Documentation
The documentation is generated by spring and swagger. To access it, start the spring application and visit:
- `http://localhost:8080/swagger-ui/index.html` for the web UI documentation.
- `http://localhost:8080/v3/api-docs` for a JSON representation of the docs.
- `http://localhost:8080/v3/api-docs.yaml` for a YAML representation of the docs.

## Directory Structure
```
api/
  controller/    # HTTP controllers
  filters/       # Authenticate requests, check feature flags
  dto/           # request/response DTOs
  security/      # Security related principals & filters

app/
  mapper/        # entity -> DTO mapping
  port/
    in/          # interfaces for other internal services to call
    out/         # interfaces for calling external services
  service/       # application business logic

configuration/   # configuration beans

domain/
  entity/        # core repository entities
    embeddable/  # embeddable composite keys for entities
  enums/         # domain related enums

infra/
  projection/    # repository projections
  repository/    # jpa repositories for domain entities
  http/          # HTTP clients for calling external APIs (google, piston, etc.)
```

---
## Services

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