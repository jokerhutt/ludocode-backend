package com.ludocode.ludocodebackend.storage.app.dto.request

data class StoragePutRequest(val path: String, val content: String)

data class StoragePutRequestList(val requests: List<StoragePutRequest>)
