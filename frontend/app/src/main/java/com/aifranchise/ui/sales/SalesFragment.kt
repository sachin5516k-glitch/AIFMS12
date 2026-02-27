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

        binding.btnSubmit.setOnClickListener {
            val quantity = binding.etQuantity.text.toString()
            val selectedId = binding.rgPaymentMode.checkedRadioButtonId
            val paymentMode = view.findViewById<RadioButton>(selectedId)?.text.toString()
            val mockItemId = "item_001" // Mocking spinner selection for Phase 2

            viewModel.submitSales("outlet_001", quantity, paymentMode, "")
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
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(context, "Sales Submitted! Fraud Score: ${state.data.fraudScore}", Toast.LENGTH_LONG).show()
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

    private fun resetForm() {
        binding.etQuantity.text?.clear()
        viewModel.resetState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
