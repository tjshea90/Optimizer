package com.tj.optimizer

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Typeface
import android.os.Bundle
import android.system.Os
import android.system.OsConstants
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileInputStream

/**
 * The first screen: a plain-text snapshot of this tablet's RAM, swap, storage and limits, and one on-demand storage speed test.
 * Framework Views only, built in code, no animations: on a 2 GB Go tablet the app is judged on size, RAM and snappiness before features.
 * It only READS the system (BOUNDARIES.md): the one thing it writes is the test's temporary file in its own cache directory.
 */
class MainActivity : Activity() {
    private lateinit var reportView: TextView
    private lateinit var benchButton: Button
    private var report: String = ""
    private var bench: StorageBenchResult? = null
    private var benchNote: String? = null
    private var benchRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density + 0.5f).toInt()

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(getColor(R.color.accent))
            typeface = Typeface.DEFAULT_BOLD
        }
        reportView = TextView(this).apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(getColor(R.color.text))
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply { addView(reportView) }
        val refresh = Button(this).apply {
            text = getString(R.string.refresh)
            setOnClickListener { refresh() }
        }
        val copy = Button(this).apply {
            text = getString(R.string.copy_report)
            setOnClickListener { copyReport() }
        }
        benchButton = Button(this).apply {
            text = getString(R.string.storage_test)
            setOnClickListener { runBench() }
        }
        // 56 dp tall: comfortable to hit on a 600x973 @170 dpi tablet at font scale 1.2
        fun rowOf(vararg buttons: Button) = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            for (b in buttons) addView(b, LinearLayout.LayoutParams(0, dp(56), 1f).apply { setMargins(dp(4), dp(8), dp(4), 0) })
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            addView(title)
            addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = dp(12) })
            addView(rowOf(refresh, copy))
            addView(rowOf(benchButton))
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        report = ReportText.build(SystemReader.read(this), bench, benchNote)
        reportView.text = report
    }

    private fun copyReport() {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), report))
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    /** Runs the storage test on its own thread; the UI stays responsive and the button stays off until it ends. */
    private fun runBench() {
        if (benchRunning) return
        val free = SystemReader.cacheFreeBytes(this)
        if (free in 0 until MIN_FREE_FOR_BENCH) {
            bench = null
            benchNote = "not run: less than 256 MB free in this app's cache area"
            refresh()
            return
        }
        benchRunning = true
        benchButton.isEnabled = false
        bench = null
        benchNote = "running, about 10 to 30 seconds (the tablet may feel slow meanwhile)..."
        refresh()
        val dir = cacheDir
        Thread {
            var result: StorageBenchResult? = null
            var note: String? = null
            try {
                result = StorageBench.run(File(dir, "storage-bench.tmp"), BENCH_MB, BENCH_RANDOM_READS) { dropPageCache(it) }
            } catch (e: Exception) {
                note = "failed: ${e.javaClass.simpleName}"
            }
            runOnUiThread {
                benchRunning = false
                if (isDestroyed) return@runOnUiThread
                benchButton.isEnabled = true
                bench = result
                benchNote = note
                refresh()
            }
        }.start()
    }

    /** Evicts the test file's pages from the page cache (posix_fadvise DONTNEED); best effort, without it the read numbers measure RAM. */
    private fun dropPageCache(file: File) {
        try {
            FileInputStream(file).use { Os.posix_fadvise(it.fd, 0L, 0L, OsConstants.POSIX_FADV_DONTNEED) }
        } catch (e: Exception) {
            // not fatal: the report then simply shows faster reads than the flash chip really gives
        }
    }

    private companion object {
        const val BENCH_MB = 64
        const val BENCH_RANDOM_READS = 1000
        const val MIN_FREE_FOR_BENCH = 256L * 1024 * 1024
    }
}
