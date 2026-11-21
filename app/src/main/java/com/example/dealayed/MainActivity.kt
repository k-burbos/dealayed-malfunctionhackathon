package com.example.dealayed

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
            installSplashScreen()          // <-- add this
            super.onCreate(savedInstanceState)

        val orange = Color.parseColor("#F0502C") // the orange-red from your design

        // Make the status bar the same color so there's no strip at the top
        window.statusBarColor = orange

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(orange) // was Color.BLACK
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(row)

        SplashScreen(row) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }.start()
    }
}