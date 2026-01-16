<h1 align="center">The Storage Service</h1>

## Overview

The storage service is responsible for performing CRUD operations on storage buckets.

Currently, the two supported storage modes are:

- Google Cloud Storage (enabled when `gcs.config` is true)
- Local storage (enabled when `gcs.config` is false)

---

### Storage Interface

```kt
    // List Operations
    fun getContentFromUrls (paths: List<String>) : Map<String, String>
    fun uploadDataList (reqs: StoragePutRequestList): UploadedPaths
    fun deleteDataList (req: StorageDeleteRequest): UploadedPaths

    // Single Element Operations
    fun getContentFromPath(path: String): String
```

### DTOs

```kt
    StoragePutRequest(val path: String, val content: String)
    StorageDeleteRequest(val paths: List<String>)
```

---

### Todos

- Rename reqs to req in `uploadDataList`
- Remove storageDeleteReq in `deleteDataList`
