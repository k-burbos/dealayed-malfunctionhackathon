package com.example.dealayed

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// One deal = one card
data class Deal(
    val category: String,
    val title: String,
    val subtitle: String,
    val nowPrice: String,
    val missedPrice: String,
    val endedText: String,
    val imageRes: Int? = null   // optional photo from res/drawable
)

class DealAdapter(private val deals: List<Deal>) :
    RecyclerView.Adapter<DealAdapter.DealViewHolder>() {

    class DealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.dealImage)
        val ended: TextView = view.findViewById(R.id.endedBadge)
        val category: TextView = view.findViewById(R.id.dealCategory)
        val title: TextView = view.findViewById(R.id.dealTitle)
        val subtitle: TextView = view.findViewById(R.id.dealSubtitle)
        val now: TextView = view.findViewById(R.id.dealNow)
        val missed: TextView = view.findViewById(R.id.dealMissed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deal, parent, false)
        return DealViewHolder(view)
    }

    override fun getItemCount() = deals.size

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val deal = deals[position]

        deal.imageRes?.let { holder.image.setImageResource(it) }
        holder.ended.text = deal.endedText
        holder.category.text = deal.category
        holder.title.text = deal.title
        holder.subtitle.text = deal.subtitle
        holder.now.text = deal.nowPrice
        holder.missed.text = deal.missedPrice

        // Strikethrough on the missed price
        holder.missed.paintFlags = holder.missed.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }
}