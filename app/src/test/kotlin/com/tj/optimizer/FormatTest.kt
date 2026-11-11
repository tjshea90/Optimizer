package com.tj.optimizer

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun bytesReadLikeKilobytes() {
        assertEquals("n/a", formatBytes(-1))
        assertEquals("5.50 GB", formatBytes(5_905_580_032L))
        assertEquals("11.00 GB", formatBytes(11_811_160_064L))
        assertEquals("1 MB", formatBytes(1_500_000L))
    }

    @Test
    fun durationsReadAsDaysHoursMinutes() {
        assertEquals("n/a", formatDuration(-1))
        assertEquals("0 min", formatDuration(0))
        assertEquals("42 min", formatDuration(42 * 60_000L))
        assertEquals("5 h 3 min", formatDuration(5 * 3_600_000L + 3 * 60_000L))
        assertEquals("3 d 4 h 12 min", formatDuration(3 * 86_400_000L + 4 * 3_600_000L + 12 * 60_000L))
    }

    @Test
    fun thermalStatusesHaveWords() {
        assertEquals("NONE (cool)", thermalName(0))
        assertEquals("MODERATE", thermalName(2))
        assertEquals("SEVERE", thermalName(3))
        assertEquals("SHUTDOWN", thermalName(6))
        assertEquals("n/a", thermalName(99))
        assertEquals("n/a", thermalName(-1))
    }

    @Test
    fun animationScalesHaveOneDecimal() {
        assertEquals("0.0", formatScale(0f))
        assertEquals("0.5", formatScale(0.5f))
        assertEquals("1.0", formatScale(1f))
        assertEquals("n/a", formatScale(-1f))
    }
}
