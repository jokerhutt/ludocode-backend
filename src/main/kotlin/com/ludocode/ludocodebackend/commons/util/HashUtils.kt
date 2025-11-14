package com.ludocode.ludocodebackend.commons.util

import java.security.MessageDigest

fun sha256(text: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
