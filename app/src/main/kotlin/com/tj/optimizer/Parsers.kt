package com.tj.optimizer

/**
 * Pure parsers for the kernel text files the baseline report reads. No Android imports, so every one of them is unit-tested on the JVM.
 * Each returns null (or an empty list) for unreadable or foreign input instead of throwing: SELinux may hide a file on some builds, and the report then says "n/a".
 */

/** What /proc/cpuinfo says about the CPU, as far as the report needs it. */
class CpuInfo(val cores: Int, val part: String, val features: String) {
    /** The core's name for the ARM part ids that matter on Android tablets; otherwise the raw id. */
    val partName: String
        get() = when (part.lowercase()) {
            "" -> "n/a"
            "0xc07" -> "Cortex-A7"
            "0xc09" -> "Cortex-A9"
            "0xc0f" -> "Cortex-A15"
            "0xd03" -> "Cortex-A53"
            "0xd04" -> "Cortex-A35"
            "0xd05" -> "Cortex-A55"
            "0xd07" -> "Cortex-A57"
            "0xd08" -> "Cortex-A72"
            "0xd09" -> "Cortex-A73"
            else -> part
        }

    companion object {
        fun parse(text: String): CpuInfo? {
            var cores = 0
            var part = ""
            var features = ""
            for (line in text.lineSequence()) {
                val colon = line.indexOf(':')
                if (colon <= 0) continue
                val key = line.substring(0, colon).trim()
                val value = line.substring(colon + 1).trim()
                when {
                    key == "processor" -> cores++
                    key == "CPU part" && part.isEmpty() -> part = value
                    key == "Features" && features.isEmpty() -> features = value
                }
            }
            return if (cores == 0) null else CpuInfo(cores, part, features)
        }
    }
}

/** One line of /proc/swaps (sizes in kB). A phone or tablet with zram lists /dev/block/zram0 here. */
class SwapDevice(val name: String, val type: String, val sizeKb: Long, val usedKb: Long) {
    val isZram: Boolean get() = name.contains("zram")

    companion object {
        fun parseAll(text: String): List<SwapDevice> {
            val out = ArrayList<SwapDevice>()
            for ((i, line) in text.lineSequence().withIndex()) {
                if (i == 0) continue   // the header line
                val f = line.trim().split(Regex("\\s+"))
                if (f.size < 4) continue
                val size = f[2].toLongOrNull() ?: continue
                val used = f[3].toLongOrNull() ?: continue
                out.add(SwapDevice(f[0], f[1], size, used))
            }
            return out
        }
    }
}

/** The cumulative counters of /proc/vmstat. They count since boot, so they are only meaningful next to the uptime ([perHour]). */
class VmStat(private val values: Map<String, Long>) {
    /** The counter, or -1 when this kernel does not have it. */
    operator fun get(key: String): Long = values[key] ?: -1L

    /** The sum of every counter whose name starts with [prefix] (a kernel may split one counter per memory zone), or -1 when there is none. */
    fun sumPrefix(prefix: String): Long {
        var found = false
        var sum = 0L
        for ((k, v) in values) if (k.startsWith(prefix)) { found = true; sum += v }
        return if (found) sum else -1L
    }

    companion object {
        fun parse(text: String): VmStat? {
            val map = HashMap<String, Long>()
            for (line in text.lineSequence()) {
                val space = line.indexOf(' ')
                if (space <= 0) continue
                val value = line.substring(space + 1).trim().toLongOrNull() ?: continue
                map[line.substring(0, space)] = value
            }
            return if (map.isEmpty()) null else VmStat(map)
        }

        /** [count] events over [uptimeMs] as "per hour"; -1 when either is unknown or the device has just booted. */
        fun perHour(count: Long, uptimeMs: Long): Long =
            if (count < 0 || uptimeMs < 60_000L) -1L else count * 3_600_000L / uptimeMs
    }
}
