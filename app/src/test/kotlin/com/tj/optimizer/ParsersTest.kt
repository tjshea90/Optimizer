package com.tj.optimizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParsersTest {
    private fun cpuinfo(cores: Int, part: String) = buildString {
        for (i in 0 until cores) {
            appendLine("processor\t: $i")
            appendLine("model name\t: ARMv7 Processor rev 4 (v7l)")
            appendLine("Features\t: half thumb fastmult vfp edsp neon vfpv3 tls vfpv4 idiva idivt vfpd32 lpae evtstrm aes pmull sha1 sha2 crc32")
            appendLine("CPU part\t: $part")
            appendLine()
        }
    }

    @Test
    fun cpuInfoCountsCoresAndNamesTheCore() {
        val c = CpuInfo.parse(cpuinfo(4, "0xd03"))
        assertNotNull(c)
        c!!
        assertEquals(4, c.cores)
        assertEquals("Cortex-A53", c.partName)
        assertTrue(c.features.startsWith("half thumb"))
        assertTrue(c.features.contains("neon"))
    }

    @Test
    fun anUnknownCorePartKeepsItsRawId() {
        assertEquals("0xd99", CpuInfo.parse(cpuinfo(1, "0xd99"))!!.partName)
    }

    @Test
    fun cpuInfoOfNothingIsNull() {
        assertNull(CpuInfo.parse(""))
        assertNull(CpuInfo.parse("Hardware : something\nRevision : 0000"))
    }

    @Test
    fun swapDevicesAreReadFromProcSwaps() {
        val text = "Filename\t\t\t\tType\t\tSize\tUsed\tPriority\n/dev/block/zram0                        partition\t524284\t146372\t-2\n"
        val swaps = SwapDevice.parseAll(text)
        assertEquals(1, swaps.size)
        assertEquals("/dev/block/zram0", swaps[0].name)
        assertTrue(swaps[0].isZram)
        assertEquals(524284L, swaps[0].sizeKb)
        assertEquals(146372L, swaps[0].usedKb)
    }

    @Test
    fun noSwapMeansAnEmptyList() {
        assertTrue(SwapDevice.parseAll("Filename Type Size Used Priority\n").isEmpty())
        assertTrue(SwapDevice.parseAll("").isEmpty())
        assertTrue(SwapDevice.parseAll("header\nnot a swap line at all\n").isEmpty())
        assertFalse(SwapDevice("/swapfile", "file", 1, 0).isZram)
    }

    private val vmstat = """
        nr_free_pages 25328
        pgmajfault 123456
        pswpin 1234
        pswpout 2345
        workingset_refault 3456
        allocstall_normal 10
        allocstall_movable 5
        not_a_counter lots
    """.trimIndent()

    @Test
    fun vmStatCountersAreLookedUpByName() {
        val v = VmStat.parse(vmstat)
        assertNotNull(v)
        v!!
        assertEquals(123456L, v["pgmajfault"])
        assertEquals(-1L, v["no_such_counter"])
        assertEquals(15L, v.sumPrefix("allocstall"))
        assertEquals(-1L, v.sumPrefix("zzz"))
    }

    @Test
    fun vmStatOfNothingIsNull() {
        assertNull(VmStat.parse(""))
        assertNull(VmStat.parse("nothing useful here"))
    }

    @Test
    fun countersBecomeRatesPerHourOfUptime() {
        assertEquals(3600L, VmStat.perHour(7200, 7_200_000))   // 7200 events in 2 hours
        assertEquals(12345L, VmStat.perHour(123456, 36_000_000))  // 10 hours
        assertEquals(-1L, VmStat.perHour(5, 30_000))             // just booted: no rate yet
        assertEquals(-1L, VmStat.perHour(-1, 100_000_000))       // unknown counter
    }
}
