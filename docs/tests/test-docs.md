<h1 align="center">Testing the Program</h1>

## Table of Contents

1. [Overview](#overview)
2. [Abstract Integration Test](#abstract-integration-test)
3. [Mutable Clock](#mutable-clock)
4. [Feature Flags](#feature-flags)
5. [Structure of a Test Run](#structure-of-a-test-run)
6. [Test Helpers](#test-helpers)
    - [TestRestClient](#testrestclient)
    - [Catalog Initialization](#catalog-initialization)
7. [Test Coverage](#test-coverage)
8. [Summary](#summary)

## Overview

Ludocode tests run against TestContainers for PostgreSQL and Fake GCS. The schema is loaded from
`schema.sql`, so the database structure matches production. Tests interact with real HTTP endpoints,
repositories, and storage.

There are 37 integration tests.

---

## Abstract Integration Test

`AbstractIntegrationTest` is the base class for every integration test. It:

- boots PostgreSQL and Fake GCS containers
- truncates and repopulates all tables before each test
- sets up a catalog and a consistent set of users
- wires repositories and a REST client for making requests
- exposes common UUIDs like `py1L1`, `pyMod1Id`, etc.

It does **not** initialize coins, course progress, or streaks. Those values are created only when the
test under inspection triggers them, so initial state is always the same.

---

## Mutable Clock

Time-related logic uses a replaceable clock:

```
clock.setTime(TestClocks.FIXED_UTC)
clock.setTime(TestClocks.NEXT_DAY_UTC)
```

This lets tests simulate different days without touching system time. For example, if we wanted to test if a streak resets after midnight, all we need to do is set the clock to a time after midnight relative to the tested users last streak submission.

---

## Feature Flags

Some areas of the code are guarded by flags. For example:

```
project.enabled = false
```

When disabled, project service tests are skipped and only filter-chain tests run. When enabled, the
project service tests execute and the filter-chain ones are skipped. Both modes rely on the same
test harness.

---

## Structure of a Test Run

1. Containers start
2. Schema loads
3. DB is truncated before each test
4. Catalog and users are inserted
5. Tests call endpoints or services directly
6. Assertions are made against real repository state

---

## Test Helpers

### `TestRestClient`

Simple wrappers for HTTP calls:

```
postOk(url, userId, body, responseType)
getOk(url, userId, responseType)
assertError(method, url, userId, expectedErrorCode)
```

Reduces repeated setup work in each test.

### Catalog Initialization

`importSnapshots(...)` loads snapshots into tables and sets correct option ordering, lesson ordering,
exercise versions, and content references. IDs are stable across runs.

---

## Test Coverage

- Authenticating a demo user
- Submitting modifications to a courses catalog
- Course progress changes
- Lesson completion and duplicate submission detection
- Correct score computation after lessons
- Attempt and token persistence
- Streak updates with timezone shifts
- Coin updates
- Project creation and file persistence (when enabled)
- Fetching exercises for lessons
- Fetching the course tree for a course
- Handling errors on invalid project submissions

---

## Summary

The test layer boots real containers, loads the schema, populates a catalog and users, and executes
the same flows the application runs under normal operation. Each test starts from a clean state and
relies on predictable IDs, time control, and the same logic paths as production.