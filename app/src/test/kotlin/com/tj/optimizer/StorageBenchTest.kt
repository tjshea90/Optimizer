package com.tj.optimizer

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageBenchTest {
    private fun tempPath(): File = File.createTempFile("optimizer-bench", ".tmp").also { it.delete() }

    @Test
    fun measuresAllThreePhasesAndDropsTheCacheBetweenThem() {
        val f = tempPath()
        var drops = 0
        val r = StorageBench.run(f, 2, 40) { drops++ }
        assertEquals("the cache must be dropped before the read phase and before the random phase", 2, drops)
        assertEquals(2, r.sizeMb)
        assertEquals(40, r.randomReads)
        assertTrue(r.writeMBps > 0.0)
        assertTrue(r.readMBps > 0.0)
        assertTrue(r.randomReadMeanUs >= 0 && r.randomReadP95Us >= r.randomReadMeanUs / 2)
        assertFalse("the temporary file must be gone", f.exists())
    }

    @Test
    fun theFileIsDeletedEvenWhenTheCacheDropFails() {
        val f = tempPath()
        assertThrows(IllegalStateException::class.java) { StorageBench.run(f, 1, 5) { throw IllegalStateException("no fadvise here") } }
        assertFalse(f.exists())
    }

    @Test
    fun readsPerSecondFollowFromTheMeanLatency() {
        assertEquals(1000L, StorageBenchResult(64, 10.0, 50.0, 1000, 2000, 1000).randomReadIops)
        assertEquals(-1L, StorageBenchResult(64, 10.0, 50.0, 0, 0, 1000).randomReadIops)
    }
}
