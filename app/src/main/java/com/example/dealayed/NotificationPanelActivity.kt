package com.example.dealayed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationPanelActivity : AppCompatActivity() {

    private lateinit var panel: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_panel)
        overridePendingTransition(0, 0) // we do our own animation

        panel = findViewById(R.id.panelContent)
        findViewById<View>(R.id.panelRoot).setOnClickListener { close() }

        findViewById<RecyclerView>(R.id.rvNotifications).apply {
            layoutManager = LinearLayoutManager(this@NotificationPanelActivity)
            adapter = NotificationAdapter(NotificationRepository.getAll().toMutableList())
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = close()
        })

        // Slide down from the top once the panel has been measured
        panel.post {
            panel.translationY = -panel.height.toFloat()
            panel.animate().translationY(0f).setDuration(300).start()
        }
    }

    private fun close() {
        panel.animate()
            .translationY(-panel.height.toFloat())
            .setDuration(220)
            .withEndAction {
                finish()
                overridePendingTransition(0, 0)
            }
            .start()
    }
}

private class NotificationAdapter(
    private val items: MutableList<NotificationItem>
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        items[position].bindTo(holder.itemView)
        holder.itemView.findViewById<View>(R.id.btnClose).setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    override fun getItemCount() = items.size
}