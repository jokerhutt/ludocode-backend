# Ludocode Backend

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Project Setup](#project-setup)
  1. [Enabling AI features (optional)](#enabling-ai-features-optional) 
  2. [Setting up firebase (optional)](#setting-up-firebase-optional)
4. [Tests Setup](#setting-up-tests)
5. [OpenAPI Documentation](#openapi-documentation)
6. [Directory Structure](#directory-structure)
7. [Services](#services)

## Overview

This repository contains the backend for **Ludocode**, a gamified code learning platform where users can complete lessons and create code projects. By running the project yourself, as an admin you are able to create & edit courses.

The application is built with **Kotlin 1.9.25** (running on **Java 21**) and uses **PostgreSQL** as its primary datastore.

Comprehensive, feature-level documentation is available in the [documentation](docs/index.md).

**Important**

The project is made so that you can run it without providing any external credentials. However, many features will be disabled unless you provide them. For more information on these, see [Feature specific configuration](#feature-specific-configuration)

---

## Features

### Learning & Content Management 📚
- Course, module, lesson, and exercise management
- Versioned course authoring with diff-based change tracking
- Dynamic language and tag configuration
- YAML imports and exports of courses
- Lesson submission and completion workflows

### User Progress & Engagement 🎯
- Timezone-aware streak tracking
- Virtual coin reward system
- Per-course and per-lesson progress tracking
- User onboarding and preference management

### Projects & Code Execution ▶️
- Project creation and modification with file snapshot diffing
- Code execution via Piston runtime

### Authentication & Access 🔒
- Firebase-based authentication (optional)
- Demo user authentication mode
- JWT-based session management

### AI Chatbot & Credits ✨
- AI chatbot integration with server-side streaming (SSR)
- Credit-based AI usage limits (optional)

### Storage & Infrastructure 🏗️
- Pluggable blob storage (Local, GCS, S3)
- Redis-based caching (optional)
- Subscription plans with Stripe integration (optional)

### Testing	 📝
- Integration test suite with 100+ tests

---

## Requirements

- **Docker**

### Feature-Specific Configuration

The application runs without external credentials. If not provided, the corresponding features are disabled or fall back to development-safe defaults.

- **AI Features**  
  Requires a Gemini API key. See [Enabling AI features](#enabling-ai-features-optional)

- **Authentication (OAuth)**  
  Requires a Firebase service account JSON. See [Setting up Firebase](#setting-up-firebase-optional)
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
  Requires bucket name and project ID. (Might not work, please go with AWS for now)

---

## Project Setup

Below are setup instructions for running the backend locally and enabling features. I recommend you use the web docs, as they also cover the frontend setup. https://ludocode.dev/resources/docs

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

After you have your Postgres & Application containers running, you will have a pre-seeded demo user & lessons. On the frontend, simply visit `your-frontend-url` and you will be automatically authenticated as a demo user.

---

### Enabling AI features (Optional)
To enable AI features, you need a Gemini API Key & enable the feature in the environment variables.

1. Go to https://aistudio.google.com/ -> Sign in with Google -> Get API Key.
2. Set `AI_ENABLED=true` in your environment variables.
3. Set `GEMINI_API_KEY` to your Gemini API Key

---

### Enabling Code Execution

Code execution uses the Piston API. Below are the setup instructions. You can also look at the Piston repo if you have issues with setup https://github.com/engineer-man/piston

1. Ensure you have a Linux environment, Docker, Node JS 15 or later, cgroup v2 enabled, and cgroup v1 disabled
2. Clone the piston repo
```
# clone and enter repo

git clone https://github.com/engineer-man/piston
```
3. Start the API container & install the CLI
```
# Start the API container

docker-compose up -d api

# Install all the dependencies for the cli

cd cli && npm i && cd -
```
4. Install the runtimes you want (with Python example)
```
# List all available packages

cli/index.js ppman list

# Install latest python

cli/index.js ppman install python

# Install specific version of python

cli/index.js ppman install python=3.9.4
```
5. Set the Piston URL on the backend & enable Piston
```
# Set the URL

PISTON_BASE=http://localhost:2000/api/v2

# Enable Piston Runtime

PISTON_ENABLED=true
```


---

### Setting up Firebase (Optional)

If not configured, Firebase authentication will be disabled and your app will be in demo mode (1 user for the app)

---

#### 1. Create a Firebase Project

1. Go to https://console.firebase.google.com
2. Click **Create Project**
3. Once created, open your project dashboard

---

#### 2. Configure Authentication Providers (Frontend)

1. Go to **Authentication → Sign-in method**
2. Enable the providers you plan to use:
  - Google
  - GitHub
  - Email/Password
  - etc.

3. For OAuth providers (Google, GitHub):
  - Configure the required OAuth credentials
  - Add your frontend domain to **Authorized domains**

   Example for local development:
   ```
   localhost
   ```

4. In **Authentication → Settings → Authorized domains**, ensure your frontend URL is listed:
  - `localhost`
  - `your-production-domain.com`

---

#### 3. Set Authorized Origins (Important)

In your OAuth provider configuration (Google/GitHub), add:

**Authorized JavaScript origins**
```
http://localhost:5173
https://your-frontend-domain.com
```

**Authorized redirect URI** (if applicable)
```
http://localhost:5173
https://your-frontend-domain.com
```

These must match your frontend configuration.

---

#### 4. Generate a Service Account Key (Backend)

1. Go to **Project Settings → Service Accounts**
2. Click **Generate new private key** (choose the java one)
3. Download the JSON file

---

#### 5. Configure Backend Environment Variable

Set:

```
FIREBASE_ENABLED=true
FIREBASE_SERVICE_ACCOUNT_JSON={...full service account JSON...}
```

The account json value must contain the entire contents of the downloaded JSON file.

When using Docker or `.env`, ensure it is either:

- A properly escaped single-line JSON string
- Injected as a multiline environment variable (recommended for Docker secrets)

If omitted or invalid, Firebase authentication will be disabled.

---

### Setting up Tests
- The test suite uses **Test containers**. Ensure Docker is installed and running before executing tests. (Ensure Docker Desktop on MacOS / Windows)
- All containers (Postgres, etc.) will be started automatically during the test run.
- Tests use a liquibase seed schema for initial data (e.g. an initial course structure)
- Make sure you have your docker engine running & Java 21
- Run the tests:
```
./mvnw clean test -Dapi.version=1.44 -Dtest='*IT'
```

---

### OpenAPI Documentation
The documentation is generated by spring and swagger. To access it, start the spring application and visit:
- `http://localhost:8080/swagger-ui/index.html` for the web UI documentation.
- `http://localhost:8080/v3/api-docs` for a JSON representation of the docs.
- `http://localhost:8080/v3/api-docs.yaml` for a YAML representation of the docs.

---

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

### AI
Handles AI chatbot messages & user credits

### Auth
Handles user authentication, and issuing JWT tokens + cookies.

### Catalog
Stores and manages all static course content: courses, tags, modules, lessons. Additionally handles curriculum modifications

### Features
Provides endpoints for querying active features (e.g. whether AI, runtimes, etc. are enabled).

### Languages
Stores and manages languages for runtimes & courses. Additionally handles language modifications.

### Lesson
Stores and manages lessons and exercises. Additionally handles exercise modifications.

### Projects
Manages user code projects and their files, saves snapshots, and executes code submissions via the Piston runtime client.

### Preferences
Manages user onboarding & preference data.

### Progress
Tracks all progress-related data:

- User coins
- User streaks
- User lesson completions
- Per-course progress

### Storage
Handles all interactions with blob stoage: uploading, fetching, and managing stored files. Configurable with either local storage, S3, or GCS.

### Subscription
Manages user subscriptions & stripe webhooks

### Tag
Handles tags that can be attached to courses

### User
Manages user creation, retrieval, updates, & deletion.

## Naming & Trademark

"Ludocode" is a trademark of the Ludocode project.

The source code in this repository is open source and can be used, modified, and even commercialized.

If you build a commercial product or hosted service based on this project, please use a different name and branding so it is not confused with the official Ludocode project.
