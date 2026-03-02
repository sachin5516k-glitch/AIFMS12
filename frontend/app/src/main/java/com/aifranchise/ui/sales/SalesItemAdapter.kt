package com.aifranchise.ui.sales

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.ItemDto
import com.aifranchise.databinding.ItemSaleItemBinding

class SalesItemAdapter(
    private var items: List<ItemDto>,
    private val onItemSelected: (ItemDto) -> Unit
) : RecyclerView.Adapter<SalesItemAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(val binding: ItemSaleItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
        holder.binding.apply {
            tvItemName.text = item.name
            tvItemCategory.text = item.category ?: ""
            tvItemPrice.text = format.format(item.unitPrice)

            // Selection highlight
            if (position == selectedPosition) {
                cardItem.strokeColor = Color.parseColor("#6366F1")
                cardItem.strokeWidth = 4
                cardItem.setCardBackgroundColor(Color.parseColor("#EEF2FF"))
            } else {
                cardItem.strokeColor = Color.TRANSPARENT
                cardItem.strokeWidth = 0
                cardItem.setCardBackgroundColor(Color.WHITE)
            }

            root.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = holder.adapterPosition
                if (prev >= 0) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onItemSelected(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<ItemDto>) {
        items = newItems
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedItem(): ItemDto? {
        return if (selectedPosition in items.indices) items[selectedPosition] else null
    }
}
