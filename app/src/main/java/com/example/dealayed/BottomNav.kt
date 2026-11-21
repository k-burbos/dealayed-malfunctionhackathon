package com.example.dealayed

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

object BottomNav {

    enum class Tab { HOME, SEARCH, PROFILE }

    fun setup(activity: Activity, selected: Tab) {
        style(activity, selected, Tab.HOME, R.id.navHomePill, R.id.navHomeIcon, R.id.navHomeLabel)
        style(activity, selected, Tab.SEARCH, R.id.navSearchPill, R.id.navSearchIcon, R.id.navSearchLabel)
        style(activity, selected, Tab.PROFILE, R.id.navProfilePill, R.id.navProfileIcon, R.id.navProfileLabel)

        activity.findViewById<View>(R.id.navHome).setOnClickListener {
            if (selected != Tab.HOME) {
                // Brings the existing Home screen back and closes the screens above it
                val intent = Intent(activity, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                activity.startActivity(intent)
                activity.overridePendingTransition(0, 0)
            }
        }

        activity.findViewById<View>(R.id.navSearch).setOnClickListener {
            if (selected != Tab.SEARCH) {
                activity.startActivity(Intent(activity, SearchActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        // Profile does nothing yet
    }

    private fun style(
        activity: Activity, selected: Tab, tab: Tab,
        pillId: Int, iconId: Int, labelId: Int
    ) {
        val active = selected == tab
        val color = ContextCompat.getColor(
            activity, if (active) R.color.white else R.color.nav_inactive
        )

        val pill = activity.findViewById<View>(pillId)
        if (active) pill.setBackgroundResource(R.drawable.bg_nav_active)
        else pill.background = null

        ImageViewCompat.setImageTintList(
            activity.findViewById<ImageView>(iconId), ColorStateList.valueOf(color)
        )
        activity.findViewById<TextView>(labelId).setTextColor(color)
    }
}