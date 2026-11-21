package com.example.dealayed

import android.app.Activity
import android.content.Intent
import android.view.View

object NotifBell {
    fun setup(activity: Activity, bell: View) {
        bell.setOnClickListener {
            activity.startActivity(Intent(activity, NotificationsActivity::class.java))
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}