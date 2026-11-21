package com.example.dealayed

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {
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
        val navPadding = (8 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            bottomNav.setPadding(0, navPadding, 0, navPadding + bars.bottom)
            insets
        }

        // ----- Deal list -----
        // Empty for now. Later, the JSON from the backend gets parsed into
        // a List<Deal> and passed to the adapter.
        val deals = emptyList<Deal>()

        val list = findViewById<RecyclerView>(R.id.dealList)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = DealAdapter(deals)

        // ----- Filter chips: tap one to highlight it -----
        val chips = listOf(
            R.id.chipAll, R.id.chipShopping, R.id.chipMovies, R.id.chipFood
        ).map { findViewById<TextView>(it) }

        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { other ->
                    val selected = other == chip
                    other.setBackgroundResource(
                        if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip
                    )
                    other.setTextColor(
                        if (selected) Color.WHITE
                        else ContextCompat.getColor(this, R.color.purple)
                    )
                }
            }
        }
    }
}