package com.aifranchise.ui.inventory

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.InventoryUpdateItem
import com.aifranchise.databinding.ItemInventoryBinding

class InventoryAdapter(
    private var items: List<InventoryItem>,
    private val onUpdate: (String, Int, Int) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    // Cache user inputs to prevent recycling issues
    private val openingCache = mutableMapOf<String, Int>()
    private val closingCache = mutableMapOf<String, Int>()

    inner class ViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.name
            tvLastStock.text = "Last: ${item.lastStock}"

            // Remove listeners before setting text to avoid infinite loops
            etOpening.onFocusChangeListener = null
            etClosing.onFocusChangeListener = null

            etOpening.setText(openingCache[item.id]?.toString() ?: "")
            etClosing.setText(closingCache[item.id]?.toString() ?: "")

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val open = etOpening.text.toString().toIntOrNull() ?: 0
                    val close = etClosing.text.toString().toIntOrNull() ?: 0
                    openingCache[item.id] = open
                    closingCache[item.id] = close
                    onUpdate(item.id, open, close)
                }
            }
            etOpening.addTextChangedListener(textWatcher)
            etClosing.addTextChangedListener(textWatcher)
        }
    }

    override fun getItemCount() = items.size
    
    fun submitList(newItems: List<InventoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
