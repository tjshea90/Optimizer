package com.tj.optimizer

/** Small pure formatters for the report. */

/** A byte count as people read it, through [formatKb]; "n/a" when unknown (negative). */
fun formatBytes(bytes: Long): String = if (bytes < 0) "n/a" else formatKb(bytes / 1024L)

/** A duration as "3 d 4 h 12 min", "5 h 3 min" or "42 min"; "n/a" when unknown. */
fun formatDuration(ms: Long): String {
    if (ms < 0) return "n/a"
    val minutes = ms / 60_000L
    val d = minutes / (24 * 60)
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return when {
        d > 0 -> "$d d $h h $m min"
        h > 0 -> "$h h $m min"
        else -> "$m min"
    }
}

/** PowerManager.THERMAL_STATUS_* as a word (API 29 and later); "n/a" for anything else. */
fun thermalName(status: Int): String = when (status) {
    0 -> "NONE (cool)"
    1 -> "LIGHT"
    2 -> "MODERATE"
    3 -> "SEVERE"
    4 -> "CRITICAL"
    5 -> "EMERGENCY"
    6 -> "SHUTDOWN"
    else -> "n/a"
}

/** One decimal for animation scales: 0.0, 0.5, 1.0; "n/a" when the setting could not be read (negative). */
fun formatScale(scale: Float): String = if (scale < 0f) "n/a" else String.format(java.util.Locale.US, "%.1f", scale)
