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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiInsightsFragment : Fragment() {

    private var _binding: FragmentAiInsightsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiInsightsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadInsights("outlet_001") // Hardcoded for demo

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insightsState.collect { state ->
                when (state) {
                    is ResultState.Loading -> binding.progressBar.isVisible = true
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        val data = state.data
                        
                        binding.tvHealthScore.text = data.healthScore.toString()
                        binding.pbHealth.progress = data.healthScore
                        
                        binding.tvFraudScore.text = "${data.fraudProbability}%"
                        if (data.fraudProbability > 50) {
                            binding.tvFraudScore.setTextColor(Color.RED)
                        } else {
                            binding.tvFraudScore.setTextColor(Color.GREEN) // or safe color
                        }

                        binding.tvRiskLevel.text = "Failure Risk: ${data.failureRisk}"
                        binding.tvFactors.text = data.topFactors.joinToString("\n• ", prefix = "• ")
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
