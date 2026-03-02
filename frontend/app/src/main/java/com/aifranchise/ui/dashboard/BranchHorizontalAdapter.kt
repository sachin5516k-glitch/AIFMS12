package com.aifranchise.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.databinding.ItemBranchHorizontalBinding

class BranchHorizontalAdapter(
    private var branches: List<BranchDto>,
    private val onClick: (BranchDto) -> Unit
) : RecyclerView.Adapter<BranchHorizontalAdapter.ViewHolder>() {

    fun submitList(newList: List<BranchDto>) {
        branches = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBranchHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val branch = branches[position]
        holder.bind(branch)
        holder.itemView.setOnClickListener { onClick(branch) }
    }

    override fun getItemCount(): Int = branches.size

    class ViewHolder(private val binding: ItemBranchHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(branch: BranchDto) {
            binding.tvBranchName.text = branch.name
            
            val hStatus = branch.healthStatus ?: "Unknown"
            binding.tvBranchHealth.text = "Stock: $hStatus"
            
            val color = when (hStatus.lowercase()) {
                "green", "good" -> "#059669"
                "yellow", "warning" -> "#D97706"
                "red", "critical" -> "#DC2626"
                else -> "#6B7280"
            }
            binding.tvBranchHealth.setTextColor(Color.parseColor(color))
        }
    }
}
