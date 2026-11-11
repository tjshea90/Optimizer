package com.tj.optimizer

/** One installed app's storage use, in bytes. */
class AppStorage(val packageName: String, val label: String, val appBytes: Long, val dataBytes: Long, val cacheBytes: Long) {
    val totalBytes: Long get() = appBytes + dataBytes + cacheBytes
}

/**
 * What the storage screen shows. -1 means "unknown". [needsUsageAccess] is true when Android refused the per-app and per-kind numbers because the
 * "Usage access" special permission is off for Optimizer (the owner switches it on once in Settings; Optimizer reads sizes only, never usage).
 */
class StorageSummary(
    val totalBytes: Long,
    val freeBytes: Long,
    val imageBytes: Long = -1L,
    val videoBytes: Long = -1L,
    val audioBytes: Long = -1L,
    val sharedAppBytes: Long = -1L,
    val apps: List<AppStorage> = emptyList(),
    val needsUsageAccess: Boolean = false,
)

/** Pure logic of the storage screen: no Android classes, unit-tested on the JVM. */
object StorageModel {
    /** Free space as a percent of the total (0..100); -1 when unknown. */
    fun freePercent(total: Long, free: Long): Int = if (total <= 0 || free < 0) -1 else (free * 100 / total).toInt().coerceIn(0, 100)

    /** One plain sentence about how much room is left. The 5% and 10% lines are this app's own rule of thumb, not an Android rule. */
    fun verdict(total: Long, free: Long): String {
        val pct = freePercent(total, free)
        if (pct < 0) return "Free space: unknown."
        val amount = "${formatBytes(free)} free of ${formatBytes(total)} ($pct%)"
        return when {
            pct < 5 -> "VERY LOW: $amount. Updates, caches and temporary files have almost no room."
            pct < 10 -> "LOW: $amount. Little room for updates and caches."
            pct < 20 -> "Getting full: $amount."
            else -> "OK: $amount."
        }
    }

    /** "VERY LOW" / "LOW" for the one-line report, or "" when there is nothing to warn about. */
    fun warningWord(total: Long, free: Long): String {
        val pct = freePercent(total, free)
        return when {
            pct < 0 -> ""
            pct < 5 -> "VERY LOW"
            pct < 10 -> "LOW"
            else -> ""
        }
    }

    /** The [n] apps using the most space (code + data + cache), biggest first. */
    fun topApps(apps: List<AppStorage>, n: Int): List<AppStorage> = apps.sortedByDescending { it.totalBytes }.take(n)

    /** Second line of an app row: "2.23 GB: code 40 MB, data 2.00 GB, cache 200 MB". */
    fun rowDetail(a: AppStorage): String = "${formatBytes(a.totalBytes)}: code ${formatBytes(a.appBytes)}, data ${formatBytes(a.dataBytes)}, cache ${formatBytes(a.cacheBytes)}"

    /** The text block above the list: the verdict, the totals per kind and what the counted apps add up to. */
    fun header(s: StorageSummary): String = buildString {
        appendLine(verdict(s.totalBytes, s.freeBytes))
        if (s.needsUsageAccess) {
            appendLine()
            appendLine("Android hides the per-app and per-kind sizes until \"Usage access\" is switched on for Optimizer (button below). Optimizer only reads sizes.")
            return@buildString
        }
        if (s.imageBytes >= 0 || s.videoBytes >= 0 || s.audioBytes >= 0) {
            appendLine()
            appendLine("On shared storage: images ${formatBytes(s.imageBytes)}, video ${formatBytes(s.videoBytes)}, audio ${formatBytes(s.audioBytes)}, app files ${formatBytes(s.sharedAppBytes)}")
        }
        if (s.apps.isNotEmpty()) {
            val code = s.apps.sumOf { it.appBytes }
            val data = s.apps.sumOf { it.dataBytes }
            val cache = s.apps.sumOf { it.cacheBytes }
            appendLine("${s.apps.size} apps counted: ${formatBytes(code + data + cache)} in all (code ${formatBytes(code)}, data ${formatBytes(data)}, cache ${formatBytes(cache)})")
        }
    }.trimEnd()
}
