package com.aifranchise.ui.branch

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.ActivityBranchDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BranchDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBranchDetailBinding
    private val viewModel: BranchDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBranchDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val branchId = intent.getStringExtra("BRANCH_ID") ?: ""
        val branchName = intent.getStringExtra("BRANCH_NAME") ?: "Unknown Branch"

        binding.tvDetailTitle.text = branchName
        binding.tvDetailName.text = "Name: $branchName"
        binding.tvDetailAddress.text = "Address: Loading..."

        binding.btnBack.setOnClickListener {
            finish()
        }

        if (branchId.isNotEmpty()) {
            observeViewModel()
            viewModel.fetchBranchDetails(branchId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.branchState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                    }
                    is ResultState.Success -> {
                        val branch = state.data
                        
                        val addressStr = buildString {
                            if (!branch.location?.address.isNullOrEmpty()) append(branch.location?.address).append(", ")
                            if (!branch.location?.city.isNullOrEmpty()) append(branch.location?.city).append(", ")
                            if (!branch.location?.state.isNullOrEmpty()) append(branch.location?.state).append(", ")
                            if (branch.location?.lat != null && branch.location?.lng != null) {
                                append("Lat: ${branch.location.lat}, Lng: ${branch.location.lng}")
                            }
                        }.trimEnd(',', ' ')
                        
                        binding.tvDetailAddress.text = "Address: ${if(addressStr.isEmpty()) "Pending" else addressStr}"
                        binding.tvDetailManager.text = "Manager: ${branch.managerName}"
                        binding.tvDetailStaff.text = "Staff Count: ${branch.staffCount} Employees"
                        
                        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
                        binding.tvDetailSales.text = "Sales: ${format.format(branch.totalSales)}"
                        binding.tvDetailStockHealth.text = "Stock Health: ${branch.stockHealth}"
                    }
                    is ResultState.Error -> {
                        Toast.makeText(this@BranchDetailActivity, state.exception.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
