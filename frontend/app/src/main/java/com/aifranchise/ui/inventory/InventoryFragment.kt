package com.aifranchise.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.InventoryUpdateItem
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentInventoryBinding
import com.aifranchise.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter
    private val updates = mutableMapOf<String, InventoryUpdateItem>()
    private var currentEditItem: InventoryItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InventoryAdapter(emptyList()) { item ->
            showModal(item)
        }
        
        binding.rvInventory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InventoryFragment.adapter
        }

        binding.btnSubmitInventory.setOnClickListener {
            viewModel.submitInventory("outlet_001", updates.values.toList())
        }

        binding.btnSaveModal.setOnClickListener {
            currentEditItem?.let { item ->
                val added = binding.etQuantityAdded.text.toString().toIntOrNull() ?: 0
                updates[item.id] = InventoryUpdateItem(item.id, added)
                hideModal()
                Toast.makeText(context, "${item.name} staged to be updated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancelModal.setOnClickListener {
            hideModal()
        }

        val swipeRefresh = binding.root.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            viewModel.loadItems()
        }

        viewModel.loadItems()

        observeViewModel()
    }

    private fun showModal(item: InventoryItem) {
        currentEditItem = item
        binding.tvModalTitle.text = "Update ${item.name}"
        binding.etQuantityAdded.setText(updates[item.id]?.quantityAdded?.toString() ?: "")
        
        binding.viewOverlay.isVisible = true
        binding.viewOverlay.alpha = 0f
        binding.viewOverlay.animate().alpha(1f).setDuration(200).start()
        
        binding.cvStockModal.isVisible = true
        binding.cvStockModal.translationY = 500f
        binding.cvStockModal.animate().translationY(0f).setDuration(300).start()
    }

    private fun hideModal() {
        binding.viewOverlay.animate().alpha(0f).setDuration(200).withEndAction {
            binding.viewOverlay.isVisible = false
        }.start()
        
        binding.cvStockModal.animate().translationY(500f).setDuration(300).withEndAction {
            binding.cvStockModal.isVisible = false
            currentEditItem = null
        }.start()
    }

    private fun observeViewModel() {
        val swipeRefresh = binding.root.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        if (swipeRefresh?.isRefreshing != true) {
                            binding.progressBar.isVisible = true
                        }
                    }
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        swipeRefresh?.isRefreshing = false
                        if (state.data.isEmpty()) {
                            binding.tvEmptyInventory.isVisible = true
                            binding.rvInventory.isVisible = false
                        } else {
                            binding.tvEmptyInventory.isVisible = false
                            binding.rvInventory.isVisible = true
                            adapter.submitList(state.data)
                            
                            // Check Low Stock Notification
                            val lowStockItems = state.data.filter { it.lastStock < 10 }
                            if (lowStockItems.isNotEmpty()) {
                                com.aifranchise.util.NotificationHelper.showNotification(
                                    requireContext(),
                                    "Low Stock Alert",
                                    "${lowStockItems.size} items are running critically low.",
                                    1001
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        swipeRefresh?.isRefreshing = false
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_SHORT).show()
                        if (adapter.itemCount == 0) binding.tvEmptyInventory.isVisible = true
                    }
                    null -> Unit
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.submissionState.collect { state ->
                 when (state) {
                    is ResultState.Loading -> binding.progressBar.isVisible = true
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, "Inventory Updated successfully!", Toast.LENGTH_SHORT).show()
                        updates.clear() // Clear cache
                    }
                     is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> Unit
                 }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
