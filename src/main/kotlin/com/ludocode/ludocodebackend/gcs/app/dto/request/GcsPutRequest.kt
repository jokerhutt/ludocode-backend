package com.ludocode.ludocodebackend.gcs.app.dto.request

data class GcsPutRequest(val bucketName: String, val path: String, val content: String)
