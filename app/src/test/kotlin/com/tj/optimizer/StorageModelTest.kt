package com.tj.optimizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageModelTest {
    // The owner's tablet on 2026-11-11: 996 MB free of 27.75 GB.
    private val total = 29_796_335_616L
    private val free = 1_044_381_696L
    private fun gb(n: Double) = (n * 1024 * 1024 * 1024).toLong()
    private fun mb(n: Long) = n * 1024 * 1024
    private fun app(name: String, code: Long, data: Long, cache: Long) = AppStorage(name, name.uppercase(), code, data, cache)

    @Test
    fun freePercentIsRelativeToTheTotalAndSurvivesUnknowns() {
        assertEquals(3, StorageModel.freePercent(total, free))
        assertEquals(50, StorageModel.freePercent(100, 50))
        assertEquals(100, StorageModel.freePercent(100, 500))   // clamped
        assertEquals(-1, StorageModel.freePercent(0, 10))
        assertEquals(-1, StorageModel.freePercent(100, -1))
    }

    @Test
    fun theVerdictNamesTheNumbersAndTheTier() {
        assertEquals("VERY LOW: 996 MB free of 27.75 GB (3%). Updates, caches and temporary files have almost no room.", StorageModel.verdict(total, free))
        assertTrue(StorageModel.verdict(100, 7).startsWith("LOW: "))
        assertTrue(StorageModel.verdict(100, 15).startsWith("Getting full: "))
        assertTrue(StorageModel.verdict(100, 50).startsWith("OK: "))
        assertEquals("Free space: unknown.", StorageModel.verdict(-1, -1))
    }

    @Test
    fun theOneLineReportOnlyWarnsBelowTenPercent() {
        assertEquals("VERY LOW", StorageModel.warningWord(total, free))
        assertEquals("LOW", StorageModel.warningWord(100, 9))
        assertEquals("", StorageModel.warningWord(100, 10))
        assertEquals("", StorageModel.warningWord(-1, -1))
    }

    @Test
    fun theBiggestAppsComeFirstAndTheListIsCut() {
        val apps = listOf(app("small", 1, 1, 1), app("huge", mb(100), gb(2.0), mb(200)), app("middle", mb(50), mb(500), mb(10)))
        assertEquals(listOf("huge", "middle"), StorageModel.topApps(apps, 2).map { it.packageName })
        assertEquals(3, StorageModel.topApps(apps, 10).size)
        assertTrue(StorageModel.topApps(emptyList(), 5).isEmpty())
    }

    @Test
    fun anAppRowShowsTotalCodeDataAndCache() {
        assertEquals("2.23 GB: code 40 MB, data 2.00 GB, cache 200 MB", StorageModel.rowDetail(app("chat", mb(40), gb(2.0), mb(200))))
    }

    @Test
    fun theHeaderShowsTheVerdictTheKindsAndWhatTheAppsAddUpTo() {
        val s = StorageSummary(total, free, imageBytes = gb(1.0), videoBytes = gb(3.0), audioBytes = mb(512), sharedAppBytes = gb(1.5),
            apps = listOf(app("a", gb(1.0), gb(2.0), gb(0.5)), app("b", gb(1.0), gb(1.0), gb(0.5))))
        val h = StorageModel.header(s)
        assertTrue(h, h.startsWith("VERY LOW: 996 MB free of 27.75 GB (3%)"))
        assertTrue(h, h.contains("On shared storage: images 1.00 GB, video 3.00 GB, audio 512 MB, app files 1.50 GB"))
        assertTrue(h, h.contains("2 apps counted: 6.00 GB in all (code 2.00 GB, data 3.00 GB, cache 1.00 GB)"))
    }

    @Test
    fun theCopiedTextHasTheHeaderAndTheBiggestAppsWithTheirPackageNames() {
        val s = StorageSummary(total, free, apps = listOf(app("small", 1, 1, 1), app("com.example.chat", mb(40), gb(2.0), mb(200))))
        val t = StorageModel.copyText(s, 5)
        assertTrue(t, t.startsWith("VERY LOW: 996 MB free of 27.75 GB (3%)"))
        assertTrue(t, t.contains("Biggest apps (code + data + cache):"))
        assertTrue(t, t.contains("1. COM.EXAMPLE.CHAT (com.example.chat) 2.23 GB: code 40 MB, data 2.00 GB, cache 200 MB"))
        assertTrue(t, t.indexOf("COM.EXAMPLE.CHAT") < t.indexOf("SMALL"))
        assertFalse(t, StorageModel.copyText(StorageSummary(total, free), 5).contains("Biggest apps"))
    }

    @Test
    fun withoutUsageAccessTheHeaderSaysSoInsteadOfInventingNumbers() {
        val h = StorageModel.header(StorageSummary(total, free, needsUsageAccess = true))
        assertTrue(h, h.contains("Usage access"))
        assertTrue(h, h.startsWith("VERY LOW"))
        assertFalse(h, h.contains("apps counted"))
    }
}
