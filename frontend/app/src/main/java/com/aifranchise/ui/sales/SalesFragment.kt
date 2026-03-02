package com.aifranchise.ui.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentSalesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Stepper Logic
        binding.btnPlus.setOnClickListener {
            val q = binding.etQuantity.text.toString().toIntOrNull() ?: 1
            binding.etQuantity.setText((q + 1).toString())
            updateLiveTotal()
        }

        binding.btnMinus.setOnClickListener {
            val q = binding.etQuantity.text.toString().toIntOrNull() ?: 1
            if (q > 1) {
                binding.etQuantity.setText((q - 1).toString())
                updateLiveTotal()
            }
        }

        binding.btnSubmit.setOnClickListener {
            val quantity = binding.etQuantity.text.toString()
            val selectedId = binding.rgPaymentMode.checkedRadioButtonId
            val paymentMode = view.findViewById<RadioButton>(selectedId)?.text.toString()
            val mockItemId = "item_001" // Mocking spinner selection for Phase 2

            viewModel.submitSales("outlet_001", quantity, paymentMode)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.salesState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.btnSubmit.isEnabled = false
                    }
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, "Sales Submitted! Fraud Score: ${state.data.fraudScore}", Toast.LENGTH_LONG).show()
                        
                        // Fire a sales milestone notification to satisfy Part 5
                        val lastAmount = binding.tvLiveTotal.text.toString().replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
                        if(lastAmount > 0.0) {
                            com.aifranchise.util.NotificationHelper.showNotification(
                                requireContext(),
                                "Sales Milestone",
                                "Great job! A sale of $$lastAmount was just recorded."
                            )
                        }
                        
                        resetForm()
                    }
                    is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(context, "Error: ${state.exception.message}", Toast.LENGTH_LONG).show()
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun updateLiveTotal() {
        // Mock Item Price: $12.00
        val q = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val total = q * 12.0
        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
        binding.tvLiveTotal.text = format.format(total)
        
        // Add a subtle bounce animation to total when updated
        binding.tvLiveTotal.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
            binding.tvLiveTotal.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    private fun resetForm() {
        binding.etQuantity.setText("1")
        updateLiveTotal()
        viewModel.resetState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
