package com.aifranchise.ui.inventory

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.databinding.ItemInventoryBinding

class InventoryAdapter(
    private var items: List<InventoryItem>,
    private val onUpdateClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.name
            tvLastStock.text = "${item.lastStock} units"
            
            pbStock.progress = Math.min(item.lastStock, 100)
            
            // Color coding Stock Health
            val color = when {
                item.lastStock > 50 -> Color.parseColor("#059669") // Green
                item.lastStock > 20 -> Color.parseColor("#D97706") // Orange
                else -> Color.parseColor("#DC2626") // Red
            }
            pbStock.progressTintList = ColorStateList.valueOf(color)

            btnUpdateStock.setOnClickListener {
                onUpdateClick(item)
            }
        }
    }

    override fun getItemCount() = items.size
    
    fun submitList(newItems: List<InventoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
