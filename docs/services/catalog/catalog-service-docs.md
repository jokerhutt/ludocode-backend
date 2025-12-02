<h1 align="center">The Catalog Service</h1>

## Overview

The Catalog Service delivers read-only access to the published course catalog.  
It exposes the entities and relationships that define a course (modules, lessons, exercises, and
options) and provides the data the frontend and Progress Service require to determine lesson order,
structure, and user progression paths.

---

## Responsibilities

- Provide read access to published course content
- Expose the structural hierarchy of courses (modules → lessons → exercises)
- Return paged or batched catalog entities when requested by ID
- Provide ordering and next-lesson navigation information
- Supply structural metadata to dependent services

---

## Boundaries & Non-Responsibilities

The Catalog Service **never** modifies catalog content.  
All catalog mutations, diffing, and versioning belong to the  
[Snapshot Service](./catalog-snapshot-service-docs.md).

It also does **not**:

- track user progress or correctness
- compute streaks or scoring
- validate lesson submissions
- serve editable snapshots

---

## Data Models

The Catalog Service exposes read-only DTOs derived from stored entities:

```
CourseResponse
  - id, title

ModuleResponse
  - id, title, courseId, orderIndex

LessonResponse
  - id, title, orderIndex, isCompleted (user-specific)

ExerciseResponse
  - id, title, prompt, exerciseType, media, orderIndex, version
  - correctOptions[], distractors[]

ExerciseOptionResponse
  - optionId, content, answerOrder (nullable), exerciseVersion
```

The Catalog Service does not expose internal tables or bridge entities directly—only the DTOs required
for rendering the user experience.

For information about the table structure and the bridging tables, see [Snapshot Service](catalog-snapshot-service-docs.md)

---

## Core Operations

- Retrieve a list of available courses
- Fetch a course’s module and lesson tree (FlatCourseTree)
- Fetch modules or lessons by ID lists (batched)
- Fetch exercises for a lesson with their option data
- Determine navigation elements such as next lesson or module for the Progress Service

---

## Public API

| Method | Path                            | Returns                  | Purpose                                                |
|-------:|---------------------------------|--------------------------|--------------------------------------------------------|
|    GET | `/courses`                      | `List<CourseResponse>`   | Retrieve list of available courses                     |
|    GET | `/courses/{courseId}/tree`      | `FlatCourseTreeResponse` | Retrieve hierarchical module–lesson structure          |
|    GET | `/modules/ids`                  | `List<ModuleResponse>`   | Retrieve modules for provided module IDs               |
|    GET | `/lessons/ids`                  | `List<LessonResponse>`   | Retrieve lessons for provided lesson IDs               |
|    GET | `/lessons/{lessonId}/exercises` | `List<ExerciseResponse>` | Retrieve exercises with options for a given lesson     |

All endpoints are **read-only**.

---

## Workflows

**Catalog Fetch Workflow**
```
Client requests course tree
→ Catalog Service returns FlatCourseTree
→ Client requests missing modules/lessons only (batched)
→ Exercises are fetched lazily per lesson when needed
```

This minimizes network calls and aligns with the Query/Batcher pattern on the frontend.

---

## Integration Points

**Inbound:**
- Progress Service queries the Catalog Service for:
    - next lesson in course
    - mapping from lesson → module → course

**Outbound:**
- None. The Catalog Service does not perform mutations or push changes elsewhere.

---

## Future Work / Known Gaps

- batched course metadata for large catalogs
- optional projection endpoints for partial exercise payloads
- localized course metadata (titles, prompts, and option content)