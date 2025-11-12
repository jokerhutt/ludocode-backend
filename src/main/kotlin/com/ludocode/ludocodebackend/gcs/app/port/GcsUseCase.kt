package com.ludocode.ludocodebackend.gcs.app.port

interface GcsUseCase {

    fun getContentFromUrls (bucket: String, paths: List<String>): Map<String, String>

}