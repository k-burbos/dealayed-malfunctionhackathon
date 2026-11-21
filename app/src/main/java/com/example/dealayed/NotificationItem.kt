package com.example.dealayed

import android.view.View
import android.widget.ImageView
import android.widget.TextView

data class NotificationItem(
    val id: String,
    val appLabel: String = "DEALAYED",
    val title: String,
    val body: String,
    val timeAgo: String,
    val iconRes: Int = R.drawable.ic_percent
)

// When the backend is ready, replace the contents of getAll() with a network call.
// The panel and the pop-up don't need to change.
object NotificationRepository {
    fun getAll(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "1",
            title = "Wow, 3 deals gone. Great pacing.",
            body = "Your \"I'll check later\" streak is at 12. Don't stop now.",
            timeAgo = "now"
        ),
        NotificationItem(
            id = "2",
            title = "@paaw saved ₱2,400 today.",
            body = "You saved a screenshot of the deal. Same thing, right?",
            timeAgo = "1m",
            iconRes = R.drawable.ic_person
        ),
        NotificationItem(
            id = "3",
            title = "That 50% off flight? Gone.",
            body = "It lasted 4 hours. You opened the app 6 hours and 1 minute later.",
            timeAgo = "5m"
        )
    )
}

// Fills one notification card. Used by both the panel and the pop-up.
fun NotificationItem.bindTo(view: View) {
    view.findViewById<ImageView>(R.id.notifIcon).setImageResource(iconRes)
    view.findViewById<TextView>(R.id.notifAppLabel).text = appLabel
    view.findViewById<TextView>(R.id.notifTitle).text = title
    view.findViewById<TextView>(R.id.notifBody).text = body
    view.findViewById<TextView>(R.id.notifTime).text = timeAgo
}