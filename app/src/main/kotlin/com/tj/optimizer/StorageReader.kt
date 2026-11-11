package com.tj.optimizer

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Process
import android.os.storage.StorageManager

/**
 * Reads the storage numbers from Android's StorageStatsManager. Read-only. The totals need no permission; the per-app and per-kind numbers need the
 * "Usage access" special permission (PACKAGE_USAGE_STATS), which the owner switches on once in Settings. Without it Android throws SecurityException
 * and [StorageSummary.needsUsageAccess] is set, so the screen can say so instead of failing.
 * Run this OFF the UI thread: asking about every installed app takes seconds on this tablet. [onProgress] gets (done, total) every few apps.
 */
object StorageReader {
    fun read(context: Context, onProgress: (Int, Int) -> Unit = { _, _ -> }): StorageSummary {
        val ssm = context.getSystemService(StorageStatsManager::class.java)
        val uuid = StorageManager.UUID_DEFAULT
        val user = Process.myUserHandle()
        val total = try { ssm.getTotalBytes(uuid) } catch (e: Exception) { -1L }
        val free = try { ssm.getFreeBytes(uuid) } catch (e: Exception) { -1L }

        var needsAccess = false
        var image = -1L
        var video = -1L
        var audio = -1L
        var sharedApp = -1L
        try {
            val ext = ssm.queryExternalStatsForUser(uuid, user)
            image = ext.imageBytes
            video = ext.videoBytes
            audio = ext.audioBytes
            sharedApp = ext.appBytes
        } catch (e: SecurityException) {
            needsAccess = true
        } catch (e: Exception) {
            // unreadable on this ROM: leave the kinds unknown
        }

        val apps = ArrayList<AppStorage>()
        if (!needsAccess) {
            val pm = context.packageManager
            val installed = try { pm.getInstalledApplications(0) } catch (e: Exception) { emptyList() }
            for ((i, info) in installed.withIndex()) {
                try {
                    val st = ssm.queryStatsForPackage(uuid, info.packageName, user)
                    val label = try { pm.getApplicationLabel(info).toString() } catch (e: Exception) { info.packageName }
                    apps.add(AppStorage(info.packageName, label, st.appBytes, st.dataBytes, st.cacheBytes))
                } catch (e: SecurityException) {
                    needsAccess = true
                    break
                } catch (e: Exception) {
                    // this one package cannot be measured (removed meanwhile, or an Android quirk): skip it
                }
                if (i % 10 == 0) onProgress(i, installed.size)
            }
        }
        return StorageSummary(total, free, image, video, audio, sharedApp, apps, needsAccess)
    }
}
