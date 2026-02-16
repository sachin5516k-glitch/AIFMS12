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
import com.aifranchise.data.remote.InventoryUpdateItem
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentInventoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter
    private val updates = mutableMapOf<String, InventoryUpdateItem>()

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

        adapter = InventoryAdapter(emptyList()) { id, open, close ->
            updates[id] = InventoryUpdateItem(id, open, close)
        }
        
        binding.rvInventory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InventoryFragment.adapter
        }

        binding.btnSubmitInventory.setOnClickListener {
            // Hardcoded outletId
            viewModel.submitInventory("outlet_001", updates.values.toList())
        }

        viewModel.loadItems()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { state ->
                when (state) {
                    is ResultState.Loading -> binding.progressBar.isVisible = true
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        adapter.submitList(state.data)
                    }
                    is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_SHORT).show()
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
                         Toast.makeText(context, "Inventory Updated!", Toast.LENGTH_SHORT).show()
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
