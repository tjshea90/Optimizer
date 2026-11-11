package com.tj.optimizer

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * The first screen: a plain-text snapshot of this tablet's RAM, swap and limits.
 * Framework Views only, built in code, no animations: on a 2 GB Go tablet the app is judged on size, RAM and snappiness before features.
 */
class MainActivity : Activity() {
    private lateinit var reportView: TextView
    private var report: String = ""

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
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
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
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            for (b in listOf(refresh, copy)) {
                // 56 dp tall: comfortable to hit on a 600x973 @170 dpi tablet at font scale 1.2
                addView(b, LinearLayout.LayoutParams(0, dp(56), 1f).apply { setMargins(dp(4), dp(8), dp(4), 0) })
            }
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            addView(title)
            addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = dp(12) })
            addView(buttons)
        }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        report = ReportText.build(SystemReader.read(this))
        reportView.text = report
    }

    private fun copyReport() {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), report))
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
    }
}
