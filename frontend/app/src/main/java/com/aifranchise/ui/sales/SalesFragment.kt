package com.aifranchise.ui.sales

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.ItemDto
import com.aifranchise.data.remote.ResultState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SalesFragment : Fragment(R.layout.fragment_sales) {

    private val viewModel: SalesViewModel by viewModels()
    private var selectedItem: ItemDto? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvLiveTotal = view.findViewById<TextView>(R.id.tvLiveTotal)
        val pbItems = view.findViewById<android.widget.ProgressBar>(R.id.pbItems)
        val tvItemsError = view.findViewById<TextView>(R.id.tvItemsError)
        val rvItems = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItems)
        val etQuantity = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etQuantity)
        val btnMinus = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMinus)
        val btnPlus = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPlus)
        val rgPaymentMode = view.findViewById<android.widget.RadioGroup>(R.id.rgPaymentMode)
        val btnSubmit = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubmit)
        val progressBar = view.findViewById<android.widget.ProgressBar>(R.id.progressBar)

        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))

        fun updateLiveTotal() {
            val q = etQuantity.text.toString().toIntOrNull() ?: 0
            val price = selectedItem?.unitPrice ?: 0.0
            val total = q * price
            tvLiveTotal.text = format.format(total)
            tvLiveTotal.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                tvLiveTotal.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
        }

        val itemAdapter = SalesItemAdapter(emptyList()) { item ->
            selectedItem = item
            updateLiveTotal()
        }
        rvItems.adapter = itemAdapter

        // Load items from database
        viewModel.loadItems()

        // Stepper logic
        btnPlus.setOnClickListener {
            val q = etQuantity.text.toString().toIntOrNull() ?: 1
            etQuantity.setText((q + 1).toString())
            updateLiveTotal()
        }

        btnMinus.setOnClickListener {
            val q = etQuantity.text.toString().toIntOrNull() ?: 1
            if (q > 1) {
                etQuantity.setText((q - 1).toString())
                updateLiveTotal()
            }
        }

        btnSubmit.setOnClickListener {
            val itemId = selectedItem?.id
            if (itemId == null) {
                Toast.makeText(context, "Please select an item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
            if (quantity < 1) {
                Toast.makeText(context, "Quantity must be at least 1", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedId = rgPaymentMode.checkedRadioButtonId
            val paymentMode = view.findViewById<RadioButton>(selectedId)?.text?.toString() ?: ""
            if (paymentMode.isBlank()) {
                Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitSales(itemId, quantity, paymentMode)
        }

        // Observe items state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        pbItems.isVisible = true
                        rvItems.isVisible = false
                        tvItemsError.isVisible = false
                    }
                    is ResultState.Success -> {
                        pbItems.isVisible = false
                        val items = state.data ?: emptyList()
                        if (items.isEmpty()) {
                            tvItemsError.text = "No items found. Admin must add items first."
                            tvItemsError.isVisible = true
                        } else {
                            itemAdapter.submitList(items)
                            rvItems.isVisible = true
                        }
                    }
                    is ResultState.Error -> {
                        pbItems.isVisible = false
                        tvItemsError.text = "Failed to load items: ${state.exception.message}"
                        tvItemsError.isVisible = true
                    }
                    null -> Unit
                }
            }
        }

        // Observe sales submission state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.salesState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        progressBar.isVisible = true
                        btnSubmit.isEnabled = false
                    }
                    is ResultState.Success -> {
                        progressBar.isVisible = false
                        btnSubmit.isEnabled = true
                        val data = state.data
                        val msg = if (data?.totalAmount != null) {
                            "Sale recorded! Total: ${format.format(data.totalAmount)}"
                        } else {
                            "Sale recorded successfully!"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                        // Reset form
                        etQuantity.setText("1")
                        selectedItem = null
                        tvLiveTotal.text = format.format(0.0)
                        itemAdapter.submitList(itemAdapter.getSelectedItem()?.let { emptyList() } ?: emptyList())
                        // Reload items to reflect updated stock availability
                        viewModel.loadItems()
                        viewModel.resetState()
                    }
                    is ResultState.Error -> {
                        progressBar.isVisible = false
                        btnSubmit.isEnabled = true
                        Toast.makeText(context, "Error: ${state.exception.message}", Toast.LENGTH_LONG).show()
                    }
                    null -> Unit
                }
            }
        }
    }
}
