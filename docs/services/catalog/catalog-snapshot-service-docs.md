<h1 align="center">The Snapshot Service</h1>

## Overview

The Snapshot Service manages editable representations (“snapshots”) of courses.  
It delivers complete course snapshots to the Builder and accepts updated snapshots
to compute diffs and apply structural and content changes to the catalog.

---

## Responsibilities

- Build a full editable snapshot of a course (modules, lessons, exercises, options)
- Validate submitted snapshots against schema and business rules
- Compute diffs between stored catalog entities and submitted snapshots
- Apply catalog changes (create, update, delete, reorder)
- Persist versioned content for exercises and options

---

## Boundaries & Non-Responsibilities

The Snapshot Service is not the source of published course content.
It maintains *editable* state only. All runtime catalog queries are handled by the  
[Catalog Service](./catalog-service-docs.md).

The Snapshot Service does **not** manage:

- user progress or scoring
- lesson execution or correctness validation
- authentication or authorization
- serving end-user catalog data

---

## Data Models

### Snapshot Models

```
CourseSnapshot
  - courseId, title, list of ModuleSnapshot

ModuleSnapshot
  - moduleId, title, list of LessonSnapshot

LessonSnapshot
  - lessonId, title, list of ExerciseSnapshot

ExerciseSnapshot
  - exerciseId, title, prompt, type, optionSnapshot list

OptionSnapshot
  - optionId, content, answerOrder (nullable → distractor)
```

Snapshots represent the *editable* shape of a course.
They are submitted as a whole and returned as a whole.

---

### Catalog Models

```
Course
Module (courseId FK, orderIndex, isDeleted)
Lesson (isDeleted)
ModuleLesson (bridge: moduleId, lessonId, orderIndex)
Exercise (id + version composite key)
LessonExercise (bridge: lessonId, exerciseId, orderIndex)
OptionContent (text content)
ExerciseOption (bridge: exerciseId, optionId, answerOrder)
```

The catalog schema separates **content** from **attachments**.  
Modules, lessons, exercises, and options do not own each other; relationships are expressed in bridge tables.

---

## Bridge Tables

Modules–Lessons, Lessons–Exercises, and Exercises–Options are all many-to-many.  
This allows:

- removing a lesson without deleting its exercises
- updating an exercise's content without invalidating its relationships
- tracking correctness and ordering independently of the option text

The bridge entities define **attachment** and **position**, not semantics.

---

## Versioning

Exercises use `(id, version)` as a composite key.  
Each significant edit creates a new version, preserving existing analytics and user data.

Example problem avoided:

A user’s incorrect attempt to `"const"` should not be tied to a new math exercise.  
Versioning isolates attempts from changing prompts and content.

The `ExerciseOption` bridge tracks exercise version, enabling correct swaps without corrupting history.

---

## Reliability

Changes pass through three gates:

1. **Zod Validation** on the frontend (structural and logical rules)
2. **Backend Transactions** in the backend (atomic updates)
3. **SQL Constraints** at commit time (schema integrity)

If persistence fails, the frontend retains the snapshot buffer and can resubmit or amend it.

---

## High Level Flow

1. Receive submitted snapshot
2. Fetch existing catalog state for the course
3. Identify removed modules/lessons/exercises and soft delete them
4. Create or update modules and lessons, adjusting order via bridge tables
5. Version exercises where content changed, preserve version otherwise
6. Create or update option content, update ExerciseOption bridges
7. Rebuild and return a fresh snapshot from the database

No partial updates, snapshots are authoritative.

---

## Core Operations

- Build an editable course snapshot
- Compute diffs between snapshot and catalog
- Persist catalog changes
- Return updated snapshot

---

## Public API

### Endpoints

| Method | Path                        | Returns              | Purpose                                           |
|-------:|----------------------------|---------------------|---------------------------------------------------|
| GET    | `/snapshot/course/{id}`     | `CourseSnapshot`     | Retrieve editable snapshot for a course           |
| POST   | `/snapshot/submit`          | `CourseSnapshot`     | Submit snapshot; apply diff; return updated state |
| POST   | `/snapshot/course/create`   | `List<CourseResponse>` | Create a new empty course                        |

**Controller:** `CatalogAdminController`

No runtime-facing APIs exist here. Everything is Builder-oriented.

---

## Workflows

**Snapshot Submission**
```
Builder edits snapshot → Submit POST
→ Validate → Diff → Persist → Rebuild snapshot
→ Return new snapshot to Builder
```

The Snapshot Service is the *authority* for structural edits.

---

## Integration Points

- **Inbound:** The AI service consumes snapshots to generate prompts and evaluate correctness patterns.
- **Outbound:** Updated snapshots are read via the Catalog Service once published.

No other services call this one directly.

---

## Future Work / Known Gaps

- snapshot history and rollback support
- multi-user editing and conflict resolution
- partial snapshot updates instead of full snapshot mutations

