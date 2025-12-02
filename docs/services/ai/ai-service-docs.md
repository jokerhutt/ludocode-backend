<h1 align="center">The AI Service</h1>

## Overview

The AI Service processes user chat requests and generates contextual AI-assisted responses.  
It streams model output token-by-token to the frontend and augments prompts with lesson or project
context depending on the active chat type.

---

## Responsibilities

- Accept chat requests from the frontend (including full message history)
- Retrieve relevant context (exercise snapshot or project file) based on `ChatType`
- Construct a prompt using the last user message, chat history, and contextual data
- Forward the prompt to an external AI model and stream the result
- Deduct user credits before issuing prompts

---

## Boundaries & Non-Responsibilities

The AI Service does **not**:

- store chat history
- validate or mutate catalog content
- persist or update projects
- track lesson completions or user progress

It only interprets chat input, builds prompts, and streams responses.

Catalog data comes from the [Snapshot](../catalog/catalog-snapshot-service-docs.md) / [Catalog](../catalog/catalog-service-docs.md) services, while file content is retrieved from the [Playground]() service.

---

## Data Models

### Request DTOs

```
ChatRequestBody
    id: String
    messages: List<UIMessageRequest>

UIMessageRequest
    id: String
    role: AiMessageRole   // "user" or "assistant"
    parts: List<UIMessagePart>
    metadata: UIMessageRequestMetadata?

UIMessageRequestMetadata
    chatType: ChatType    // DEFAULT | LESSON | PROJECT
    targetId: UUID?       // exerciseId or fileId (depending on chatType)

UIMessagePart
    type: String          // always "text"
    text: String?         // message content
```

The last message is the one that the user has just sent, while all the ones before it are the chat history.

### Outbound DTOs

```
GeminiRequest
    contents: List<GeminiContent>

GeminiContent
    parts: List<GeminiPart>

GeminiPart
    text: String

AIMessagePart
    type: "text"
    text: String
```

The service maps a prompt into `GeminiRequest` and converts streamed Gemini responses into `AIMessagePart`.

---

## Core Operations

- **Extract chat history** and identify the latest user query
- **Resolve context**:
    - `ChatType.LESSON` → fetch exercise snapshot
    - `ChatType.PROJECT` → fetch project file content
    - `ChatType.DEFAULT` → no contextual fetch
- **Build model prompt** using system role, history, user request, and context
- **Check credits** and deduct if available
- **Stream AI tokens** back to the frontend as `Flux<AIMessagePart>`

---

## Public API

| Method | Path              | Returns        | Purpose                                      |
|-------:|-------------------|----------------|----------------------------------------------|
|    GET | `/ai/prompt/send` | `Flux<String>` | Accept chat request and stream AI responses  |

*Note:* The controller internally maps the flux of tokens into the Vercel streaming format.

---

## Workflows

### Prompt Execution Workflow
```
Client sends ChatRequestBody
→ AI Service validates credits & deducts
→ Extract last message and history
→ If LESSON → fetch ExerciseSnap
→ If PROJECT → fetch file content
→ Build prompt with history + context
→ Gemini client streams output
→ Convert tokens to AIMessagePart
→ Return Flux<String> to client
```

---

## Integration Points

**Inbound Dependencies**
- **CatalogPortForAI** (exercise snapshots)
- **ProjectsPortForAI** (project code files)
- **AICreditService** (credit initialization and deduction)

**Outbound**
- External AI model endpoint (Gemini)

The service itself is stateless beyond credit accounting.

---

## Future Work / Known Gaps

- Token based credit deduction
- Prompt compression for long histories
- Support for additional chat types (e.g., lesson group)
- Optional model selection per user or course
