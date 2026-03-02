package com.aifranchise.ui.branch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.databinding.ItemBranchBinding

class BranchesAdapter(
    private var branches: List<BranchDto>,
    private val onBranchClick: (BranchDto) -> Unit
) : RecyclerView.Adapter<BranchesAdapter.BranchViewHolder>() {

    fun submitList(newList: List<BranchDto>) {
        branches = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val binding = ItemBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BranchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        holder.bind(branches[position], onBranchClick)
    }

    override fun getItemCount() = branches.size

    class BranchViewHolder(private val binding: ItemBranchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(branch: BranchDto, onClick: (BranchDto) -> Unit) {
            binding.tvBranchName.text = branch.name
            binding.tvBranchStatus.text = "Status: ${branch.status.uppercase()}"
            
            if (branch.status.equals("active", ignoreCase = true)) {
                binding.tvBranchStatus.setTextColor(Color.parseColor("#059669"))
            } else {
                binding.tvBranchStatus.setTextColor(Color.parseColor("#DC2626"))
            }

            val address = branch.location?.address ?: ""
            val city = branch.location?.city ?: ""
            val addrStr = "$address $city".trim()
            
            if (addrStr.isNotEmpty()) {
                binding.tvBranchLocation.text = "Location: $addrStr"
            } else if (branch.location?.lat != null && branch.location?.lng != null) {
                binding.tvBranchLocation.text = String.format(java.util.Locale.US, "Location: %.4f, %.4f", branch.location.lat, branch.location.lng)
            } else {
                binding.tvBranchLocation.text = "Location: Pending"
            }

            binding.btnViewDetails.setOnClickListener { onClick(branch) }
        }
    }
}
