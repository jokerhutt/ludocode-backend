<h1 align="center">The Lesson Completion Service</h1>

## Table of Contents

## Table of Contents

1. [Overview](#overview)
2. [Responsibilities](#responsibilities)
3. [Boundaries & Non-Responsibilities](#boundaries--non-responsibilities)
4. [Data Models](#data-models)
    - [Submission DTOs](#submission-dtos)
    - [Response Types](#response-types)
5. [Scoring Rules](#scoring-rules)
    - [INFO Lessons](#info-lessons)
6. [Analytics & Attempt Storage](#why-store-attempts)
    - [Persisted Structures](#persisted-structures)
    - [Version-linked Analytics](#version-linked-analytics)
    - [INFO Exercise Implications](#info-exercises-1)
    - [Enabled Metrics](#metrics-enabled)
7. [Core Operations](#core-operations)
    - [Submission Pipeline](#submission-pipeline)
8. [Public API](#public-api)
9. [Workflows](#workflows)
    - [Lesson Completion Workflow](#lesson-completion-workflow)
10. [Integration Points](#integration-points)
11. [Future Work / Known Gaps](#future-work--known-gaps)

## Overview

The Lesson Completion Service finalizes a user's lesson submission, computes their score, updates
course progress, applies streak and coin changes, and returns a `LessonCompletionPacket` that the
frontend uses to determine which completion screens to display.

It represents the end of the lesson lifecycle: once the user commits their attempts, this service
validates the submission, persists it, derives the user's rewards, and moves them to the next lesson.

---

## Responsibilities

- Accept and validate a lesson submission
- Persist attempts and compute scoring and accuracy
- Mark the lesson as completed for the user
- Update course progression (next lesson, first-time completion, etc.)
- Apply streak and coin deltas
- Return a structured `LessonCompletionPacket` summarizing the results

---

## Boundaries & Non-Responsibilities

The Lesson Completion Service does **not**:

- fetch or render catalog content (Catalog Service does this)
- provide or evaluate lesson content logic (Lesson Flow handles correctness)
- manage frontend UI transitions
- batch or modify snapshots (Snapshot Service owns catalog mutations)

It operates strictly at the moment of final submission.

---

## Data Models

### Submission DTOs
```
LessonSubmissionRequest
    id: UUID                              // unique submission ID
    lessonId: UUID                        // target lesson
    submissions: List<ExerciseSubmissionRequest>

ExerciseSubmissionRequest
    exerciseId: UUID
    version: Int                          // exercise version at time of attempt
    attempts: List<ExerciseAttemptRequest>

ExerciseAttemptRequest
    exerciseId: UUID
    isCorrect: Boolean
    answer: List<AttemptToken>

AttemptToken
    id: UUID                              // ExerciseOptionId
    value: String                         // selected option content
```

### Response Types
```
LessonCompletionPacket
    content: LessonCompletionResponse?     // null if duplicate
    status: LessonCompletionStatus         // OK | COURSE_COMPLETE | DUPLICATE

LessonCompletionResponse
    newCoins: UserCoinsResponse
    newStreak: UserStreakResponse
    newCourseProgress: CourseProgressResponse
    updatedCompletedLesson: LessonResponse
    accuracy: BigDecimal
```

Duplicate submissions return a `LessonCompletionPacket` with `content = null`.

---

## Scoring Rules

Scoring is derived per submission based on user attempts:

- **Correct attempt on the first try** → higher score (`isPerfect`)
- **Correct after retries** → reduced score
- **Incorrect attempts** → zero score
- **Perfect lesson** (all exercises solved on first attempt) → bonus points

### INFO Lessons

INFO-type exercises contain no graded attempts:

```
- No correctness
- No answer tokens
- Do not affect accuracy
- Count as completed automatically if viewed
```

If a lesson contains only INFO exercises, then it is an INFO lesson. For these, the total attempt count is zero, and accuracy defaults to `1.0`.

---

### Why store attempts?

A final "correct/incorrect" state tells you if a user succeeded.
Attempts show how they got there, including:

- how many retries were needed
- which incorrect options were most frequently selected
- whether changes to an exercise improved outcomes
- differences in performance across exercise versions

#### Persisted Structures

Exercise attempts are stored with both identity and version context:
```
ExerciseAttempt
id: UUID
userId: UUID
exerciseId: UUID
exerciseVersion: Int        // ties analytics to the exact revision of the exercise

AttemptOption
attemptId: UUID
exerciseOptionId: UUID      // references the option the user selected
```


Because `ExerciseOption` defines the ordering and correctness of options, analytics can infer:

- distractor preference patterns
- common incorrect sequences
- whether option position affects selection rates

#### Version-linked Analytics

By persisting `exerciseVersion`, attempts remain attributable even after modifying an exercise:

v1: "Declare a variable using const" → common confusion between const/"const"
v2: redesigned prompt and answers → correctness improves

Without version tracking, historical data for unrelated prompts would be merged and rendered meaningless.

For more on the version tracking, see the [Snapshot Service](../catalog/catalog-snapshot-service-docs.md)

#### INFO Exercises

INFO-type exercises generate no attempts, do not affect accuracy, and are excluded from analytics.
This produces clean separation between:

- viewed content
- attempted exercises
- scored lessons

---

#### Metrics Enabled

With the stored structures, the following analyses become possible:

- average attempts per exercise
- correctness distribution per version
- distractor rankings and frequency
- course difficulty heatmaps
- drop-off points per lesson
- streak correlation with retries and accuracy

The analytics emerge from the persisted attempts and do not require additional client logic.

However, the actual functionality for this is still in the works. For now, the data structures exist to create an analytics system.

---

## Core Operations

### Submission Pipeline
- Detect duplicate submission requests
- Store exercise attempts and selected options
- Compute per-exercise and per-attempt scores
- Grant bonus points for perfect lessons (one attempt per exercise)
- Apply coin deltas and update streak
- Mark the lesson as completed and advance course progress
- Return updated state in a `LessonCompletionPacket`

---

## Public API

| Method | Path                          | Returns                 | Purpose                                            |
|------: |-------------------------------|-------------------------|----------------------------------------------------|
|   GET  | `/progress/completion/submit` | `LessonCompletionPacket`| Commit a lesson submission and return result state |

The frontend uses the status and metadata in the packet to decide which completion screens to render.

---

## Workflows

### Lesson Completion Workflow
```
Client submits LessonSubmissionRequest
→ Check duplicate submission
→ Persist attempts and compute points
→ Update lesson completion flag and set isCompleted=true if new completion
→ Update user streak and coin balance
→ Recompute course progress and determine next lesson
→ Derive status:
       - OK
       - COURSE_COMPLETE
       - DUPLICATE
→ Return LessonCompletionPacket
```

Accuracy is computed from total correct attempts vs attempts made.

---

## Integration Points

**Inbound**
- Lesson Flow sends the final `LessonSubmissionRequest`

**Outbound**
- **CourseProgress Service** — updates current lesson and progression
- **Streak Service** — records goal met and updates streak counters
- **Coins Service** — applies point deltas
- **Catalog Service** — resolves lesson tree and next lesson ID

The Lesson Completion Service orchestrates updates but owns none of these domains.

---

## Future Work / Known Gaps

- Per exercise type scoring metrics
- Retry-safe idempotent submissions without relying on DB existence check
- Aggregated performance analytics across multiple lessons