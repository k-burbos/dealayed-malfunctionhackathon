package com.example.dealayed

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

object BottomNav {

    enum class Tab { HOME, SEARCH, PROFILE }

    fun setup(
        activity: Activity,
        selected: Tab,
        root: View = activity.window.decorView
    ) {
        style(activity, root, selected, Tab.HOME, R.id.navHomePill, R.id.navHomeIcon, R.id.navHomeLabel)
        style(activity, root, selected, Tab.SEARCH, R.id.navSearchPill, R.id.navSearchIcon, R.id.navSearchLabel)
        style(activity, root, selected, Tab.PROFILE, R.id.navProfilePill, R.id.navProfileIcon, R.id.navProfileLabel)

        root.findViewById<View>(R.id.navHome).setOnClickListener {
            go(activity, selected, Tab.HOME, HomeActivity::class.java)
        }
        root.findViewById<View>(R.id.navSearch).setOnClickListener {
            go(activity, selected, Tab.SEARCH, SearchActivity::class.java)
        }
        root.findViewById<View>(R.id.navProfile).setOnClickListener {
            go(activity, selected, Tab.PROFILE, ProfileActivity::class.java)
        }
    }

    private fun go(activity: Activity, selected: Tab, target: Tab, dest: Class<out Activity>) {
        if (selected == target) return

        val intent = Intent(activity, dest).apply {
            // Reuse the existing screen instead of rebuilding it
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.intent?.getStringExtra(ProfileActivity.EXTRA_EMAIL)?.let {
                putExtra(ProfileActivity.EXTRA_EMAIL, it)
            }
        }
        activity.startActivity(intent)
        fade(activity)
    }

    // Cross-fade between screens
    @Suppress("DEPRECATION")
    private fun fade(activity: Activity) {
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun style(
        activity: Activity, root: View, selected: Tab, tab: Tab,
        pillId: Int, iconId: Int, labelId: Int
    ) {
        val active = selected == tab
        val color = ContextCompat.getColor(
            activity, if (active) R.color.white else R.color.nav_inactive
        )

        val pill = root.findViewById<View>(pillId)
        if (active) pill.setBackgroundResource(R.drawable.bg_nav_active)
        else pill.background = null

        ImageViewCompat.setImageTintList(
            root.findViewById<ImageView>(iconId), ColorStateList.valueOf(color)
        )
        root.findViewById<TextView>(labelId).setTextColor(color)
    }
}