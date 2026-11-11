package com.tj.optimizer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTextTest {
    private val meminfo = MemInfo.parse("MemTotal: 1953264 kB\nMemFree: 101312 kB\nMemAvailable: 712344 kB\nBuffers: 20244 kB\nCached: 598700 kB\nSwapTotal: 524284 kB\nSwapFree: 377912 kB\n")

    private fun full(): DeviceFacts = DeviceFacts(
        appVersion = "0.2.0", androidRelease = "10", sdkInt = 29, securityPatch = "2021-07-05", model = "alps tb8168p1_bsp",
        abis = listOf("armeabi-v7a", "armeabi"), lowRamDevice = true, memoryClassMb = 128, largeMemoryClassMb = 256,
        lowMemoryNow = false, lowMemoryThresholdKb = 153600, mem = meminfo,
        uptimeMs = 36_000_000L,
        cpu = CpuInfo(4, "0xd03", "half thumb neon vfpv4"),
        swaps = listOf(SwapDevice("/dev/block/zram0", "partition", 524284, 146372)),
        vm = VmStat.parse("pgmajfault 123456\npswpin 10\npswpout 20\nworkingset_refault 3456\nallocstall_normal 7\nallocstall_movable 3\n"),
        thermal = "NONE (cool)",
        dataTotalBytes = 11_811_160_064L, dataFreeBytes = 5_905_580_032L,
        animWindow = 0f, animTransition = 0f, animAnimator = 0f,
        appsTotal = 142, appsSystem = 96, selfPssKb = 9_000,
    )

    @Test
    fun theBaselineReportNamesEveryFact() {
        val t = ReportText.build(full())
        assertTrue(t, t.contains("CPU: 4 cores, Cortex-A53 (0xd03); ABIs armeabi-v7a, armeabi"))
        assertTrue(t, t.contains("CPU features: half thumb neon vfpv4"))
        assertTrue(t, t.contains("Up since boot: 10 h 0 min"))
        assertTrue(t, t.contains("Thermal status: NONE (cool)"))
        assertTrue(t, t.contains("Swap device:    /dev/block/zram0 (zram)"))
        assertTrue(t, t.contains("major page faults 123456 (12345/h), refaults 3456 (345/h)"))
        assertTrue(t, t.contains("direct-reclaim stalls 10 (1/h)"))
        assertTrue(t, t.contains("This app now: 8 MB (PSS)") || t.contains("This app now: 9 MB (PSS)"))
        assertTrue(t, t.contains("Storage (data): 5.50 GB free of 11.00 GB (50%)"))
        assertTrue(t, t.contains("Apps installed: 142 (96 system, 46 added)"))
        assertTrue(t, t.contains("Animation scales (window / transition / animator): 0.0 / 0.0 / 0.0"))
        assertTrue(t, t.contains("the Launcher owns these"))
        assertTrue(t, t.contains("Storage speed test: not run (tap the button)"))
    }

    @Test
    fun missingSourcesPrintAsNotAvailableAndNothingElseBreaks() {
        val t = ReportText.build(DeviceFacts("0.2.0", "10", 29, "?", "x", emptyList(), false, 128, 256, false, 0, null))
        assertTrue(t, t.contains("CPU: n/a"))
        assertTrue(t, t.contains("RAM: /proc/meminfo could not be read"))
        assertTrue(t, t.contains("Memory-pressure counters: n/a"))
        assertTrue(t, t.contains("Storage (data): n/a free of n/a"))
        assertTrue(t, t.contains("Up since boot: n/a"))
        assertFalse(t, t.contains("Swap device"))
        assertFalse(t, t.contains("Apps installed"))
    }

    @Test
    fun theStorageTestShowsItsResultsOrWhyThereAreNone() {
        val r = StorageBenchResult(64, 18.5, 4000, 9000, 200)
        val t = ReportText.build(full(), r)
        assertTrue(t, t.contains("Storage speed test (64 MB temporary file in this app's cache, deleted afterwards):"))
        assertTrue(t, t.contains("sequential write + fsync: 18.5 MB/s"))
        assertTrue(t, t.contains("mean 4000 us, 95th percentile 9000 us (about 250/s), 200 writes"))
        assertTrue(t, t.contains("reads are not timed"))
        assertTrue(ReportText.build(full(), null, "running, about 10 to 30 seconds").contains("Storage speed test: running, about 10 to 30 seconds"))
    }
}
