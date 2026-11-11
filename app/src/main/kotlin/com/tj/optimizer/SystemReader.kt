package com.tj.optimizer

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.io.File

/** Reads the device facts from the framework and the kernel. Everything here is allowed for an ordinary app, with no permission. */
object SystemReader {
    fun read(context: Context): DeviceFacts {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val meminfo = try {
            MemInfo.parse(File("/proc/meminfo").readText())
        } catch (e: Exception) {
            null
        }
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
            mem = meminfo,
        )
    }
}

/** The app's own version text, read from the installed package (BuildConfig is off to keep the APK lean). */
internal object BuildConfigLite {
    fun versionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (e: Exception) {
        "?"
    }
}
