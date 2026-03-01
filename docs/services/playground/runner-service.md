<h1 align="center">The Code Runner Service</h1>

## Overview

The Code Runner Service receives executable project snapshots from the Playground Service, prepares
them for the external Piston API, executes the user's code in an isolated runtime, and returns the
resulting output. It abstracts the execution layer from the frontend, ensuring a uniform interface
regardless of language or runtime constraints.

---

## Responsibilities

- Accept a user's project snapshot for execution
- Map project files to Piston-compatible request objects
- Trigger execution in an isolated runtime environment
- Return stdout, stderr, and exit code results to the frontend
- Handle missing or invalid runtime responses defensively

---

## Boundaries & Non-Responsibilities

The Code Runner Service does **not**:

- store or persist project files ([Project Service](project-service-docs.md) owns that)
- manage project versioning or file diffs
- expose or manage language-specific compilers directly

Execution is delegated to **Piston**, a stateless external runtime API that supports multiple languages.

---

## Data Models

### Request Types
```
PistonRequest
    language: String                 // e.g. "python", "javascript", "lua"
    version: String                  // "*" = latest
    files: List<PistonFile>          // mapped from ProjectSnapshot
    stdin: String
    args: List<String>
    compile_timeout: Int
    run_timeout: Int
    compile_memory_limit: Int
    run_memory_limit: Int

PistonFile
    name: String                     // path or identifier of the file
    content: String                  // file source code
```

### Response Types
```
RunnerResult
    stdout: String
    stderr: String
    exitCode: Int

PistonResponse
    run: PistonRun?

PistonRun
    stdout: String
    stderr: String
    code: Int
```

---

## Core Operations

- Receive a `ProjectSnapshot` containing all code files
- Convert snapshot files into Piston format
- Invoke the external execution endpoint
- Map the Piston execution result into `RunnerResult`
- Handle error states (null runs, invalid responses)

---

## Workflows

### Code Execution Workflow
```
Client requests "run"
→ Playground Service provides ProjectSnapshot
→ Code Runner maps files to PistonFile list
→ Build PistonRequest using project language
→ Call Piston API for execution
→ Receive stdout, stderr, exitCode
→ Return RunnerResult to frontend
```

Execution never touches the database.  
All runtime isolation is guaranteed by Piston.

---

## Public API

| Method | Path          | Returns        | Purpose                                             |
|------: |--------------|----------------|-----------------------------------------------------|
|   GET  | `/runner/run`| `RunnerResult` | Execute the user's project code and return the result |

---

## Integration Points

**Inbound**
- Receives project snapshots from the Playground Service

**Outbound**
- **Piston API** — executes code in a secure runtime for supported languages

The Code Runner Service has no internal persistence layer. Its only dependency is the outbound port
responsible for invoking Piston.

---

## Future Work / Known Gaps

- Add structured error codes for runtime failures (OOM, timeout)