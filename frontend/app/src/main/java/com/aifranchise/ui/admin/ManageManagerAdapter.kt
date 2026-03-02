package com.aifranchise.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.ManagerDto
import com.aifranchise.databinding.ItemManagerBinding

class ManageManagerAdapter(
    private val onDeactivateClick: (ManagerDto) -> Unit
) : ListAdapter<ManagerDto, ManageManagerAdapter.ManagerViewHolder>(ManagerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagerViewHolder {
        val binding = ItemManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManagerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ManagerViewHolder(private val binding: ItemManagerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(manager: ManagerDto) {
            binding.tvManagerName.text = manager.name
            binding.tvManagerEmail.text = manager.email ?: "No Email"
            binding.tvBranchName.text = "Branch: ${manager.branchId?.name ?: "None"}"
            
            binding.tvStatus.text = manager.status.uppercase()
            if (manager.status == "active") {
                binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                binding.btnDeactivate.visibility = View.VISIBLE
            } else {
                binding.tvStatus.setTextColor(Color.parseColor("#F44336"))
                binding.btnDeactivate.visibility = View.GONE
            }

            binding.btnDeactivate.setOnClickListener {
                onDeactivateClick(manager)
            }
        }
    }

    class ManagerDiffCallback : DiffUtil.ItemCallback<ManagerDto>() {
        override fun areItemsTheSame(oldItem: ManagerDto, newItem: ManagerDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ManagerDto, newItem: ManagerDto): Boolean {
            return oldItem == newItem
        }
    }
}
