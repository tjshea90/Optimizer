package com.tj.optimizer

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

/** What one storage speed test measured. A negative value means "could not measure". */
class StorageBenchResult(
    val sizeMb: Int,
    val writeMBps: Double,
    val readMBps: Double,
    val randomReadMeanUs: Long,
    val randomReadP95Us: Long,
    val randomReads: Int,
) {
    /** Random 4 KB reads per second, from the mean latency (one thread, one outstanding read). */
    val randomReadIops: Long get() = if (randomReadMeanUs <= 0) -1L else 1_000_000L / randomReadMeanUs
}

/**
 * Storage speed test: a sequential write with fsync, a sequential read, then random 4 KB reads, all on one temporary file that is always deleted.
 * Pure java.io, so it runs in JVM unit tests with a tiny size. [dropCache] must evict the file's pages from the page cache between the phases
 * (on Android: posix_fadvise DONTNEED); without it the read numbers measure RAM, not the flash chip.
 * The caller decides where [file] lives (the app's own cache directory) and runs this OFF the UI thread: it blocks for seconds on a slow eMMC.
 */
object StorageBench {
    private const val CHUNK = 1024 * 1024
    private const val BLOCK = 4096

    fun run(file: File, sizeMb: Int, randomReads: Int, dropCache: (File) -> Unit): StorageBenchResult {
        require(sizeMb >= 1 && randomReads >= 1)
        try {
            val chunk = ByteArray(CHUNK) { (it * 31 + 7).toByte() }

            val w0 = System.nanoTime()
            FileOutputStream(file).use { out ->
                repeat(sizeMb) { out.write(chunk) }
                out.flush()
                out.fd.sync()   // the data must really be on the flash before the clock stops
            }
            val writeSec = seconds(System.nanoTime() - w0)

            dropCache(file)
            val buf = ByteArray(CHUNK)
            val r0 = System.nanoTime()
            file.inputStream().use { input -> while (input.read(buf) > 0) { /* read it all */ } }
            val readSec = seconds(System.nanoTime() - r0)

            dropCache(file)
            val blocks = (sizeMb.toLong() * CHUNK / BLOCK).toInt()
            val rnd = java.util.Random(12345)   // fixed seed: two runs read the same offsets
            val small = ByteArray(BLOCK)
            val micros = LongArray(randomReads)
            RandomAccessFile(file, "r").use { raf ->
                for (i in 0 until randomReads) {
                    raf.seek(rnd.nextInt(blocks).toLong() * BLOCK)
                    val t0 = System.nanoTime()
                    raf.readFully(small)
                    micros[i] = (System.nanoTime() - t0) / 1000L
                }
            }
            micros.sort()
            val mean = micros.sum() / micros.size
            val p95 = micros[((micros.size - 1) * 95) / 100]
            return StorageBenchResult(sizeMb, sizeMb / writeSec, sizeMb / readSec, mean, p95, randomReads)
        } finally {
            file.delete()
        }
    }

    private fun seconds(nanos: Long): Double = maxOf(nanos, 1L) / 1_000_000_000.0
}
