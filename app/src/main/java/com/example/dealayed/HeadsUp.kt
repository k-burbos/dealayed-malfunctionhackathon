package com.example.dealayed

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout

object HeadsUp {

    fun show(activity: Activity, item: NotificationItem, durationMs: Long = 4000L) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        val density = activity.resources.displayMetrics.density
        val topMarginPx = (48 * density).toInt()

        val view = LayoutInflater.from(activity).inflate(R.layout.item_notification, root, false)
        item.bindTo(view)
        view.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ).apply {
            topMargin = topMarginPx
            marginStart = (12 * density).toInt()
            marginEnd = (12 * density).toInt()
        }
        root.addView(view)

        // Start fully above the screen, then slide down
        view.post {
            view.translationY = -(view.height + topMarginPx).toFloat()
            view.animate().translationY(0f).setDuration(300).start()
        }

        fun dismiss() {
            if (view.parent != root) return // already removed
            view.animate()
                .translationY(-(view.height + topMarginPx).toFloat())
                .alpha(0f)
                .setDuration(250)
                .withEndAction { root.removeView(view) }
                .start()
        }

        view.setOnClickListener { dismiss() }
        view.postDelayed({ dismiss() }, durationMs)
    }
}