package com.aifranchise.ui.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.data.remote.InventoryItem
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentTransferBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferFragment : Fragment() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransferViewModel by viewModels()

    private var branchList: List<BranchDto> = emptyList()
    private var itemList: List<InventoryItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        viewModel.loadFormPrerequisites()

        binding.btnSubmitTransfer.setOnClickListener {
            submitTransfer()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.branchesState.collect { state ->
                when (state) {
                    is ResultState.Success -> {
                        branchList = state.data
                        val names = branchList.map { it.name }
                        val fromAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                        val toAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                        binding.spFromBranch.adapter = fromAdapter
                        binding.spToBranch.adapter = toAdapter
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { state ->
                when (state) {
                    is ResultState.Success -> {
                        itemList = state.data
                        val names = itemList.map { it.displayName() }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
                        binding.spItem.adapter = adapter
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transferState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.btnSubmitTransfer.isEnabled = false
                    }
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmitTransfer.isEnabled = true
                        Toast.makeText(requireContext(), "Transfer requested successfully (${state.data.distanceKm}km approx)", Toast.LENGTH_LONG).show()
                        binding.etQuantity.text?.clear()
                        viewModel.clearTransferState()
                    }
                    is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmitTransfer.isEnabled = true
                        Toast.makeText(requireContext(), state.exception.message, Toast.LENGTH_LONG).show()
                        viewModel.clearTransferState()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun submitTransfer() {
        if (branchList.isEmpty() || itemList.isEmpty()) {
            Toast.makeText(requireContext(), "Form data not fully loaded. Try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val fromIdx = binding.spFromBranch.selectedItemPosition
        val toIdx = binding.spToBranch.selectedItemPosition
        val itemIdx = binding.spItem.selectedItemPosition
        val quantityStr = binding.etQuantity.text.toString().trim()

        if (fromIdx < 0 || toIdx < 0 || itemIdx < 0) return
        
        if (fromIdx == toIdx) {
            Toast.makeText(requireContext(), "Source and Destination branch cannot be the same", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(requireContext(), "Enter quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull() ?: 0
        if (quantity <= 0) {
            Toast.makeText(requireContext(), "Must enter valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        val fromBranchId = branchList[fromIdx].id
        val toBranchId = branchList[toIdx].id
        val itemId = itemList[itemIdx].actualItemId()

        viewModel.submitManualTransfer(fromBranchId, toBranchId, itemId, quantity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
