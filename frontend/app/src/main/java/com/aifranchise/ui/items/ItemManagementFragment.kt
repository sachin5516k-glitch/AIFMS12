package com.aifranchise.ui.items

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
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemManagementFragment : Fragment() {

    private val viewModel: ItemManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_item_management, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etItemName)
        val etCategory = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCategory)
        val etUnitPrice = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUnitPrice)
        val etPurchaseCost = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPurchaseCost)
        val btnCreate = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreateItem)
        val progressCreate = view.findViewById<android.widget.ProgressBar>(R.id.progressCreate)
        val progressItems = view.findViewById<android.widget.ProgressBar>(R.id.progressItems)
        val tvEmpty = view.findViewById<android.widget.TextView>(R.id.tvEmptyItems)
        val rvItems = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItems)

        val adapter = ItemAdapter()
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        rvItems.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            viewModel.loadItems()
        }

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val price = etUnitPrice.text.toString().toDoubleOrNull()
            val cost = etPurchaseCost.text.toString().toDoubleOrNull()

            if (name.isEmpty()) {
                etName.error = "Required"
                return@setOnClickListener
            }
            if (category.isEmpty()) {
                etCategory.error = "Required"
                return@setOnClickListener
            }
            if (price == null || price <= 0) {
                etUnitPrice.error = "Enter valid price"
                return@setOnClickListener
            }
            if (cost == null || cost <= 0) {
                etPurchaseCost.error = "Enter valid cost"
                return@setOnClickListener
            }

            viewModel.createItem(name, category, price, cost)
        }

        // Observe items list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { state ->
                swipeRefresh.isRefreshing = false
                when (state) {
                    is ResultState.Loading -> {
                        progressItems.isVisible = true
                        rvItems.isVisible = false
                        tvEmpty.isVisible = false
                    }
                    is ResultState.Success -> {
                        progressItems.isVisible = false
                        val items = state.data
                        if (items.isEmpty()) {
                            tvEmpty.isVisible = true
                            rvItems.isVisible = false
                        } else {
                            tvEmpty.isVisible = false
                            adapter.submitList(items)
                            rvItems.isVisible = true
                        }
                    }
                    is ResultState.Error -> {
                        progressItems.isVisible = false
                        tvEmpty.text = "Error: ${state.exception.message}"
                        tvEmpty.isVisible = true
                        rvItems.isVisible = false
                    }
                    null -> {}
                }
            }
        }

        // Observe create state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        progressCreate.isVisible = true
                        btnCreate.isEnabled = false
                    }
                    is ResultState.Success -> {
                        progressCreate.isVisible = false
                        btnCreate.isEnabled = true
                        Toast.makeText(context, "✅ Item '${state.data.name}' created!", Toast.LENGTH_SHORT).show()
                        etName.text?.clear()
                        etCategory.text?.clear()
                        etUnitPrice.text?.clear()
                        etPurchaseCost.text?.clear()
                        viewModel.clearCreateState()
                    }
                    is ResultState.Error -> {
                        progressCreate.isVisible = false
                        btnCreate.isEnabled = true
                        Toast.makeText(context, "❌ ${state.exception.message}", Toast.LENGTH_LONG).show()
                        viewModel.clearCreateState()
                    }
                    null -> {}
                }
            }
        }

        viewModel.loadItems()
    }
}
