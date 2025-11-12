package com.ludocode.ludocodebackend.playground.app.port.out

interface GcsPortForPlayground {

    fun getContentFromUrls (bucket: String, paths: List<String>) : Map<String, String>

}