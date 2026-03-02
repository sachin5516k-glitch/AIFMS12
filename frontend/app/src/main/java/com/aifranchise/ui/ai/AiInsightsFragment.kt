package com.aifranchise.ui.ai

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentAiInsightsBinding
import com.aifranchise.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiInsightsFragment : Fragment() {

    private var _binding: FragmentAiInsightsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiInsightsViewModel by viewModels()

    @javax.inject.Inject
    lateinit var tokenManager: com.aifranchise.util.TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val branchId = tokenManager.getBranchId() ?: "global"

        val swipeRefresh = binding.root.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            viewModel.loadInsights(branchId)
        }

        viewModel.loadInsights(branchId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insightsState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        if (swipeRefresh?.isRefreshing != true) {
                            binding.progressBar.isVisible = true
                        }
                    }
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        swipeRefresh?.isRefreshing = false
                        val data = state.data
                        
                        binding.tvHealthScore.text = "Global Health: ${data.healthScore}%"
                        setupPieChart(data.healthScore)
                        
                        binding.tvFraudScore.text = "Fraud Probability: ${data.fraudProbability}%"
                        if (data.fraudProbability > 50) {
                            binding.tvFraudScore.setTextColor(Color.parseColor("#DC2626"))
                        } else {
                            binding.tvFraudScore.setTextColor(Color.parseColor("#059669"))
                        }

                        binding.tvRiskLevel.text = "Overall Risk Level: ${data.failureRisk}"
                        binding.tvFactors.text = data.topFactors.joinToString("\n• ", prefix = "• ")
                    }
                    is ResultState.Error -> {
                         binding.progressBar.isVisible = false
                         swipeRefresh?.isRefreshing = false
                         Toast.makeText(context, state.exception.message ?: "Failed to load insights", Toast.LENGTH_SHORT).show()
                    }
                    null -> Unit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemSalesState.collect { state ->
                if (state is ResultState.Success) {
                    setupBarChart(state.data)
                }
            }
        }
    }

    private fun setupPieChart(healthScore: Int) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(healthScore.toFloat(), "Healthy"))
        entries.add(PieEntry((100 - healthScore).toFloat(), "Risk"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.parseColor("#059669"), Color.parseColor("#DC2626"))
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val pieData = PieData(dataSet)
        binding.pieChartHealth.data = pieData
        binding.pieChartHealth.description.isEnabled = false
        binding.pieChartHealth.legend.isEnabled = false
        binding.pieChartHealth.centerText = "$healthScore%"
        binding.pieChartHealth.setCenterTextSize(24f)
        binding.pieChartHealth.animateY(1000)
    }

    private fun setupBarChart(salesData: List<com.aifranchise.data.remote.AnalyticsItemSalesDto>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        salesData.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.totalSales.toFloat()))
            labels.add(item.itemName)
        }

        if (entries.isEmpty()) {
            binding.barChartBranches.clear()
            return
        }

        val dataSet = BarDataSet(entries, "Total Units Sold (Last 7 Days)")
        dataSet.colors = listOf(Color.parseColor("#D97706"))
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        binding.barChartBranches.data = barData
        binding.barChartBranches.description.isEnabled = false
        
        binding.barChartBranches.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChartBranches.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChartBranches.xAxis.setDrawGridLines(false)
        binding.barChartBranches.xAxis.granularity = 1f
        binding.barChartBranches.xAxis.isGranularityEnabled = true
        binding.barChartBranches.axisLeft.axisMinimum = 0f
        binding.barChartBranches.axisRight.isEnabled = false
        
        binding.barChartBranches.animateY(1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
