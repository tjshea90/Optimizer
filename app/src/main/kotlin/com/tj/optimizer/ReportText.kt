package com.tj.optimizer

/**
 * The facts the screen prints, collected by [SystemReader]. Every default means "unknown": a fact the device will not give simply prints as n/a.
 * Kept apart from the Android classes so [ReportText] stays pure and testable.
 */
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
    val uptimeMs: Long = -1L,
    val cpu: CpuInfo? = null,
    val swaps: List<SwapDevice> = emptyList(),
    val vm: VmStat? = null,
    val thermal: String = "n/a",
    val dataTotalBytes: Long = -1L,
    val dataFreeBytes: Long = -1L,
    val animWindow: Float = -1f,
    val animTransition: Float = -1f,
    val animAnimator: Float = -1f,
    val appsTotal: Int = -1,
    val appsSystem: Int = -1,
    val selfPssKb: Int = -1,
)

object ReportText {
    /**
     * A plain-text snapshot of this device's memory, storage and speed situation. The owner copies it into a chat, so a new Claude session sees real numbers.
     * [bench] is the last storage speed test; [benchNote] says why there is none ("running...", "not run: ...").
     */
    fun build(f: DeviceFacts, bench: StorageBenchResult? = null, benchNote: String? = null): String = buildString {
        appendLine("Optimizer ${f.appVersion}")
        appendLine("Android ${f.androidRelease} (API ${f.sdkInt}), security patch ${f.securityPatch}")
        appendLine("Device: ${f.model}")
        val c = f.cpu
        val cpuText = if (c == null) "n/a" else "${c.cores} cores, ${c.partName} (${c.part})"
        appendLine("CPU: $cpuText; ABIs ${f.abis.joinToString(", ")}")
        if (c != null && c.features.isNotEmpty()) appendLine("CPU features: ${c.features}")
        appendLine("Up since boot: ${formatDuration(f.uptimeMs)} (sleep counts)")
        appendLine("Thermal status: ${f.thermal}")
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
        if (f.swaps.isNotEmpty()) {
            appendLine("Swap device:    " + f.swaps.joinToString(", ") { it.name + if (it.isZram) " (zram)" else "" })
        }
        appendMemoryPressure(f)
        appendLine()
        appendLine("Low-RAM (Go) device: ${f.lowRamDevice}")
        appendLine("Memory low right now: ${f.lowMemoryNow} (threshold ${formatKb(f.lowMemoryThresholdKb)})")
        appendLine("Heap limit per app: ${f.memoryClassMb} MB (large heap ${f.largeMemoryClassMb} MB)")
        if (f.selfPssKb >= 0) appendLine("This app now: ${formatKb(f.selfPssKb.toLong())} (PSS)")
        appendLine()
        val dataPct = if (f.dataTotalBytes > 0 && f.dataFreeBytes >= 0) " (${f.dataFreeBytes * 100 / f.dataTotalBytes}%)" else ""
        appendLine("Storage (data): ${formatBytes(f.dataFreeBytes)} free of ${formatBytes(f.dataTotalBytes)}$dataPct")
        if (f.appsTotal >= 0) appendLine("Apps installed: ${f.appsTotal} (${f.appsSystem} system, ${f.appsTotal - f.appsSystem} added)")
        appendLine(
            "Animation scales (window / transition / animator): ${formatScale(f.animWindow)} / ${formatScale(f.animTransition)} / ${formatScale(f.animAnimator)}" +
                "  [the Launcher owns these; Optimizer only reads them]"
        )
        appendLine()
        appendBench(bench, benchNote)
    }

    private fun StringBuilder.appendMemoryPressure(f: DeviceFacts) {
        val vm = f.vm
        if (vm == null) {
            appendLine("Memory-pressure counters: n/a")
            return
        }
        fun n(v: Long) = if (v < 0) "n/a" else v.toString()
        fun perHour(v: Long): String {
            val h = VmStat.perHour(v, f.uptimeMs)
            return if (h < 0) "" else " (${h}/h)"
        }
        val major = vm["pgmajfault"]
        val refault = if (vm["workingset_refault"] >= 0) vm["workingset_refault"] else vm.sumPrefix("workingset_refault")
        val stalls = vm.sumPrefix("allocstall")
        appendLine("Since boot:     major page faults ${n(major)}${perHour(major)}, refaults ${n(refault)}${perHour(refault)}")
        appendLine("                swapped in ${n(vm["pswpin"])} / out ${n(vm["pswpout"])} pages, direct-reclaim stalls ${n(stalls)}${perHour(stalls)}")
    }

    private fun StringBuilder.appendBench(bench: StorageBenchResult?, note: String?) {
        if (bench == null) {
            appendLine("Storage speed test: ${note ?: "not run (tap the button)"}")
            return
        }
        fun mb(v: Double) = String.format(java.util.Locale.US, "%.1f MB/s", v)
        appendLine("Storage speed test (${bench.sizeMb} MB temporary file in this app's cache, deleted afterwards):")
        appendLine("  write + fsync: ${mb(bench.writeMBps)}")
        appendLine("  read:          ${mb(bench.readMBps)}")
        appendLine("  random 4 KB:   mean ${bench.randomReadMeanUs} us, 95th percentile ${bench.randomReadP95Us} us (about ${bench.randomReadIops} reads/s), ${bench.randomReads} reads")
    }
}
