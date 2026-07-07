package com.boondi.android.ui.common

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Relative-time formatting mirroring the web app's `formatRelativeTime`. Tolerates both
 * the backend's OffsetDateTime (posts) and LocalDateTime (user.createdAt) ISO strings.
 * java.time is available natively on minSdk 26+.
 */
fun formatRelativeTime(iso: String?): String {
    val then = parseInstant(iso) ?: return ""
    val now = OffsetDateTime.now(ZoneOffset.UTC)
    val seconds = Duration.between(then, now).seconds
    return when {
        seconds < 0 -> "now"
        seconds < 60 -> "now"
        seconds < 3600 -> "${seconds / 60}m"
        seconds < 86_400 -> "${seconds / 3600}h"
        seconds < 604_800 -> "${seconds / 86_400}d"
        else -> then.format(SHORT_DATE)
    }
}

/** e.g. "Joined July 2026" style full date. */
fun formatFullDate(iso: String?): String {
    val then = parseInstant(iso) ?: return ""
    return then.format(FULL_DATE)
}

private fun parseInstant(iso: String?): OffsetDateTime? {
    if (iso.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(iso)
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(iso).atOffset(ZoneOffset.UTC)
        } catch (_: Exception) {
            try {
                LocalDate.parse(iso).atStartOfDay().atOffset(ZoneOffset.UTC)
            } catch (_: Exception) {
                null
            }
        }
    }
}

private val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val FULL_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
