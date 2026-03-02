package com.aifranchise.ui.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.R
import com.aifranchise.data.remote.ItemDto
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private var items: List<ItemDto> = emptyList()
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemCategory: TextView = view.findViewById(R.id.tvItemCategory)
        val tvItemPrice: TextView = view.findViewById(R.id.tvItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvItemName.text = item.name
        holder.tvItemCategory.text = item.category ?: "Uncategorized"
        holder.tvItemPrice.text = currencyFormat.format(item.unitPrice)
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<ItemDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
