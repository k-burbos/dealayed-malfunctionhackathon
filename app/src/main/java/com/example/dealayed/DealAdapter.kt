package com.example.dealayed

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DealAdapter(private var deals: List<Deal> = emptyList()) :
    RecyclerView.Adapter<DealAdapter.DealViewHolder>() {

    class DealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.dealImage)
        val ended: TextView = view.findViewById(R.id.endedBadge)
        val category: TextView = view.findViewById(R.id.dealCategory)
        val title: TextView = view.findViewById(R.id.dealTitle)
        val subtitle: TextView = view.findViewById(R.id.dealSubtitle)
        val headline: TextView = view.findViewById(R.id.dealHeadline)
        val expiredOn: TextView = view.findViewById(R.id.dealExpiredOn)
        val roast: TextView = view.findViewById(R.id.dealRoast)
    }

    // Call this whenever the list changes (for example when a chip is tapped)
    fun submit(newDeals: List<Deal>) {
        deals = newDeals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deal, parent, false)
        return DealViewHolder(view)
    }

    override fun getItemCount() = deals.size

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val deal = deals[position]
        val context = holder.itemView.context

        // Photo (falls back to the blue placeholder if the file is missing)
        val bitmap = deal.imagePath?.let {
            loadBitmap(context, it, context.resources.displayMetrics.widthPixels)
        }
        holder.image.setImageBitmap(bitmap)

        holder.ended.text = endedText(deal.daysExpired)
        holder.category.text = deal.category.uppercase()
        holder.title.text = deal.businessName
        holder.subtitle.text = deal.title
        holder.headline.text = deal.headline
        holder.expiredOn.text = deal.expirationDate
        holder.roast.text = deal.roastMessage
        holder.roast.visibility = if (deal.roastMessage.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun endedText(days: Int): String = when {
        days <= 0 -> "Ended today"
        days == 1 -> "Ended yesterday"
        days < 60 -> "Ended $days days ago"
        days < 365 -> "Ended ${days / 30} months ago"
        else -> {
            val years = days / 365
            "Ended $years yr${if (years > 1) "s" else ""} ago"
        }
    }

    // Loads an image from assets, shrunk to screen width so big photos don't use too much memory
    private fun loadBitmap(context: Context, path: String, targetWidth: Int): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetWidth) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
    } catch (e: Exception) {
        null
    }
}