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
            if (selected != Tab.HOME) {
                val intent = Intent(activity, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                activity.startActivity(intent)
                fade(activity)
            }
        }

        root.findViewById<View>(R.id.navSearch).setOnClickListener {
            if (selected != Tab.SEARCH) {
                activity.startActivity(Intent(activity, SearchActivity::class.java))
                fade(activity)
            }
        }

        root.findViewById<View>(R.id.navProfile).setOnClickListener {
            if (selected != Tab.PROFILE) {
                val intent = Intent(activity, ProfileActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    activity.intent?.getStringExtra(ProfileActivity.EXTRA_EMAIL)?.let {
                        putExtra(ProfileActivity.EXTRA_EMAIL, it)
                    }
                }
                activity.startActivity(intent)
                fade(activity)
            }
        }
    }

    // Cross-fade between screens
    @Suppress("DEPRECATION")
    private fun fade(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out
            )
        } else {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
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