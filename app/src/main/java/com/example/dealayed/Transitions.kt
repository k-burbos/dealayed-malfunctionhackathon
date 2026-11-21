package com.example.dealayed

import android.app.Activity
import android.os.Build

fun Activity.applyFadeTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        // New API (Android 14+): set both opening and closing
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}