package com.example.dealayed

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

data class Deal(
    val id: String,
    val category: String,
    val businessName: String,
    val title: String,
    val headline: String,        // "50% OFF" or "₱702"
    val expirationDate: String,  // "Feb 13, 2026"
    val daysExpired: Int,
    val imagePath: String?,      // path inside assets
    val roastMessage: String
)

object DealRepository {
    private const val BASE = "expired_deals/"

    fun loadDeals(context: Context): List<Deal> {
        return try {
            val text = context.assets.open(BASE + "expired_deals.json")
                .bufferedReader().use { it.readText() }
            val items = JSONObject(text).getJSONArray("items")

            val deals = mutableListOf<Deal>()
            for (i in 0 until items.length()) {
                val o = items.getJSONObject(i)

                // One of your entries says "10.jpg" instead of "img/10.jpg", so fix that here
                val rawImage = o.str("imageUrl")
                val imagePath = when {
                    rawImage.isEmpty() -> null
                    rawImage.startsWith("img/") -> BASE + rawImage
                    else -> BASE + "img/" + rawImage
                }

                deals.add(
                    Deal(
                        id = o.str("id"),
                        category = o.str("category", "Other"),
                        businessName = o.str("businessName"),
                        title = o.str("title"),
                        headline = headline(o),
                        expirationDate = prettyDate(o.str("expirationDate")),
                        daysExpired = if (o.isNull("daysExpired")) 0 else o.getInt("daysExpired"),
                        imagePath = imagePath,
                        roastMessage = o.str("roastMessage")
                    )
                )
            }
            // Most recently expired first
            deals.sortedBy { it.daysExpired }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // org.json turns JSON null into the text "null", so we check for it ourselves
    private fun JSONObject.str(key: String, default: String = ""): String =
        if (isNull(key)) default else getString(key)

    private fun headline(o: JSONObject): String {
        if (!o.isNull("discountPercent")) return "${o.getInt("discountPercent")}% OFF"
        if (!o.isNull("discountedPrice")) {
            val p = o.getDouble("discountedPrice")
            val shown = if (p % 1.0 == 0.0) {
                String.format(Locale.US, "%,d", p.toLong())
            } else {
                String.format(Locale.US, "%.2f", p)
            }
            return "₱$shown"
        }
        return "    -   "
    }

    private fun prettyDate(iso: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(date!!)
    } catch (e: Exception) {
        iso
    }
}