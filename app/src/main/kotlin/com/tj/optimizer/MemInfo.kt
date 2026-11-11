package com.tj.optimizer

/**
 * What the kernel says about RAM and swap: a parsed /proc/meminfo, every size in kB.
 * Pure Kotlin (no Android imports) so it runs in plain JVM unit tests. A field the kernel does not print is -1 ("unknown"), never a silent 0.
 */
class MemInfo(
    val totalKb: Long,
    val freeKb: Long,
    val availableKb: Long,
    val buffersKb: Long,
    val cachedKb: Long,
    val swapTotalKb: Long,
    val swapFreeKb: Long,
) {
    val swapUsedKb: Long
        get() = if (swapTotalKb < 0 || swapFreeKb < 0) -1 else swapTotalKb - swapFreeKb

    /** RAM the system could give an app without a fight, as a percent of the total (0..100); -1 when unknown. */
    val availablePercent: Int
        get() = if (totalKb <= 0 || availableKb < 0) -1 else (availableKb * 100 / totalKb).toInt().coerceIn(0, 100)

    companion object {
        /** Parses the text of /proc/meminfo. Returns null when it has no usable MemTotal (unreadable or not meminfo at all). */
        fun parse(text: String): MemInfo? {
            val values = HashMap<String, Long>()
            for (line in text.lineSequence()) {
                val colon = line.indexOf(':')
                if (colon <= 0) continue
                val number = line.substring(colon + 1).trim().takeWhile { it.isDigit() }.toLongOrNull() ?: continue
                values[line.substring(0, colon).trim()] = number
            }
            val total = values["MemTotal"] ?: return null
            if (total <= 0) return null
            val free = values["MemFree"] ?: -1L
            val buffers = values["Buffers"] ?: -1L
            val cached = values["Cached"] ?: -1L
            // MemAvailable exists since Linux 3.14. If a vendor kernel lacks it, free + buffers + cached is the usual rough stand-in.
            val available = values["MemAvailable"]
                ?: if (free >= 0) free + maxOf(buffers, 0L) + maxOf(cached, 0L) else -1L
            return MemInfo(total, free, available, buffers, cached, values["SwapTotal"] ?: -1L, values["SwapFree"] ?: -1L)
        }
    }
}

/** A size in kB as people read it: "1.86 GB", "712 MB", "640 kB"; "n/a" for an unknown (negative) value. */
fun formatKb(kb: Long): String = when {
    kb < 0 -> "n/a"
    kb >= 1024L * 1024L -> String.format(java.util.Locale.US, "%.2f GB", kb / 1024.0 / 1024.0)
    kb >= 1024L -> "${kb / 1024} MB"
    else -> "$kb kB"
}
