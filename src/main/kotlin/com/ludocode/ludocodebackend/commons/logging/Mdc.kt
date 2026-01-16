package com.ludocode.ludocodebackend.commons.logging

import org.slf4j.MDC

inline fun <T> withMdc(vararg pairs: Pair<String, String>, block: () -> T): T {
    val old = MDC.getCopyOfContextMap()
    try {
        for ((k, v) in pairs) MDC.put(k, v)
        return block()
    } finally {
        if (old == null) MDC.clear() else MDC.setContextMap(old)
    }
}