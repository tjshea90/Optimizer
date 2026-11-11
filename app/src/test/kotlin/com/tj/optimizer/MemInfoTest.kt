package com.tj.optimizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemInfoTest {
    // The shape of /proc/meminfo on a 2 GB Android Go tablet with zram swap (kernel 4.14).
    private val sample = """
        MemTotal:        1953264 kB
        MemFree:          101312 kB
        MemAvailable:     712344 kB
        Buffers:           20244 kB
        Cached:           598700 kB
        SwapCached:         3232 kB
        Active:           776764 kB
        SwapTotal:        524284 kB
        SwapFree:         377912 kB
        HugePages_Total:       0
    """.trimIndent()

    @Test
    fun parsesTheFieldsTheOptimizerNeeds() {
        val m = MemInfo.parse(sample)
        assertNotNull(m)
        m!!
        assertEquals(1953264L, m.totalKb)
        assertEquals(101312L, m.freeKb)
        assertEquals(712344L, m.availableKb)
        assertEquals(598700L, m.cachedKb)
        assertEquals(524284L - 377912L, m.swapUsedKb)
    }

    @Test
    fun availablePercentIsRelativeToTheTotal() {
        assertEquals(36, MemInfo.parse(sample)!!.availablePercent)   // 712344 / 1953264 = 36.5 %
    }

    @Test
    fun aKernelWithoutMemAvailableFallsBackToFreePlusBuffersPlusCached() {
        val m = MemInfo.parse("MemTotal: 1000 kB\nMemFree: 100 kB\nBuffers: 20 kB\nCached: 300 kB\n")!!
        assertEquals(420L, m.availableKb)
    }

    @Test
    fun missingFieldsAreUnknownNotZero() {
        val m = MemInfo.parse("MemTotal: 1000 kB\n")!!
        assertEquals(-1L, m.availableKb)
        assertEquals(-1L, m.swapUsedKb)
        assertEquals(-1, m.availablePercent)
    }

    @Test
    fun garbageIsNullNotACrash() {
        assertNull(MemInfo.parse(""))
        assertNull(MemInfo.parse("this is not meminfo"))
        assertNull(MemInfo.parse("MemTotal: lots kB"))
        assertNull(MemInfo.parse("MemTotal: 0 kB"))
    }

    @Test
    fun formatsSizesTheWayPeopleReadThem() {
        assertEquals("n/a", formatKb(-1))
        assertEquals("640 kB", formatKb(640))
        assertEquals("696 MB", formatKb(712344))      // 695.6 MB, rounded
        assertEquals("512 MB", formatKb(524284))      // the kernel's "512 MB" swap is 524284 kB
        assertEquals("1.86 GB", formatKb(1953264))
    }

    @Test
    fun theReportNamesTheRealNumbersAndSurvivesAMissingMeminfo() {
        val facts = DeviceFacts("0.1.0", "10", 29, "2021-07-05", "alps tb8168p1_bsp", listOf("armeabi-v7a", "armeabi"),
            lowRamDevice = true, memoryClassMb = 128, largeMemoryClassMb = 256, lowMemoryNow = false, lowMemoryThresholdKb = 153600, mem = MemInfo.parse(sample))
        val text = ReportText.build(facts)
        assertTrue(text, text.contains("RAM total:      1.86 GB"))
        assertTrue(text, text.contains("RAM available:  696 MB (36%)"))
        assertTrue(text, text.contains("Swap used:      143 MB of 512 MB"))
        assertTrue(text, text.contains("Low-RAM (Go) device: true"))
        assertTrue(text, text.contains("CPU ABIs: armeabi-v7a, armeabi"))
        val noMem = ReportText.build(DeviceFacts("0.1.0", "10", 29, "?", "x", emptyList(), false, 128, 256, false, 0, null))
        assertTrue(noMem, noMem.contains("could not be read"))
    }
}
