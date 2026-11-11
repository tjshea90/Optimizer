package com.tj.optimizer

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageBenchTest {
    private fun tempPath(): File = File.createTempFile("optimizer-bench", ".tmp").also { it.delete() }

    @Test
    fun measuresTheSequentialWriteAndTheSmallSyncedWritesAndDeletesTheFile() {
        val f = tempPath()
        val r = StorageBench.run(f, 2, 20)
        assertEquals(2, r.sizeMb)
        assertEquals(20, r.syncWrites)
        assertTrue(r.writeMBps > 0.0)
        assertTrue(r.syncWriteMeanUs >= 0 && r.syncWriteP95Us >= 0)
        assertFalse("the temporary file must be gone", f.exists())
    }

    @Test
    fun theFileIsDeletedEvenWhenTheRunFails() {
        // a directory that does not exist: opening the file fails, and nothing may be left behind
        val f = File(File(System.getProperty("java.io.tmpdir"), "optimizer-no-such-dir-${System.nanoTime()}"), "x.tmp")
        try {
            StorageBench.run(f, 1, 5)
        } catch (e: java.io.IOException) {
            // expected
        }
        assertFalse(f.exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun aTestWithoutSizeIsRefused() {
        StorageBench.run(tempPath(), 0, 5)
    }

    @Test
    fun syncedWritesPerSecondFollowFromTheMeanLatency() {
        assertEquals(250L, StorageBenchResult(64, 10.0, 4000, 9000, 200).syncWritesPerSecond)
        assertEquals(-1L, StorageBenchResult(64, 10.0, 0, 0, 200).syncWritesPerSecond)
    }
}
