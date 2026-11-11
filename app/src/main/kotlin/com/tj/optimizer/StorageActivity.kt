package com.tj.optimizer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * "What uses my storage": the free space, the totals per kind and the apps using the most room. READ-ONLY: it never deletes or clears anything.
 * Tapping an app opens that app's own Android settings page, where Storage > Clear cache is the safe way to free space.
 * Framework Views built in code, no animations.
 */
class StorageActivity : Activity() {
    private lateinit var header: TextView
    private lateinit var accessButton: Button
    private lateinit var copyButton: Button
    private lateinit var rows: LinearLayout
    private var lastSummary: StorageSummary? = null
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density + 0.5f).toInt()

        val title = TextView(this).apply {
            text = getString(R.string.storage_title)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(getColor(R.color.accent))
            typeface = Typeface.DEFAULT_BOLD
        }
        header = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(getColor(R.color.text))
            setPadding(0, dp(8), 0, dp(8))
        }
        accessButton = Button(this).apply {
            text = getString(R.string.grant_usage_access)
            visibility = android.view.View.GONE
            setOnClickListener { openUsageAccessSettings() }
        }
        copyButton = Button(this).apply {
            text = getString(R.string.copy_list)
            visibility = android.view.View.GONE
            setOnClickListener { copyList() }
        }
        val hint = TextView(this).apply {
            text = getString(R.string.storage_hint)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(getColor(R.color.text_muted))
            setPadding(0, dp(4), 0, dp(8))
        }
        rows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(rows) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            addView(title)
            addView(header)
            addView(accessButton, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
            addView(copyButton, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
            addView(hint)
            addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        if (loading) return
        loading = true
        header.text = getString(R.string.storage_reading)
        val appContext = applicationContext
        Thread {
            val summary = StorageReader.read(appContext) { done, total ->
                runOnUiThread { if (!isDestroyed) header.text = "Reading app sizes: $done of $total ..." }
            }
            runOnUiThread {
                loading = false
                if (!isDestroyed) show(summary)
            }
        }.start()
    }

    private fun show(s: StorageSummary) {
        lastSummary = s
        header.text = StorageModel.header(s)
        accessButton.visibility = if (s.needsUsageAccess) android.view.View.VISIBLE else android.view.View.GONE
        copyButton.visibility = if (s.apps.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        rows.removeAllViews()
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density + 0.5f).toInt()
        for (app in StorageModel.topApps(s.apps, TOP_N)) {
            val row = TextView(this).apply {
                text = app.label + "\n" + StorageModel.rowDetail(app)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(getColor(R.color.text))
                minimumHeight = dp(56)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                // a plain pressed colour, no ripple: nothing animates in this app
                background = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(0x33FFFFFF))
                    addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
                }
                setOnClickListener { openAppSettings(app.packageName) }
            }
            rows.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun openAppSettings(packageName: String) {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUsageAccessSettings() {
        try {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:$packageName")))
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private companion object {
        const val TOP_N = 40
    }
}
