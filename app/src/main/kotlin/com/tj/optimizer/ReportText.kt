package com.tj.optimizer

/** The facts the first screen prints, collected by [SystemReader]. Kept apart from the Android classes so [ReportText] stays pure and testable. */
class DeviceFacts(
    val appVersion: String,
    val androidRelease: String,
    val sdkInt: Int,
    val securityPatch: String,
    val model: String,
    val abis: List<String>,
    val lowRamDevice: Boolean,
    val memoryClassMb: Int,
    val largeMemoryClassMb: Int,
    val lowMemoryNow: Boolean,
    val lowMemoryThresholdKb: Long,
    val mem: MemInfo?,
)

object ReportText {
    /** A plain-text snapshot of this device's memory situation. The owner can copy it into a chat, so a new Claude session sees real numbers. */
    fun build(f: DeviceFacts): String = buildString {
        appendLine("Optimizer ${f.appVersion}")
        appendLine("Android ${f.androidRelease} (API ${f.sdkInt}), security patch ${f.securityPatch}")
        appendLine("Device: ${f.model}")
        appendLine("CPU ABIs: ${f.abis.joinToString(", ")}")
        appendLine()
        val m = f.mem
        if (m == null) {
            appendLine("RAM: /proc/meminfo could not be read")
        } else {
            val pct = if (m.availablePercent >= 0) " (${m.availablePercent}%)" else ""
            appendLine("RAM total:      ${formatKb(m.totalKb)}")
            appendLine("RAM available:  ${formatKb(m.availableKb)}$pct")
            appendLine("RAM free:       ${formatKb(m.freeKb)}")
            appendLine("File cache:     ${formatKb(m.cachedKb)}")
            appendLine("Swap used:      ${formatKb(m.swapUsedKb)} of ${formatKb(m.swapTotalKb)}")
        }
        appendLine()
        appendLine("Low-RAM (Go) device: ${f.lowRamDevice}")
        appendLine("Memory low right now: ${f.lowMemoryNow} (threshold ${formatKb(f.lowMemoryThresholdKb)})")
        appendLine("Heap limit per app: ${f.memoryClassMb} MB (large heap ${f.largeMemoryClassMb} MB)")
    }
}
