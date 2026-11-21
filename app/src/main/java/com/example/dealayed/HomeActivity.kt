package com.example.dealayed

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

class HomeActivity : AppCompatActivity() {

    private lateinit var allDeals: List<Deal>
    private lateinit var adapter: DealAdapter
    private lateinit var list: RecyclerView
    private val chips = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_home)

        // Dark clock/battery icons on the beige background
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        // Leave room for the status bar (top) and gesture bar (bottom)
        val root = findViewById<View>(R.id.root)
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val navPadding = dp(8)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            bottomNav.setPadding(0, navPadding, 0, navPadding + bars.bottom)
            insets
        }

        // ----- Load deals from the JSON -----
        allDeals = DealRepository.loadDeals(this)

        adapter = DealAdapter(allDeals)
        list = findViewById(R.id.dealList)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        // ----- Build chips: "All" + every category found in the data -----
        val labels = listOf("All") + allDeals.map { it.category }.distinct()
        val container = findViewById<LinearLayout>(R.id.chipContainer)

        labels.forEach { label ->
            val chip = makeChip(label)
            chip.setOnClickListener { selectChip(chip, label) }
            chips.add(chip)
            container.addView(chip)
        }
        selectChip(chips.first(), "All")
        BottomNav.setup(this, BottomNav.Tab.HOME)
    }

    private fun selectChip(selected: TextView, label: String) {
        chips.forEach { chip ->
            val isSelected = chip == selected
            chip.setBackgroundResource(
                if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip
            )
            chip.setTextColor(
                if (isSelected) Color.WHITE
                else ContextCompat.getColor(this, R.color.purple)
            )
        }

        val filtered = if (label == "All") allDeals else allDeals.filter { it.category == label }
        adapter.submit(filtered)
        list.scrollToPosition(0)
    }

    private fun makeChip(label: String): TextView = TextView(this).apply {
        text = label
        typeface = ResourcesCompat.getFont(context, R.font.lexend)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        gravity = Gravity.CENTER
        setPadding(dp(28), 0, dp(28), 0)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)
        ).apply { marginEnd = dp(10) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

}