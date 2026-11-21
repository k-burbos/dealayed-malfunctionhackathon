package com.example.dealayed

import android.animation.ValueAnimator // NEW
import android.graphics.Color
import android.util.TypedValue
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlin.random.Random
import androidx.core.animation.doOnEnd                  // NEW
import kotlin.math.PI                                   // NEW
import kotlin.math.abs                                  // NEW
import kotlin.math.sin                                     //NEW
import kotlin.math.exp                                   // NEW
import android.view.ViewGroup

class SplashScreen(
    private val row: LinearLayout,
    private val word: String = "DEALAYED",
    private val onFinished: () -> Unit
) {
    private val letterGap = 250L
    private val rushDuration = 300L
    private val recoilOut = 90L
    private val recoilSettle = 350L
    private val recoilDistance = 30f
    private val bumpDistance = 14f
    private val lateWait = 10L
    private val crawlFraction = 0.35f
    private val crawlDuration = 1000L
    private val hesitate = 100L
    private val chargeDuration = 180L

    private val bounceDistance = 60f     // how far the first rebound goes (dp)
    private val bounceCount = 3          // how many times it hits the wall
    private val bounceDuration = 1000L   // total time of all the bounces
    private val endPause = 400L

    fun start() {
        val context = row.context
        val screenW = context.resources.displayMetrics.widthPixels.toFloat()

        row.clipChildren = false

        // NEW: stop every parent layout from cropping the bouncing D
        var parent = row.parent
        while (parent is ViewGroup) {
            parent.clipChildren = false
            parent.clipToPadding = false
            parent = parent.parent
        }

        val letters = word.map { ch ->
            TextView(context).apply {
                text = ch.toString()
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
                typeface = ResourcesCompat.getFont(context, R.font.lexend)
                translationX = screenW
            }.also { row.addView(it) }
        }

        letters.dropLast(1).forEachIndexed { i, tv ->
            tv.animate()
                .translationX(0f)
                .setStartDelay(i * letterGap)
                .setDuration(rushDuration)
                .setInterpolator(AccelerateInterpolator(1.5f))
                .withEndAction {
                    recoil(tv)
                    letters.getOrNull(i - 1)?.let { bump(it) }
                }
                .start()
        }

        val lateStart = (letters.size - 2) * letterGap +
                rushDuration + recoilOut + recoilSettle + lateWait
        lateLetter(letters, lateStart, screenW)
    }

    private fun lateLetter(
        letters: List<TextView>,
        startDelay: Long,
        screenW: Float,
    ) {
        val d = letters.last()
        d.animate()
            .translationX(screenW * crawlFraction)
            .setStartDelay(startDelay)
            .setDuration(crawlDuration)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                d.animate()
                    .translationX(0f)
                    .setStartDelay(hesitate)
                    .setDuration(chargeDuration)
                    .setInterpolator(AccelerateInterpolator(3f))
                    .withEndAction { bounceOffWall(d) } //changed crash(letters, screenW, screenH)
                    .start()
            }
            .start()
    }

    private fun bounceOffWall(d: TextView) {
        val distance = bounceDistance * d.resources.displayMetrics.density

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = bounceDuration
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                val shrink = exp(-3f * t)                              // each bounce gets smaller
                val wave = abs(sin(PI * bounceCount * t)).toFloat()    // 0 = touching the wall
                d.translationX = distance * shrink * wave
            }
            doOnEnd { d.postDelayed({ onFinished() }, endPause) }
            start()
        }
    }
    private fun recoil(tv: TextView) {
        tv.animate()
            .translationX(recoilDistance)
            .setStartDelay(0)
            .setDuration(recoilOut)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                tv.animate()
                    .translationX(0f)
                    .setDuration(recoilSettle)
                    .setInterpolator(BounceInterpolator())
                    .start()
            }
            .start()
    }

    private fun bump(tv: TextView) {
        tv.animate()
            .translationX(-bumpDistance)
            .setStartDelay(0)
            .setDuration(60)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                tv.animate()
                    .translationX(0f)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator(3f))
                    .start()
            }
            .start()
    }
}