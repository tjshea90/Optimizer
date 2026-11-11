package com.tj.optimizer

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import java.io.File

/**
 * Reads the device facts from the framework and the kernel. Everything here is allowed for an ordinary app with no permission and changes nothing.
 * Each source is read on its own and guarded on its own: SELinux or a vendor build may hide any one file, and the report then says n/a for that line only.
 */
object SystemReader {
    fun read(context: Context): DeviceFacts {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)

        val data = guard { StatFs(Environment.getDataDirectory().path) }
        val cr = context.contentResolver
        // An absent animation setting means the platform default, 1.0.
        fun scale(name: String): Float = guard { Settings.Global.getFloat(cr, name, 1f) } ?: -1f
        val apps = guard { context.packageManager.getInstalledApplications(0) }
        val pss = guard { Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }.totalPss }
        val thermal = guard { thermalName(context.getSystemService(PowerManager::class.java).currentThermalStatus) } ?: "n/a"

        return DeviceFacts(
            appVersion = BuildConfigLite.versionName(context),
            androidRelease = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            abis = Build.SUPPORTED_ABIS.toList(),
            lowRamDevice = am.isLowRamDevice,
            memoryClassMb = am.memoryClass,
            largeMemoryClassMb = am.largeMemoryClass,
            lowMemoryNow = info.lowMemory,
            lowMemoryThresholdKb = info.threshold / 1024L,
            mem = MemInfo.parse(readFile("/proc/meminfo")),
            uptimeMs = SystemClock.elapsedRealtime(),
            cpu = CpuInfo.parse(readFile("/proc/cpuinfo")),
            swaps = SwapDevice.parseAll(readFile("/proc/swaps")),
            vm = VmStat.parse(readFile("/proc/vmstat")),
            thermal = thermal,
            dataTotalBytes = data?.totalBytes ?: -1L,
            dataFreeBytes = data?.availableBytes ?: -1L,
            animWindow = scale(Settings.Global.WINDOW_ANIMATION_SCALE),
            animTransition = scale(Settings.Global.TRANSITION_ANIMATION_SCALE),
            animAnimator = scale(Settings.Global.ANIMATOR_DURATION_SCALE),
            appsTotal = apps?.size ?: -1,
            appsSystem = apps?.count { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 } ?: -1,
            selfPssKb = pss ?: -1,
        )
    }

    /** Free bytes where the app keeps its cache (the storage test's home); -1 when unknown. */
    fun cacheFreeBytes(context: Context): Long = guard { context.cacheDir.usableSpace } ?: -1L

    private fun readFile(path: String): String = guard { File(path).readText() } ?: ""

    private inline fun <T> guard(block: () -> T): T? = try { block() } catch (e: Exception) { null }
}

/** The app's own version text, read from the installed package (BuildConfig is off to keep the APK lean). */
internal object BuildConfigLite {
    fun versionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (e: Exception) {
        "?"
    }
}
