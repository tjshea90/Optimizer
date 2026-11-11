package com.tj.optimizer

import java.io.File
import java.io.FileOutputStream

/** What one storage speed test measured. A negative value means "could not measure". */
class StorageBenchResult(
    val sizeMb: Int,
    val writeMBps: Double,
    val syncWriteMeanUs: Long,
    val syncWriteP95Us: Long,
    val syncWrites: Int,
) {
    /** Small synced writes per second, from the mean latency (one thread, one write at a time); -1 when unknown. */
    val syncWritesPerSecond: Long get() = if (syncWriteMeanUs <= 0) -1L else 1_000_000L / syncWriteMeanUs
}

/**
 * Storage speed test on one temporary file that is always deleted:
 *  1. a sequential write of [sizeMb] MB followed by fsync (the data must really be on the flash before the clock stops);
 *  2. [syncWrites] small writes of 4 KB, each followed by fsync: what a database commit or a settings save costs. On aged eMMC this is a classic cause of lag.
 * It deliberately does NOT time reads: the system keeps a file it just wrote in RAM, and an app cannot evict it (no posix_fadvise, no O_DIRECT in the public API),
 * so a read-back would measure memory, not the flash chip. Pure java.io, so it runs in JVM unit tests with a tiny size.
 * The caller chooses where [file] lives (the app's own cache directory) and runs this OFF the UI thread: it blocks for seconds on a slow eMMC.
 */
object StorageBench {
    private const val CHUNK = 1024 * 1024
    private const val BLOCK = 4096

    fun run(file: File, sizeMb: Int, syncWrites: Int): StorageBenchResult {
        require(sizeMb >= 1 && syncWrites >= 1)
        try {
            val chunk = ByteArray(CHUNK) { (it * 31 + 7).toByte() }
            val w0 = System.nanoTime()
            FileOutputStream(file).use { out ->
                repeat(sizeMb) { out.write(chunk) }
                out.flush()
                out.fd.sync()
            }
            val writeSec = maxOf(System.nanoTime() - w0, 1L) / 1_000_000_000.0

            val block = ByteArray(BLOCK) { (it * 17 + 3).toByte() }
            val micros = LongArray(syncWrites)
            FileOutputStream(file, true).use { out ->
                for (i in 0 until syncWrites) {
                    val t0 = System.nanoTime()
                    out.write(block)
                    out.flush()
                    out.fd.sync()
                    micros[i] = (System.nanoTime() - t0) / 1000L
                }
            }
            micros.sort()
            val mean = micros.sum() / micros.size
            val p95 = micros[((micros.size - 1) * 95) / 100]
            return StorageBenchResult(sizeMb, sizeMb / writeSec, mean, p95, syncWrites)
        } finally {
            file.delete()
        }
    }
}
