package com.aifranchise.ui.dashboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.R
import com.aifranchise.data.remote.InventoryItem

class DashboardInventoryAdapter(
    private var items: List<InventoryItem> = emptyList()
) : RecyclerView.Adapter<DashboardInventoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvStockBadge: TextView = view.findViewById(R.id.tvStockBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val stock = item.closingStock

        holder.tvItemName.text = item.displayName()
        holder.tvStockBadge.text = "$stock units"

        // Color code the badge
        val color = when {
            stock > 50 -> Color.parseColor("#059669") // Green
            stock > 20 -> Color.parseColor("#D97706") // Amber
            stock > 0 -> Color.parseColor("#DC2626")  // Red
            else -> Color.parseColor("#6B7280")        // Gray for 0
        }
        val bg = holder.tvStockBadge.background
        if (bg is GradientDrawable) {
            bg.setColor(color)
        } else {
            val drawable = GradientDrawable()
            drawable.cornerRadius = 40f
            drawable.setColor(color)
            holder.tvStockBadge.background = drawable
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<InventoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
